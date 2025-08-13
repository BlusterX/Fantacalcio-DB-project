package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.util.DatabaseConnection;

public class SquadraFantacalcioDAO {
    
    private final DatabaseConnection dbConnection;
    private final CalciatoreDAO calciatoreDAO;
    
    public SquadraFantacalcioDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.calciatoreDAO = new CalciatoreDAO();
    }
    
    /**
     * Crea una nuova squadra fantacalcio
     */
    public boolean creaSquadra(SquadraFantacalcio squadra) {
        String sql = """
            INSERT INTO SQUADRA_FANTACALCIO 
            (Nome_squadra, ID_Utente, Budget_totale, Budget_rimanente, Data_creazione, Data_ultima_modifica, Completata) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, squadra.getNomeSquadra());
            stmt.setInt(2, squadra.getIdUtente());
            stmt.setInt(3, squadra.getBudgetTotale());
            stmt.setInt(4, squadra.getBudgetRimanente());
            stmt.setTimestamp(5, Timestamp.valueOf(squadra.getDataCreazione()));
            stmt.setTimestamp(6, Timestamp.valueOf(squadra.getDataUltimaModifica()));
            stmt.setBoolean(7, squadra.isCompletata());
            
            int righeInserite = stmt.executeUpdate();
            
            if (righeInserite > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        squadra.setIdSquadraFantacalcio(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Squadra fantacalcio creata: " + squadra.getNomeSquadra());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore creazione squadra fantacalcio: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("   Nome squadra già esistente per questo utente!");
            }
        }
        return false;
    }
    
    /**
     * Trova una squadra per ID
     */
    public Optional<SquadraFantacalcio> trovaSquadraPerId(int idSquadra) {
        String sql = "SELECT * FROM SQUADRA_FANTACALCIO WHERE ID_SquadraFantacalcio = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSquadra);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    SquadraFantacalcio squadra = creaSquadraDaResultSet(rs);
                    
                    // Carica anche i calciatori
                    caricaCalciatori(squadra);
                    
                    return Optional.of(squadra);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca squadra per ID: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Trova tutte le squadre di un utente
     */
    public List<SquadraFantacalcio> trovaSquadrePerUtente(int idUtente) {
        List<SquadraFantacalcio> squadre = new ArrayList<>();
        String sql = "SELECT * FROM SQUADRA_FANTACALCIO WHERE ID_Utente = ? ORDER BY Data_creazione DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUtente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SquadraFantacalcio squadra = creaSquadraDaResultSet(rs);
                    
                    // Carica anche i calciatori per ogni squadra
                    caricaCalciatori(squadra);
                    
                    squadre.add(squadra);
                }
            }
            
            System.out.println("Trovate " + squadre.size() + " squadre per utente ID " + idUtente);
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca squadre per utente: " + e.getMessage());
        }
        return squadre;
    }
    
    /**
     * Aggiorna una squadra esistente
     */
    public boolean aggiornaSquadra(SquadraFantacalcio squadra) {
        String sql = """
            UPDATE SQUADRA_FANTACALCIO 
            SET Nome_squadra = ?, Budget_rimanente = ?, Data_ultima_modifica = ?, Completata = ?
            WHERE ID_SquadraFantacalcio = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, squadra.getNomeSquadra());
            stmt.setInt(2, squadra.getBudgetRimanente());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(4, squadra.isCompletata());
            stmt.setInt(5, squadra.getIdSquadraFantacalcio());
            
            int righeAggiornate = stmt.executeUpdate();
            
            if (righeAggiornate > 0) {
                System.out.println("Squadra aggiornata: " + squadra.getNomeSquadra());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento squadra: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Elimina una squadra e tutte le sue composizioni
     */
    public boolean eliminaSquadra(int idSquadra) {
        String sql = "DELETE FROM SQUADRA_FANTACALCIO WHERE ID_SquadraFantacalcio = ?";
        
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
        }
        return false;
    }
    
    /**
     * Aggiunge un calciatore alla squadra
     */
    public boolean aggiungiCalciatoreAllaSquadra(int idSquadra, int idCalciatore) {
        String sql = "INSERT INTO COMPOSIZIONE_SQUADRA (ID_SquadraFantacalcio, ID_Calciatore) VALUES (?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSquadra);
            stmt.setInt(2, idCalciatore);
            
            int righeInserite = stmt.executeUpdate();
            
            if (righeInserite > 0) {
                System.out.println("Calciatore aggiunto alla squadra (ID squadra: " + idSquadra + ", ID calciatore: " + idCalciatore + ")");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore aggiunta calciatore alla squadra: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("   Calciatore già presente nella squadra!");
            }
        }
        return false;
    }
    
    /**
     * Rimuove un calciatore dalla squadra
     */
    public boolean rimuoviCalciatoreDallaSquadra(int idSquadra, int idCalciatore) {
        String sql = "DELETE FROM COMPOSIZIONE_SQUADRA WHERE ID_SquadraFantacalcio = ? AND ID_Calciatore = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSquadra);
            stmt.setInt(2, idCalciatore);
            
            int righeEliminate = stmt.executeUpdate();
            
            if (righeEliminate > 0) {
                System.out.println("Calciatore rimosso dalla squadra (ID squadra: " + idSquadra + ", ID calciatore: " + idCalciatore + ")");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore rimozione calciatore dalla squadra: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Verifica se un nome squadra è già utilizzato da un utente
     */
    public boolean nomeSquadraEsiste(String nomeSquadra, int idUtente) {
        String sql = "SELECT COUNT(*) FROM SQUADRA_FANTACALCIO WHERE Nome_squadra = ? AND ID_Utente = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nomeSquadra);
            stmt.setInt(2, idUtente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore verifica nome squadra: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Conta il numero di squadre create da un utente
     */
    public int contaSquadreUtente(int idUtente) {
        String sql = "SELECT COUNT(*) FROM SQUADRA_FANTACALCIO WHERE ID_Utente = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUtente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore conteggio squadre utente: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Carica i calciatori di una squadra dal database
     */
    private void caricaCalciatori(SquadraFantacalcio squadra) {
        String sql = """
            SELECT c.* FROM CALCIATORI c
            JOIN COMPOSIZIONE_SQUADRA cs ON c.ID_Calciatore = cs.ID_Calciatore
            WHERE cs.ID_SquadraFantacalcio = ?
            ORDER BY c.Ruolo, c.Cognome, c.Nome
            """;
        
        List<Calciatore> calciatori = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, squadra.getIdSquadraFantacalcio());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Calciatore calciatore = new Calciatore(
                        rs.getInt("ID_Calciatore"),
                        rs.getString("Nome"),
                        rs.getString("Cognome"),
                        Calciatore.Ruolo.valueOf(rs.getString("Ruolo")),
                        rs.getInt("Costo")
                    );
                    calciatori.add(calciatore);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore caricamento calciatori squadra: " + e.getMessage());
        }
        
        squadra.setCalciatori(calciatori);
    }
    
    /**
     * Trova squadre complete (pronte per giocare)
     */
    public List<SquadraFantacalcio> trovaSquadreComplete() {
        List<SquadraFantacalcio> squadre = new ArrayList<>();
        String sql = "SELECT * FROM SQUADRA_FANTACALCIO WHERE Completata = true ORDER BY Data_creazione DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                SquadraFantacalcio squadra = creaSquadraDaResultSet(rs);
                caricaCalciatori(squadra);
                squadre.add(squadra);
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca squadre complete: " + e.getMessage());
        }
        return squadre;
    }
    
    /**
     * Metodo helper per creare un oggetto SquadraFantacalcio da un ResultSet
     */
    private SquadraFantacalcio creaSquadraDaResultSet(ResultSet rs) throws SQLException {
        return new SquadraFantacalcio(
            rs.getInt("ID_SquadraFantacalcio"),
            rs.getString("Nome_squadra"),
            rs.getInt("ID_Utente"),
            rs.getInt("Budget_totale"),
            rs.getInt("Budget_rimanente"),
            rs.getTimestamp("Data_creazione").toLocalDateTime(),
            rs.getTimestamp("Data_ultima_modifica").toLocalDateTime(),
            rs.getBoolean("Completata")
        );
    }
}