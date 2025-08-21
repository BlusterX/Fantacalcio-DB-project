package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.Classifica;
import fantacalcio.util.DatabaseConnection;

/**
 * DAO per la tabella CLASSIFICA.
 * Tabella: CLASSIFICA(ID_Classifica PK, Punti, Vittorie, Pareggi, Sconfitte,
 *                    Punteggio_totale, Punteggio_subito)
 * Relazione: SQUADRA_FANTACALCIO.ID_Classifica -> CLASSIFICA.ID_Classifica
 */
public class ClassificaDAO {

    private final DatabaseConnection db;

    public ClassificaDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    /* ========================= CRUD BASE ========================= */

    /** Crea una riga di classifica "vuota" (tutti zero) e ritorna l'ID generato. */
    public Optional<Integer> creaClassificaVuota() {
        final String sql = "INSERT INTO CLASSIFICA (Punti, Vittorie, Pareggi, Sconfitte, Punteggio_totale, Punteggio_subito) " +
                           "VALUES (0,0,0,0,0,0)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) return Optional.of(gk.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore creazione classifica: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Trova una classifica per ID. */
    public Optional<Classifica> trovaPerId(int idClassifica) {
        final String sql = "SELECT * FROM CLASSIFICA WHERE ID_Classifica = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idClassifica);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore lettura classifica: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Azzera le statistiche della classifica indicata. */
    public boolean reset(int idClassifica) {
        final String sql = """
            UPDATE CLASSIFICA
            SET Punti=0, Vittorie=0, Pareggi=0, Sconfitte=0, Punteggio_totale=0, Punteggio_subito=0
            WHERE ID_Classifica=?
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idClassifica);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore reset classifica: " + e.getMessage());
            return false;
        }
    }

    /* ===================== UPDATE A EVENTO PARTITA ===================== */

    /** Registra una vittoria: +3 punti, +1 vittoria, somma fatti/subiti. */
    public boolean registraVittoria(int idClassifica, double puntiFatti, double puntiSubiti) {
        return applyMatchDelta(idClassifica, 3, 1, 0, 0, puntiFatti, puntiSubiti);
    }

    /** Registra un pareggio: +1 punto, +1 pareggio, somma fatti/subiti. */
    public boolean registraPareggio(int idClassifica, double puntiFatti, double puntiSubiti) {
        return applyMatchDelta(idClassifica, 1, 0, 1, 0, puntiFatti, puntiSubiti);
    }

    /** Registra una sconfitta: +0 punti, +1 sconfitta, somma fatti/subiti. */
    public boolean registraSconfitta(int idClassifica, double puntiFatti, double puntiSubiti) {
        return applyMatchDelta(idClassifica, 0, 0, 0, 1, puntiFatti, puntiSubiti);
    }

    private boolean applyMatchDelta(int idClassifica,
                                    int deltaPunti, int dVitt, int dPar, int dSconf,
                                    double fatti, double subiti) {
        final String sql = """
            UPDATE CLASSIFICA
            SET Punti = Punti + ?,
                Vittorie = Vittorie + ?,
                Pareggi = Pareggi + ?,
                Sconfitte = Sconfitte + ?,
                Punteggio_totale = Punteggio_totale + ?,
                Punteggio_subito = Punteggio_subito + ?
            WHERE ID_Classifica = ?
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deltaPunti);
            ps.setInt(2, dVitt);
            ps.setInt(3, dPar);
            ps.setInt(4, dSconf);
            ps.setDouble(5, fatti);
            ps.setDouble(6, subiti);
            ps.setInt(7, idClassifica);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento classifica (delta): " + e.getMessage());
            return false;
        }
    }

    /* ===================== LETTURE PER LEGA (JOIN) ===================== */

    /**
     * Ritorna la classifica di una lega:
     * join CLASSIFICA <- SQUADRA_FANTACALCIO per avere nome squadra e ID.
     * Ordinamento: Punti desc, (Punteggio_totale - Punteggio_subito) desc, Vittorie desc, Nome asc.
     */
    public List<RigaClassificaLega> classificaPerLega(int idLega) {
        final String sql = """
            SELECT sf.ID_Squadra_Fantacalcio,
                   sf.Nome AS Nome_Squadra,
                   c.ID_Classifica,
                   c.Punti, c.Vittorie, c.Pareggi, c.Sconfitte,
                   c.Punteggio_totale, c.Punteggio_subito,
                   (c.Punteggio_totale - c.Punteggio_subito) AS DiffPunti
            FROM SQUADRA_FANTACALCIO sf
            JOIN CLASSIFICA c ON c.ID_Classifica = sf.ID_Classifica
            WHERE sf.ID_Lega = ?
            ORDER BY c.Punti DESC, DiffPunti DESC, c.Vittorie DESC, sf.Nome ASC
        """;
        List<RigaClassificaLega> out = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RigaClassificaLega r = new RigaClassificaLega();
                    r.idSquadraFantacalcio = rs.getInt("ID_Squadra_Fantacalcio");
                    r.nomeSquadra = rs.getString("Nome_Squadra");
                    r.idClassifica = rs.getInt("ID_Classifica");
                    r.punti = rs.getInt("Punti");
                    r.vittorie = rs.getInt("Vittorie");
                    r.pareggi = rs.getInt("Pareggi");
                    r.sconfitte = rs.getInt("Sconfitte");
                    r.punteggioTotale = rs.getDouble("Punteggio_totale");
                    r.punteggioSubito = rs.getDouble("Punteggio_subito");
                    r.diffPunti = rs.getDouble("DiffPunti");
                    out.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore lettura classifica lega: " + e.getMessage());
        }
        return out;
    }

    /* ========================= HELPERS ========================= */

    private Classifica fromResultSet(ResultSet rs) throws SQLException {
        Classifica c = new Classifica();
        c.setIdClassifica(rs.getInt("ID_Classifica"));
        c.setPunti(rs.getInt("Punti"));
        c.setVittorie(rs.getInt("Vittorie"));
        c.setPareggi(rs.getInt("Pareggi"));
        c.setSconfitte(rs.getInt("Sconfitte"));
        c.setPunteggioTotale(rs.getDouble("Punteggio_totale"));
        c.setPunteggioSubito(rs.getDouble("Punteggio_subito"));
        return c;
    }

    /** DTO leggero per la classifica di lega (per evitare di creare subito un nuovo model). */
    public static class RigaClassificaLega {
        public int idSquadraFantacalcio;
        public String nomeSquadra;
        public int idClassifica;
        public int punti;
        public int vittorie;
        public int pareggi;
        public int sconfitte;
        public double punteggioTotale;
        public double punteggioSubito;
        public double diffPunti;

        @Override public String toString() {
            return String.format("%s: %dp (V:%d N:%d P:%d)  Tot:%.2f Sub:%.2f Diff:%.2f",
                    nomeSquadra, punti, vittorie, pareggi, sconfitte,
                    punteggioTotale, punteggioSubito, diffPunti);
        }
    }
}
