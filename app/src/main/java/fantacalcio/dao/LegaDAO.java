package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.Lega;
import fantacalcio.util.DatabaseConnection;

/**
 * Data Access Object per la gestione delle leghe
 */
public class LegaDAO {
    
    private final DatabaseConnection dbConnection;
    
    public LegaDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Crea una nuova lega
     */
    public boolean creaLega(Lega lega) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Inserisci la lega
            String sqlLega = "INSERT INTO LEGA (Nome, Codice_accesso) VALUES (?, ?)";
            
            try (PreparedStatement stmtLega = conn.prepareStatement(sqlLega, Statement.RETURN_GENERATED_KEYS)) {
                stmtLega.setString(1, lega.getNome());
                stmtLega.setString(2, lega.getCodiceAccesso());
                
                int righeInserite = stmtLega.executeUpdate();
                
                if (righeInserite > 0) {
                    try (ResultSet generatedKeys = stmtLega.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            lega.setIdLega(generatedKeys.getInt(1));
                        }
                    }
                    
                    // 2. Inserisci la relazione ADMIN
                    String sqlAdmin = "INSERT INTO ADMIN (ID_Utente, ID_Lega) VALUES (?, ?)";
                    
                    try (PreparedStatement stmtAdmin = conn.prepareStatement(sqlAdmin)) {
                        stmtAdmin.setInt(1, lega.getIdAdmin());
                        stmtAdmin.setInt(2, lega.getIdLega());
                        stmtAdmin.executeUpdate();
                    }
                    
                    conn.commit();
                    System.out.println("Lega creata: " + lega.getNome() + " (Codice: " + lega.getCodiceAccesso() + ")");
                    return true;
                }
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Errore rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Errore creazione lega: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("   Nome lega già esistente!");
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Errore ripristino autocommit: " + ex.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Trova una lega per codice di accesso
     */
    public Optional<Lega> trovaLegaPerCodice(String codiceAccesso) {
        String sql = "SELECT * FROM LEGA WHERE Codice_accesso = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codiceAccesso);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaLegaDaResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca lega per codice: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Trova tutte le leghe di un admin
     */
    public List<Lega> trovaLeghePerAdmin(int idAdmin) {
        List<Lega> leghe = new ArrayList<>();
        String sql = """
            SELECT l.* FROM LEGA l
            JOIN ADMIN a ON l.ID_Lega = a.ID_Lega
            WHERE a.ID_Utente = ?
            ORDER BY l.Nome
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idAdmin);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Lega lega = creaLegaDaResultSet(rs);
                    lega.setIdAdmin(idAdmin); // Impostiamo manualmente l'ID admin
                    leghe.add(lega);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca leghe per admin: " + e.getMessage());
        }
        return leghe;
    }
    
    /**
     * Trova le leghe a cui partecipa un utente
     */
    public List<Lega> trovaLeghePerUtente(int idUtente) {
        List<Lega> leghe = new ArrayList<>();
        String sql = """
            SELECT l.* FROM LEGA l
            JOIN PARTECIPA p ON l.ID_Lega = p.ID_Lega
            JOIN SQUADRA_FANTACALCIO sf ON p.ID_Squadra = sf.ID_Squadra
            WHERE sf.ID_Utente = ?
            ORDER BY l.Nome
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUtente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leghe.add(creaLegaDaResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca leghe per utente: " + e.getMessage());
        }
        return leghe;
    }
    
    /**
     * Fa partecipare una squadra a una lega
     */
    public boolean partecipaLega(int idSquadra, int idLega) {
        String sql = "INSERT INTO PARTECIPA (ID_Squadra, ID_Lega) VALUES (?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSquadra);
            stmt.setInt(2, idLega);
            
            int righeInserite = stmt.executeUpdate();
            
            if (righeInserite > 0) {
                System.out.println("Squadra " + idSquadra + " aggiunta alla lega " + idLega);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore partecipazione lega: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("   Squadra già partecipa a questa lega!");
            }
        }
        return false;
    }
    
    /**
     * Verifica se una squadra partecipa già a una lega
     */
    public boolean squadraPartecipaLega(int idSquadra, int idLega) {
        String sql = "SELECT COUNT(*) FROM PARTECIPA WHERE ID_Squadra = ? AND ID_Lega = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSquadra);
            stmt.setInt(2, idLega);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore verifica partecipazione: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Conta il numero di partecipanti a una lega
     */
    public int contaPartecipanti(int idLega) {
        String sql = "SELECT COUNT(*) FROM PARTECIPA WHERE ID_Lega = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore conteggio partecipanti: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Elimina una lega (solo se è l'admin)
     */
    public boolean eliminaLega(int idLega, int idAdmin) {
        String sql = """
            DELETE l FROM LEGA l
            JOIN ADMIN a ON l.ID_Lega = a.ID_Lega
            WHERE l.ID_Lega = ? AND a.ID_Utente = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            stmt.setInt(2, idAdmin);
            
            int righeEliminate = stmt.executeUpdate();
            
            if (righeEliminate > 0) {
                System.out.println("Lega eliminata (ID: " + idLega + ")");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore eliminazione lega: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Aggiorna una lega
     */
    public boolean aggiornaLega(Lega lega) {
        String sql = """
            UPDATE LEGA l
            JOIN ADMIN a ON l.ID_Lega = a.ID_Lega
            SET l.Nome = ?
            WHERE l.ID_Lega = ? AND a.ID_Utente = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, lega.getNome());
            stmt.setInt(2, lega.getIdLega());
            stmt.setInt(3, lega.getIdAdmin());
            
            int righeAggiornate = stmt.executeUpdate();
            
            if (righeAggiornate > 0) {
                System.out.println("Lega aggiornata: " + lega.getNome());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento lega: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Verifica se un nome lega esiste già per un admin
     */
    public boolean nomeLegaEsiste(String nomeLega, int idAdmin) {
        String sql = """
            SELECT COUNT(*) FROM LEGA l
            JOIN ADMIN a ON l.ID_Lega = a.ID_Lega
            WHERE l.Nome = ? AND a.ID_Utente = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nomeLega);
            stmt.setInt(2, idAdmin);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore verifica nome lega: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Metodo helper per creare un oggetto Lega da un ResultSet
     */
    private Lega creaLegaDaResultSet(ResultSet rs) throws SQLException {
        return new Lega(
            rs.getInt("ID_Lega"),
            rs.getString("Nome"),
            rs.getString("Codice_accesso"),
            0 // L'ID admin verrà impostato separatamente se necessario
        );
    }
}