package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.FasciaGiocatore;
import fantacalcio.util.DatabaseConnection;

/**
 * DAO per tabella FASCIA_GIOCATORE
 * Colonne:
 *  ID_Fascia (PK), Nome_fascia,
 *  Prob_gol_attaccante, Prob_gol_centrocampista, Prob_gol_difensore,
 *  Prob_assist, Prob_ammonizione, Prob_espulsione, Prob_imbattibilita,
 *  Voto_base_standard
 */
public class FasciaGiocatoreDAO {

    private final DatabaseConnection db;

    public FasciaGiocatoreDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public boolean inserisci(FasciaGiocatore f) {
        final String sql = """
            INSERT INTO FASCIA_GIOCATORE
            (Nome_fascia, Prob_gol_attaccante, Prob_gol_centrocampista, Prob_gol_difensore,
             Prob_assist, Prob_ammonizione, Prob_espulsione, Prob_imbattibilita, Voto_base_standard)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, f.getNomeFascia());
            ps.setDouble(2, f.getProbGolAttaccante());
            ps.setDouble(3, f.getProbGolCentrocampista());
            ps.setDouble(4, f.getProbGolDifensore());
            ps.setDouble(5, f.getProbAssist());
            ps.setDouble(6, f.getProbAmmonizione());
            ps.setDouble(7, f.getProbEspulsione());
            ps.setDouble(8, f.getProbImbattibilita());
            ps.setDouble(9, f.getVotoBaseStandard());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) f.setIdFascia(gk.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore inserimento fascia: " + e.getMessage());
        }
        return false;
    }

    public Optional<FasciaGiocatore> trovaPerId(int idFascia) {
        final String sql = "SELECT * FROM FASCIA_GIOCATORE WHERE ID_Fascia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFascia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore find by id fascia: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<FasciaGiocatore> trovaPerNome(String nomeFascia) {
        final String sql = "SELECT * FROM FASCIA_GIOCATORE WHERE Nome_fascia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeFascia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore find by nome fascia: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<FasciaGiocatore> trovaTutte() {
        final String sql = "SELECT * FROM FASCIA_GIOCATORE ORDER BY Nome_fascia";
        List<FasciaGiocatore> out = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Errore find all fasce: " + e.getMessage());
        }
        return out;
    }

    public boolean aggiorna(FasciaGiocatore f) {
        final String sql = """
            UPDATE FASCIA_GIOCATORE
            SET Nome_fascia = ?, 
                Prob_gol_attaccante = ?, Prob_gol_centrocampista = ?, Prob_gol_difensore = ?,
                Prob_assist = ?, Prob_ammonizione = ?, Prob_espulsione = ?, Prob_imbattibilita = ?,
                Voto_base_standard = ?
            WHERE ID_Fascia = ?
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, f.getNomeFascia());
            ps.setDouble(2, f.getProbGolAttaccante());
            ps.setDouble(3, f.getProbGolCentrocampista());
            ps.setDouble(4, f.getProbGolDifensore());
            ps.setDouble(5, f.getProbAssist());
            ps.setDouble(6, f.getProbAmmonizione());
            ps.setDouble(7, f.getProbEspulsione());
            ps.setDouble(8, f.getProbImbattibilita());
            ps.setDouble(9, f.getVotoBaseStandard());
            ps.setInt(10, f.getIdFascia());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore update fascia: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una fascia.
     * Prima pulisce la relazione in FASCIA_APPARTENENZA (FK verso FASCIA_GIOCATORE).
     */
    public boolean elimina(int idFascia) {
        final String delRel = "DELETE FROM FASCIA_APPARTENENZA WHERE ID_Fascia = ?";
        final String delFas = "DELETE FROM FASCIA_GIOCATORE WHERE ID_Fascia = ?";
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(delRel);
                 PreparedStatement p2 = conn.prepareStatement(delFas)) {

                p1.setInt(1, idFascia);
                p1.executeUpdate();

                p2.setInt(1, idFascia);
                int rows = p2.executeUpdate();

                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore delete fascia: " + e.getMessage());
            return false;
        }
    }

    private FasciaGiocatore mapRow(ResultSet rs) throws SQLException {
        FasciaGiocatore f = new FasciaGiocatore();
        f.setIdFascia(rs.getInt("ID_Fascia"));
        f.setNomeFascia(rs.getString("Nome_fascia"));
        f.setProbGolAttaccante(rs.getDouble("Prob_gol_attaccante"));
        f.setProbGolCentrocampista(rs.getDouble("Prob_gol_centrocampista"));
        f.setProbGolDifensore(rs.getDouble("Prob_gol_difensore"));
        f.setProbAssist(rs.getDouble("Prob_assist"));
        f.setProbAmmonizione(rs.getDouble("Prob_ammonizione"));
        f.setProbEspulsione(rs.getDouble("Prob_espulsione"));
        f.setProbImbattibilita(rs.getDouble("Prob_imbattibilita"));
        f.setVotoBaseStandard(rs.getDouble("Voto_base_standard"));
        return f;
    }
}
