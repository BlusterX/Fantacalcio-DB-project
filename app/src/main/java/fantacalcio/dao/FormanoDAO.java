package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import fantacalcio.model.Formano;
import fantacalcio.model.Formano.PanchinaFlag;
import fantacalcio.util.DatabaseConnection;

public class FormanoDAO {
    private final DatabaseConnection dbConnection;

    public FormanoDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /** Aggiunge un calciatore alla formazione con flag panchina. */
    public boolean aggiungi(int idFormazione, int idCalciatore, PanchinaFlag panchina) {
        final String sql = "INSERT INTO FORMANO (ID_Calciatore, ID_Formazione, Panchina) VALUES (?,?,?)";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            ps.setInt(2, idFormazione);
            ps.setString(3, panchina.name()); // 'SI'/'NO'
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException dup) {
            // già presente: aggiorna solo il flag panchina
            return aggiornaPanchina(idFormazione, idCalciatore, panchina);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Aggiorna il flag panchina per un calciatore già in formazione. */
    public boolean aggiornaPanchina(int idFormazione, int idCalciatore, PanchinaFlag panchina) {
        final String sql = "UPDATE FORMANO SET Panchina=? WHERE ID_Formazione=? AND ID_Calciatore=?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, panchina.name());
            ps.setInt(2, idFormazione);
            ps.setInt(3, idCalciatore);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Rimuove un calciatore dalla formazione. */
    public boolean rimuovi(int idFormazione, int idCalciatore) {
        final String sql = "DELETE FROM FORMANO WHERE ID_Formazione=? AND ID_Calciatore=?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idFormazione);
            ps.setInt(2, idCalciatore);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Lista completa (titolari+panchina) per una formazione. */
    public List<Formano> findByFormazione(int idFormazione) {
        final String sql = "SELECT ID_Formazione, ID_Calciatore, Panchina FROM FORMANO WHERE ID_Formazione=?";
        List<Formano> out = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idFormazione);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Formano(
                        rs.getInt("ID_Formazione"),
                        rs.getInt("ID_Calciatore"),
                        PanchinaFlag.valueOf(rs.getString("Panchina"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /** Solo titolari. */
    public List<Integer> findTitolariIds(int idFormazione) {
        final String sql = "SELECT ID_Calciatore FROM FORMANO WHERE ID_Formazione=? AND Panchina='NO'";
        List<Integer> out = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idFormazione);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /** Solo panchina. */
    public List<Integer> findPanchinaIds(int idFormazione) {
        final String sql = "SELECT ID_Calciatore FROM FORMANO WHERE ID_Formazione=? AND Panchina='SI'";
        List<Integer> out = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idFormazione);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }
}
