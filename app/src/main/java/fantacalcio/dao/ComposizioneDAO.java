package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import fantacalcio.util.DatabaseConnection;

public class ComposizioneDAO {
    private final DatabaseConnection db;

    public ComposizioneDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    /** Inserisce un calciatore nel roster della squadra fantacalcio. */
    public boolean aggiungi(int idCalciatore, int idSquadraFantacalcio) {
        final String sql = "INSERT INTO COMPOSIZIONE (ID_Calciatore, ID_Squadra_Fantacalcio) VALUES (?, ?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            ps.setInt(2, idSquadraFantacalcio);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException dup) {
            // già presente (PK composta): consideralo ok/idempotente
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Rimuove il legame calciatore–squadra fantacalcio. */
    public boolean rimuovi(int idCalciatore, int idSquadraFantacalcio) {
        final String sql = "DELETE FROM COMPOSIZIONE WHERE ID_Calciatore=? AND ID_Squadra_Fantacalcio=?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            ps.setInt(2, idSquadraFantacalcio);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Elenco dei calciatori della squadra fantacalcio. */
    public List<Integer> findCalciatoriBySquadraFantacalcio(int idSquadraFantacalcio) {
        final String sql = "SELECT ID_Calciatore FROM COMPOSIZIONE WHERE ID_Squadra_Fantacalcio=?";
        List<Integer> out = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idSquadraFantacalcio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /** Conta quanti giocatori ha in rosa una squadra fantacalcio. */
    public int countBySquadraFantacalcio(int idSquadraFantacalcio) {
        final String sql = "SELECT COUNT(*) FROM COMPOSIZIONE WHERE ID_Squadra_Fantacalcio=?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idSquadraFantacalcio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
