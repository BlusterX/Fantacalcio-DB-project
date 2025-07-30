package fantacalcio.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.Utente;
import fantacalcio.util.DatabaseConnection;

public class UtenteDAO {
    private final DatabaseConnection dbConnection;
    public UtenteDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    /**
     * Inserisce un nuovo utente nel database
     */
    public boolean inserisciUtente(Utente utente) {
        String sql = """
            INSERT INTO UTENTE (Nome, Cognome, Nickname, Email, Numero, Data_di_nascita, Password) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getCognome());
            stmt.setString(3, utente.getNickname());
            stmt.setString(4, utente.getEmail());
            stmt.setString(5, utente.getNumero());
            stmt.setDate(6, Date.valueOf(utente.getDataDiNascita()));
            stmt.setString(7, utente.getPassword());
            
            int righeInserite = stmt.executeUpdate();
            
            if (righeInserite > 0) {
                // Recupera l'ID generato automaticamente
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        utente.setIdUtente(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Utente inserito: " + utente.getNickname());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore inserimento utente: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains("nickname")) {
                    System.err.println(" Nickname già esistente!");
                } else if (e.getMessage().contains("email")) {
                    System.err.println(" Email già esistente!");
                }
            }
        }
        return false;
    }
    
    /**
     * Trova un utente per ID
     */
    public Optional<Utente> trovaUtentePerId(int idUtente) {
        String sql = "SELECT * FROM UTENTE WHERE ID_Utente = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUtente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaUtenteDaResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca utente per ID: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Trova un utente per nickname
     */
    public Optional<Utente> trovaUtentPerNickname(String nickname) {
        String sql = "SELECT * FROM UTENTE WHERE Nickname = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nickname);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaUtenteDaResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca utente per nickname: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Trova un utente per email
     */
    public Optional<Utente> trovaUtentPerEmail(String email) {
        String sql = "SELECT * FROM UTENTE WHERE Email = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaUtenteDaResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca utente per email: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Recupera tutti gli utenti
     */
    public List<Utente> trovaTuttiGliUtenti() {
        List<Utente> utenti = new ArrayList<>();
        String sql = "SELECT * FROM UTENTE ORDER BY Nickname";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                utenti.add(creaUtenteDaResultSet(rs));
            }
            
            System.out.println("Trovati " + utenti.size() + " utenti");
            
        } catch (SQLException e) {
            System.err.println("Errore recupero tutti gli utenti: " + e.getMessage());
        }
        return utenti;
    }
    
    /**
     * Aggiorna un utente esistente
     */
    public boolean aggiornaUtente(Utente utente) {
        String sql = """
            UPDATE UTENTE 
            SET Nome = ?, Cognome = ?, Nickname = ?, Email = ?, 
                Numero = ?, Data_di_nascita = ?, Password = ?
            WHERE ID_Utente = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getCognome());
            stmt.setString(3, utente.getNickname());
            stmt.setString(4, utente.getEmail());
            stmt.setString(5, utente.getNumero());
            stmt.setDate(6, Date.valueOf(utente.getDataDiNascita()));
            stmt.setString(7, utente.getPassword());
            stmt.setInt(8, utente.getIdUtente());
            
            int righeAggiornate = stmt.executeUpdate();
            
            if (righeAggiornate > 0) {
                System.out.println("Utente aggiornato: " + utente.getNickname());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento utente: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Elimina un utente (e cascading le sue squadre)
     */
    public boolean eliminaUtente(int idUtente) {
        String sql = "DELETE FROM UTENTE WHERE ID_Utente = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUtente);
            
            int righeEliminate = stmt.executeUpdate();
            
            if (righeEliminate > 0) {
                System.out.println("Utente eliminato (ID: " + idUtente + ")");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore eliminazione utente: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Verifica login utente
     */
    public Optional<Utente> verificaLogin(String nickname, String password) {
        String sql = "SELECT * FROM UTENTE WHERE Nickname = ? AND Password = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nickname);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Login successful per: " + nickname);
                    return Optional.of(creaUtenteDaResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore verifica login: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Conta il numero totale di utenti
     */
    public int contaUtenti() {
        String sql = "SELECT COUNT(*) FROM UTENTE";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Errore conteggio utenti: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Metodo helper per creare un oggetto Utente da un ResultSet
     */
    private Utente creaUtenteDaResultSet(ResultSet rs) throws SQLException {
        return new Utente(
            rs.getInt("ID_Utente"),
            rs.getString("Nome"),
            rs.getString("Cognome"),
            rs.getString("Nickname"),
            rs.getString("Email"),
            rs.getString("Numero"),
            rs.getDate("Data_di_nascita").toLocalDate(),
            rs.getString("Password")
        );
    }
}