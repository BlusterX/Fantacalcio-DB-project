package fantacalcio.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Classe per gestire la connessione al database MySQL
 * Implementa il pattern Singleton per garantire una sola connessione
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Parametri di connessione (saranno letti da file di configurazione)
    private String url;
    private String username;
    private String password;
    private String driver;
    
    /**
     * Costruttore privato per implementare Singleton
     */
    private DatabaseConnection() {
        try {
            loadDatabaseProperties();
            establishConnection();
        } catch (SQLException | IOException e) {
            System.err.println("Errore durante l'inizializzazione della connessione al database:");
        }
    }
    
    /**
     * Metodo per ottenere l'istanza singleton
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Carica le proprietà del database dal file di configurazione
     */
    private void loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("database.properties")) {
            
            if (input == null) {
                // Se il file non esiste, usa valori di default
                System.out.println("File database.properties non trovato. Uso valori di default.");
                setDefaultProperties();
                return;
            }
            
            props.load(input);
            
            // Leggi le proprietà
            this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3307/fantacalcio");
            this.username = props.getProperty("db.username", "root");
            this.password = props.getProperty("db.password", "");
            this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            
        }
    }
    
    /**
     * Imposta valori di default per la connessione
     */
    private void setDefaultProperties() {
        this.url = "jdbc:mysql://localhost:3307/fantacalcio?useSSL=false&serverTimezone=UTC";
        this.username = "root";
        this.password = ""; 
        this.driver = "com.mysql.cj.jdbc.Driver";
    }
    
    /**
     * Stabilisce la connessione al database
     */
    private void establishConnection() throws SQLException {
        try {
            // Carica il driver JDBC
            Class.forName(driver);
            
            // Crea la connessione
            this.connection = DriverManager.getConnection(url, username, password);
            
            System.out.println("Connessione al database stabilita con successo!");
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL non trovato: " + driver, e);
        }
    }
    
    /**
     * Restituisce la connessione al database
     */
    public Connection getConnection() throws SQLException {
        // Verifica se la connessione è ancora valida
        if (connection == null || connection.isClosed()) {
            establishConnection();
        }
        return connection;
    }
    
    /**
     * Chiude la connessione al database
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connessione al database chiusa.");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la chiusura della connessione:");
        }
    }
    
    /**
     * Testa la connessione al database
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Test connessione fallito: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Metodo per ottenere informazioni sulla connessione
     */
    public String getConnectionInfo() {
        try {
            if (connection != null && !connection.isClosed()) {
                return String.format("Connesso a: %s come utente: %s", 
                    connection.getMetaData().getURL(),
                    connection.getMetaData().getUserName());
            }
        } catch (SQLException e) {
            return "Errore nel recuperare informazioni sulla connessione: " + e.getMessage();
        }
        return "Nessuna connessione attiva";
    }
}