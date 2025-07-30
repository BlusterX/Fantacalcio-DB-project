package fantacalcio;

import java.sql.Connection;
import java.sql.SQLException;

import fantacalcio.util.DatabaseConnection;

public class App {
    public static void main(String[] args) {
        System.out.println(" Welcome to the Fantacalcio application! ");
        
        // Test della connessione al database
        testDatabaseConnection();
        
        // Qui andranno le altre funzionalità dell'applicazione
        System.out.println("\n Applicazione pronta per l'uso!");
    }
    
    /**
     * Testa la connessione al database
     */
    private static void testDatabaseConnection() {
        System.out.println("Testing database connection...");
        
        try {
            // Ottieni l'istanza del DatabaseConnection
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            
            // Testa la connessione
            if (dbConnection.testConnection()) {
                System.out.println("Database connection successful!");
                System.out.println(" " + dbConnection.getConnectionInfo());
                
                // Test di una query semplice
                Connection conn = dbConnection.getConnection();
                try (java.sql.Statement stmt = conn.createStatement(); 
                    java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'fantacalcio'")) {
                    
                    if (rs.next()) {
                        int tableCount = rs.getInt("table_count");
                        System.out.println("Trovate " + tableCount + " tabelle nel database 'fantacalcio'");
                    }
                    
                }
                
            } else {
                System.err.println("Database connection failed!");
            }
            
        } catch (SQLException e) {
            System.err.println(" Errore durante il test della connessione:");
            System.err.println("   " + e.getMessage());
            System.err.println("\n Possibili soluzioni:");
            System.err.println("   1. Verifica che MySQL sia avviato");
            System.err.println("   2. Controlla username/password in database.properties");
            System.err.println("   3. Verifica che il database 'fantacalcio' esista");
        } catch (Exception e) {
            System.err.println(" Errore generico: " + e.getMessage());
            e.printStackTrace();
        }
    }
}