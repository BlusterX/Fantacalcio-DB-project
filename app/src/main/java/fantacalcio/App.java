package fantacalcio;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fantacalcio.gui.MainFrame;
import fantacalcio.util.DatabaseConnection;

public class App {
    public static void main(String[] args) {
        // Test della connessione al database
        if (!testDatabaseConnection()) {
            JOptionPane.showMessageDialog(null, 
                "Impossibile connettersi al database.\nVerifica la configurazione in database.properties", 
                "Errore Database", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println("Impossibile impostare il Look and Feel del sistema");
        }
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                System.out.println("GUI avviata con successo!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Errore durante l'avvio dell'interfaccia grafica:\n" + e.getMessage(), 
                    "Errore", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    /**
     * Testa la connessione al database
     */
    private static boolean testDatabaseConnection() {
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            if (dbConnection.testConnection()) {
                System.out.println("Database connection successful!");
                System.out.println(dbConnection.getConnectionInfo());
                
                // Test di una query semplice
                Connection conn = dbConnection.getConnection();
                try (var stmt = conn.createStatement(); 
                     var rs = stmt.executeQuery("SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'fantacalcio'")) {
                    if (rs.next()) {
                        int tableCount = rs.getInt("table_count");
                        System.out.println("Trovate " + tableCount + " tabelle nel database 'fantacalcio'");
                    }
                }
                return true;
                
            } else {
                System.err.println("Database connection failed!");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il test della connessione:");
            System.err.println("   " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Errore generico: " + e.getMessage());
            return false;
        }
    }
}