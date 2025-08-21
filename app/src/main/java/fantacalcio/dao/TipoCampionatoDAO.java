package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import fantacalcio.model.TipoCampionato;
import fantacalcio.util.DatabaseConnection;

public class TipoCampionatoDAO {
    private final DatabaseConnection db;

    public TipoCampionatoDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public Optional<TipoCampionato> findById(int id) {
        final String sql = "SELECT * FROM TIPO_CAMPIONATO WHERE ID_Campionato = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("TipoCampionatoDAO.findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<TipoCampionato> findByNomeAnno(String nome, int anno) {
        final String sql = "SELECT * FROM TIPO_CAMPIONATO WHERE Nome = ? AND Anno = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setInt(2, anno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("TipoCampionatoDAO.findByNomeAnno: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Crea se non esiste, altrimenti ritorna quello esistente */
    public TipoCampionato getOrCreate(String nome, int anno) {
        return findByNomeAnno(nome, anno).orElseGet(() -> {
            final String ins = "INSERT INTO TIPO_CAMPIONATO (Nome, Anno) VALUES (?, ?)";
            try (Connection c = db.getConnection();
                 PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nome);
                ps.setInt(2, anno);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        return new TipoCampionato(gk.getInt(1), nome, anno);
                    }
                }
            } catch (SQLException e) {
                System.err.println("TipoCampionatoDAO.getOrCreate: " + e.getMessage());
            }
            // fallback (in caso di race con UNIQUE mancante): riprova a leggere
            return findByNomeAnno(nome, anno).orElseThrow(
                () -> new IllegalStateException("Impossibile creare o leggere TIPO_CAMPIONATO " + nome + " " + anno)
            );
        });
    }

    /** Comodo per il tuo bottone “precarica campionati” */
    public void seedDefaults() {
        getOrCreate("Serie A", 2024);
        getOrCreate("Premier League", 2024);
        System.out.println("Seed TIPO_CAMPIONATO: Serie A 2024, Premier League 2024 ok");
    }

    private TipoCampionato map(ResultSet rs) throws SQLException {
        return new TipoCampionato(
            rs.getInt("ID_Campionato"),
            rs.getString("Nome"),
            rs.getInt("Anno")
        );
    }
}
