package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import fantacalcio.util.DatabaseConnection;

public class GiornataDAO {
    private final DatabaseConnection db;

    public GiornataDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public boolean exists(int numero) {
        final String sql = "SELECT 1 FROM GIORNATA WHERE Numero = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, numero);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            System.err.println("GiornataDAO.exists: " + e.getMessage());
            return false;
        }
    }

    public boolean insert(int numero) {
        final String sql = "INSERT INTO GIORNATA (Numero) VALUES (?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, numero);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // se già esiste (PK), per noi va bene
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) return true;
            System.err.println("GiornataDAO.insert: " + e.getMessage());
            return false;
        }
    }

    /** Popola 1..38 (idempotente) */
    public void ensureGiornate38() {
        int ok = 0;
        for (int i = 1; i <= 38; i++) {
            if (exists(i) || insert(i)) ok++;
        }
        System.out.println("GiornataDAO.ensureGiornate38 -> pronte " + ok + "/38");
    }
}
