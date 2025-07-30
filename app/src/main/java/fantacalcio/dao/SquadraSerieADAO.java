package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.SquadraSerieA;
import fantacalcio.util.DatabaseConnection;

/**
 * Data Access Object per la gestione delle squadre di Serie A
 */
public class SquadraSerieADAO {
    
    private final DatabaseConnection dbConnection;
    
    public SquadraSerieADAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Inserisce una nuova squadra Serie A
     */
    public boolean inserisciSquadra(SquadraSerieA squadra) {
        String sql = "INSERT INTO SQUADRA_SERIE_A (Nome) VALUES (?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, squadra.getNome());
            
            int righeInserite = stmt.executeUpdate();
            
            if (righeInserite > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        squadra.setIdSquadraA(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Squadra Serie A inserita: " + squadra.getNome());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore inserimento squadra Serie A: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("   Squadra già esistente!");
            }
        }
        return false;
    }
    
    /**
     * Trova una squadra per ID
     */
    public Optional<SquadraSerieA> trovaSquadraPerId(int idSquadra) {
        String sql = "SELECT * FROM SQUADRA_SERIE_A WHERE ID_SquadraA = ?";
        
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
    public Optional<SquadraSerieA> trovaSquadraPerNome(String nome) {
        String sql = "SELECT * FROM SQUADRA_SERIE_A WHERE Nome = ?";
        
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
     * Recupera tutte le squadre Serie A
     */
    public List<SquadraSerieA> trovaTutteLeSquadre() {
        List<SquadraSerieA> squadre = new ArrayList<>();
        String sql = "SELECT * FROM SQUADRA_SERIE_A ORDER BY Nome";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                squadre.add(creaSquadraDaResultSet(rs));
            }
            
            System.out.println("Trovate " + squadre.size() + " squadre Serie A");
            
        } catch (SQLException e) {
            System.err.println("Errore recupero squadre Serie A: " + e.getMessage());
        }
        return squadre;
    }
    
    /**
     * Aggiorna una squadra esistente
     */
    public boolean aggiornaSquadra(SquadraSerieA squadra) {
        String sql = "UPDATE SQUADRA_SERIE_A SET Nome = ? WHERE ID_SquadraA = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, squadra.getNome());
            stmt.setInt(2, squadra.getIdSquadraA());
            
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
     * Elimina una squadra (attenzione: elimina anche i suoi calciatori!)
     */
    public boolean eliminaSquadra(int idSquadra) {
        String sql = "DELETE FROM SQUADRA_SERIE_A WHERE ID_SquadraA = ?";
        
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
            if (e.getMessage().contains("foreign key constraint")) {
                System.err.println("   Impossibile eliminare: ci sono calciatori associati!");
            }
        }
        return false;
    }
    
    /**
     * Conta il numero di squadre
     */
    public int contaSquadre() {
        String sql = "SELECT COUNT(*) FROM SQUADRA_SERIE_A";
        
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
    
    /**
     * Popola il database con le squadre di Serie A 2024/25
     */
    public void popolaSquadreSerieA() {
        String[] squadreSerieA = {
            "AC Milan", "Inter", "Juventus", "Napoli", "AS Roma", "Lazio", 
            "Atalanta", "Fiorentina", "Bologna", "Torino", "Udinese", 
            "Sassuolo", "Empoli", "Verona", "Salernitana", "Genoa", 
            "Cagliari", "Frosinone", "Lecce", "Monza"
        };
        
        System.out.println("Popolamento squadre Serie A...");
        int inserite = 0;
        
        for (String nomeSquadra : squadreSerieA) {
            SquadraSerieA squadra = new SquadraSerieA(nomeSquadra);
            if (inserisciSquadra(squadra)) {
                inserite++;
            }
        }
        
        System.out.println("Popolamento completato: " + inserite + "/" + squadreSerieA.length + " squadre inserite");
    }
    
    /**
     * Metodo helper per creare un oggetto SquadraSerieA da un ResultSet
     */
    private SquadraSerieA creaSquadraDaResultSet(ResultSet rs) throws SQLException {
        return new SquadraSerieA(
            rs.getInt("ID_SquadraA"),
            rs.getString("Nome")
        );
    }
}