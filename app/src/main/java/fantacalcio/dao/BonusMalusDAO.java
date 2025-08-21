package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.BonusMalus;
import fantacalcio.util.DatabaseConnection;

/**
 * DAO per tabella BONUS_MALUS
 * Colonne: ID_Bonus_Malus (PK), Tipologia, Punteggio
 */
public class BonusMalusDAO {

    private final DatabaseConnection db;

    public BonusMalusDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    /** INSERT */
    public boolean inserisci(BonusMalus bm) {
        final String sql = "INSERT INTO BONUS_MALUS (Tipologia, Punteggio) VALUES (?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bm.getTipologia());
            ps.setDouble(2, bm.getPunteggio());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) bm.setIdBonusMalus(gk.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore inserimento BONUS_MALUS: " + e.getMessage());
        }
        return false;
    }

    /** UPDATE by ID */
    public boolean aggiorna(BonusMalus bm) {
        final String sql = "UPDATE BONUS_MALUS SET Tipologia = ?, Punteggio = ? WHERE ID_Bonus_Malus = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bm.getTipologia());
            ps.setDouble(2, bm.getPunteggio());
            ps.setInt(3, bm.getIdBonusMalus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore update BONUS_MALUS: " + e.getMessage());
            return false;
        }
    }

    /** DELETE by ID */
    public boolean elimina(int idBonusMalus) {
        final String sql = "DELETE FROM BONUS_MALUS WHERE ID_Bonus_Malus = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBonusMalus);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // NB: se referenziato da PUNTEGGIO, la FK bloccherà la delete
            System.err.println("Errore delete BONUS_MALUS: " + e.getMessage());
            return false;
        }
    }

    /** SELECT by ID */
    public Optional<BonusMalus> trovaPerId(int idBonusMalus) {
        final String sql = "SELECT * FROM BONUS_MALUS WHERE ID_Bonus_Malus = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBonusMalus);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore findById BONUS_MALUS: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** SELECT by Tipologia (univoca se imposti UNIQUE a DB; qui ritorna il primo match) */
    public Optional<BonusMalus> trovaPerTipologia(String tipologia) {
        final String sql = "SELECT * FROM BONUS_MALUS WHERE Tipologia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipologia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore findByTipologia BONUS_MALUS: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** SELECT * */
    public List<BonusMalus> trovaTutti() {
        final String sql = "SELECT * FROM BONUS_MALUS ORDER BY Tipologia";
        List<BonusMalus> out = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Errore findAll BONUS_MALUS: " + e.getMessage());
        }
        return out;
    }

    /** Esiste tipologia? (utile prima di inserire) */
    public boolean esisteTipologia(String tipologia) {
        final String sql = "SELECT 1 FROM BONUS_MALUS WHERE Tipologia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipologia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Errore exists tipologia BONUS_MALUS: " + e.getMessage());
            return false;
        }
    }

    /** Inserimento massivo semplice */
    public int inserisciBulk(List<BonusMalus> list) {
        final String sql = "INSERT INTO BONUS_MALUS (Tipologia, Punteggio) VALUES (?, ?)";
        int count = 0;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (BonusMalus bm : list) {
                ps.setString(1, bm.getTipologia());
                ps.setDouble(2, bm.getPunteggio());
                ps.addBatch();
            }
            int[] res = ps.executeBatch();
            for (int r : res) if (r >= 0) count += r;
        } catch (SQLException e) {
            System.err.println("Errore bulk insert BONUS_MALUS: " + e.getMessage());
        }
        return count;
    }

    private BonusMalus mapRow(ResultSet rs) throws SQLException {
        return new BonusMalus(
            rs.getInt("ID_Bonus_Malus"),
            rs.getString("Tipologia"),
            rs.getDouble("Punteggio")
        );
        // Schema DB: ID_Bonus_Malus, Tipologia, Punteggio
    }

    public int seedDefaults() {
        Object[][] defaults = new Object[][]{
            {"Gol", 3.00},
            {"Assist", 1.00},
            {"Ammonizione", -0.50},
            {"Espulsione", -1.00},
            {"Rigore Sbagliato", -3.00},
            {"Autogol", -2.00},
            {"Porta Imbattuta", 1.00}
        };

        final String checkSql = "SELECT 1 FROM BONUS_MALUS WHERE Tipologia = ?";
        final String insSql   = "INSERT INTO BONUS_MALUS (Tipologia, Punteggio) VALUES (?, ?)";

        int inserted = 0;
        try (Connection conn = db.getConnection();
            PreparedStatement chk = conn.prepareStatement(checkSql);
            PreparedStatement ins = conn.prepareStatement(insSql)) {

            for (Object[] row : defaults) {
                String tip = (String) row[0];
                double pun = (double) row[1];

                chk.setString(1, tip);
                try (ResultSet rs = chk.executeQuery()) {
                    if (!rs.next()) {
                        ins.setString(1, tip);
                        ins.setDouble(2, pun);
                        inserted += ins.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore seed BONUS_MALUS: " + e.getMessage());
        }
        return inserted;
    }
}