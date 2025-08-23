package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.FasciaAppartenenza;
import fantacalcio.model.FasciaGiocatore;
import fantacalcio.util.DatabaseConnection;

/**
 * DAO per tabella FASCIA_APPARTENENZA (relazione Calciatore <-> Fascia)
 * PK composta: (ID_Calciatore, ID_Fascia)
 */
public class FasciaAppartenenzaDAO {

    private final DatabaseConnection db;

    public FasciaAppartenenzaDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    /** Assegna una fascia a un calciatore */
    public boolean assegnaFascia(int idCalciatore, int idFascia) {
        final String sql = "INSERT INTO FASCIA_APPARTENENZA (ID_Calciatore, ID_Fascia) VALUES (?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            ps.setInt(2, idFascia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore assegnazione fascia: " + e.getMessage());
            return false;
        }
    }

    /** Rimuove l'appartenenza fascia da un calciatore */
    public boolean rimuoviFascia(int idCalciatore, int idFascia) {
        final String sql = "DELETE FROM FASCIA_APPARTENENZA WHERE ID_Calciatore = ? AND ID_Fascia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            ps.setInt(2, idFascia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore rimozione fascia: " + e.getMessage());
            return false;
        }
    }

    /** Rimuove tutte le fasce da un calciatore */
    public int rimuoviTutteLeFasceDelCalciatore(int idCalciatore) {
        final String sql = "DELETE FROM FASCIA_APPARTENENZA WHERE ID_Calciatore = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore rimozione tutte le fasce per calciatore: " + e.getMessage());
            return 0;
        }
    }

    /** Restituisce gli ID_fascia associati al calciatore */
    public List<Integer> trovaFasceIdPerCalciatore(int idCalciatore) {
        final String sql = "SELECT ID_Fascia FROM FASCIA_APPARTENENZA WHERE ID_Calciatore = ? ORDER BY ID_Fascia";
        List<Integer> out = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Errore lettura fasce per calciatore: " + e.getMessage());
        }
        return out;
    }

    /** Restituisce gli oggetti FasciaGiocatore associati al calciatore (join) */
    public List<FasciaGiocatore> trovaFascePerCalciatore(int idCalciatore) {
        final String sql = """
            SELECT f.*
            FROM FASCIA_GIOCATORE f
            JOIN FASCIA_APPARTENENZA fa ON fa.ID_Fascia = f.ID_Fascia
            WHERE fa.ID_Calciatore = ?
            ORDER BY f.Nome_fascia
        """;
        List<FasciaGiocatore> out = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    out.add(f);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore join fasce per calciatore: " + e.getMessage());
        }
        return out;
    }

    /** Restituisce gli ID_calciatore associati a una fascia */
    public List<Integer> trovaCalciatoriIdPerFascia(int idFascia) {
        final String sql = "SELECT ID_Calciatore FROM FASCIA_APPARTENENZA WHERE ID_Fascia = ? ORDER BY ID_Calciatore";
        List<Integer> out = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFascia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Errore lettura calciatori per fascia: " + e.getMessage());
        }
        return out;
    }

    /** Verifica se esiste l'appartenenza */
    public boolean esisteAssegnazione(int idCalciatore, int idFascia) {
        final String sql = "SELECT 1 FROM FASCIA_APPARTENENZA WHERE ID_Calciatore = ? AND ID_Fascia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            ps.setInt(2, idFascia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Errore exists assegnazione: " + e.getMessage());
            return false;
        }
    }

    /** Conta i calciatori assegnati a una fascia */
    public int contaCalciatoriInFascia(int idFascia) {
        final String sql = "SELECT COUNT(*) FROM FASCIA_APPARTENENZA WHERE ID_Fascia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFascia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Errore count calciatori in fascia: " + e.getMessage());
        }
        return 0;
    }

    public boolean assegnaFasciaUnica(int idCalciatore, int idFascia) {
        rimuoviTutteLeFasceDelCalciatore(idCalciatore);
        return assegnaFascia(idCalciatore, idFascia);
    }

    // FasciaAppartenenzaDAO.java  (aggiungi in classe)
    public Optional<FasciaGiocatore> trovaFasciaPerCalciatore(int idCalciatore) {
        final String sql = """
            SELECT f.*
            FROM FASCIA_GIOCATORE f
            JOIN FASCIA_APPARTENENZA fa ON fa.ID_Fascia = f.ID_Fascia
            WHERE fa.ID_Calciatore = ?
            LIMIT 1
        """;
        try (Connection conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
                    return Optional.of(f);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore trovaFasciaPerCalciatore: " + e.getMessage());
        }
        return Optional.empty();
    }


    /** Ritorna tutte le coppie (ID_Calciatore, ID_Fascia) */
    public List<FasciaAppartenenza> trovaTutte() {
        final String sql = "SELECT * FROM FASCIA_APPARTENENZA ORDER BY ID_Fascia, ID_Calciatore";
        List<FasciaAppartenenza> out = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                out.add(new FasciaAppartenenza(
                    rs.getInt("ID_Calciatore"),
                    rs.getInt("ID_Fascia")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore find all FASCIA_APPARTENENZA: " + e.getMessage());
        }
        return out;
    }
}

