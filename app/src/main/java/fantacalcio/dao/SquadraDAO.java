package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.Squadra;
import fantacalcio.util.DatabaseConnection;

/**
 * Data Access Object per la gestione delle squadre (tabella SQUADRA)
 */
public class SquadraDAO {

    private final DatabaseConnection dbConnection;

    public SquadraDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inserisce una nuova squadra
     */
    public boolean inserisciSquadra(Squadra squadra) {
        String sql = "INSERT INTO SQUADRA (Nome, ID_Campionato, ID_Calciatore) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, squadra.getNome());
            stmt.setInt(2, squadra.getIdCampionato());
            stmt.setInt(3, squadra.getIdCalciatore());

            int righeInserite = stmt.executeUpdate();

            if (righeInserite > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        squadra.setIdSquadra(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Squadra inserita: " + squadra.getNome());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore inserimento squadra: " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("duplicate")) {
                System.err.println("   Squadra già esistente!");
            }
        }
        return false;
    }

    /**
     * Trova una squadra per ID
     */
    public Optional<Squadra> trovaSquadraPerId(int idSquadra) {
        String sql = "SELECT * FROM SQUADRA WHERE ID_Squadra = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSquadra);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaSquadraDaResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore ricerca squadra per ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Trova una squadra per nome
     */
    public Optional<Squadra> trovaSquadraPerNome(String nome) {
        String sql = "SELECT * FROM SQUADRA WHERE Nome = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaSquadraDaResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore ricerca squadra per nome: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Recupera tutte le squadre
     */
    public List<Squadra> trovaTutteLeSquadre() {
        List<Squadra> squadre = new ArrayList<>();
        String sql = "SELECT * FROM SQUADRA ORDER BY Nome";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                squadre.add(creaSquadraDaResultSet(rs));
            }
            System.out.println("Trovate " + squadre.size() + " squadre");

        } catch (SQLException e) {
            System.err.println("Errore recupero squadre: " + e.getMessage());
        }
        return squadre;
    }

    /**
     * Aggiorna una squadra esistente
     */
    public boolean aggiornaSquadra(Squadra squadra) {
        String sql = "UPDATE SQUADRA SET Nome = ?, ID_Campionato = ?, ID_Calciatore = ? WHERE ID_Squadra = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, squadra.getNome());
            stmt.setInt(2, squadra.getIdCampionato());
            stmt.setInt(3, squadra.getIdCalciatore());
            stmt.setInt(4, squadra.getIdSquadra());

            int righeAggiornate = stmt.executeUpdate();

            if (righeAggiornate > 0) {
                System.out.println("Squadra aggiornata: " + squadra.getNome());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore aggiornamento squadra: " + e.getMessage());
        }
        return false;
    }

    /**
     * Elimina una squadra (attenzione: FK su CALCIATORE)
     */
    public boolean eliminaSquadra(int idSquadra) {
        String sql = "DELETE FROM SQUADRA WHERE ID_Squadra = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSquadra);

            int righeEliminate = stmt.executeUpdate();

            if (righeEliminate > 0) {
                System.out.println("Squadra eliminata (ID: " + idSquadra + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore eliminazione squadra: " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("foreign key")) {
                System.err.println("   Impossibile eliminare: ci sono calciatori associati!");
            }
        }
        return false;
    }

    /**
     * Conta il numero di squadre
     */
    public int contaSquadre() {
        String sql = "SELECT COUNT(*) FROM SQUADRA";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Errore conteggio squadre: " + e.getMessage());
        }
        return 0;
    }

    public List<Squadra> trovaSquadrePerCampionato(int idCampionato) {
        List<Squadra> out = new ArrayList<>();
        String sql = "SELECT * FROM SQUADRA WHERE ID_Campionato = ? ORDER BY Nome";
        try (Connection c = dbConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCampionato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(creaSquadraDaResultSet(rs));
            }
        } catch (SQLException e) { System.err.println("trovaSquadrePerCampionato: " + e.getMessage()); }
        return out;
    }

    /**
     * Helper per creare un oggetto Squadra da un ResultSet
     */
    private Squadra creaSquadraDaResultSet(ResultSet rs) throws SQLException {
        return new Squadra(
            rs.getInt("ID_Squadra"),
            rs.getString("Nome"),
            rs.getInt("ID_Campionato"),
            rs.getInt("ID_Calciatore")
        );
    }
}
