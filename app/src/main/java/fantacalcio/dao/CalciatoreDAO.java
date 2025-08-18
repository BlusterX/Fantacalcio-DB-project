package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraSerieA;
import fantacalcio.util.DatabaseConnection;

/**
 * Data Access Object per la gestione dei calciatori
 */
public class CalciatoreDAO {
    
    private final DatabaseConnection dbConnection;
    private final SquadraSerieADAO squadraDAO;
    
    public CalciatoreDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.squadraDAO = new SquadraSerieADAO();
    }
    
    /**
     * Inserisce un nuovo calciatore e la sua relazione con la squadra Serie A
     */
    public boolean inserisciCalciatore(Calciatore calciatore, int idSquadraSerieA) {
        String sqlCalciatore = "INSERT INTO CALCIATORE (Nome, Cognome, Ruolo, Costo, ID_Fascia) VALUES (?, ?, ?, ?, ?)";
        String sqlAppartenenza = "INSERT INTO APPARTENENZA_SQUADRA_SERIE_A (ID_Calciatore, ID_SquadraA) VALUES (?, ?)";
        
        try (Connection conn = dbConnection.getNewConnection()) {
            conn.setAutoCommit(false);
            
            // Controlla se esiste già
            if (calciatoreEsiste(calciatore.getNome(), calciatore.getCognome())) {
                System.out.println("Calciatore già esistente: " + calciatore.getNomeCompleto() + " - saltato");
                return false;
            }
            
            try {
                // 1. Inserisci il calciatore
                try (PreparedStatement stmtCalciatore = conn.prepareStatement(sqlCalciatore, Statement.RETURN_GENERATED_KEYS)) {
                    stmtCalciatore.setString(1, calciatore.getNome());
                    stmtCalciatore.setString(2, calciatore.getCognome());
                    stmtCalciatore.setString(3, calciatore.getRuolo().getAbbreviazione());
                    stmtCalciatore.setInt(4, calciatore.getCosto());
                    stmtCalciatore.setInt(5, calciatore.getIdFascia());
                    
                    int righeInserite = stmtCalciatore.executeUpdate();
                    
                    if (righeInserite > 0) {
                        try (ResultSet generatedKeys = stmtCalciatore.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                calciatore.setIdCalciatore(generatedKeys.getInt(1));
                            }
                        }
                        
                        // 2. Inserisci la relazione APPARTENENZA_SQUADRA_SERIE_A
                        try (PreparedStatement stmtAppartenenza = conn.prepareStatement(sqlAppartenenza)) {
                            stmtAppartenenza.setInt(1, calciatore.getIdCalciatore());
                            stmtAppartenenza.setInt(2, idSquadraSerieA);
                            stmtAppartenenza.executeUpdate();
                        }
                        
                        conn.commit();
                        System.out.println("Calciatore inserito: " + calciatore.getNomeCompleto() + " (costo: " + calciatore.getCosto() + ")");
                        return true;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore inserimento calciatore: " + e.getMessage());
        }
        
        return false;
    }

    public boolean calciatoreEsiste(String nome, String cognome) {
        String sql = "SELECT COUNT(*) FROM CALCIATORE WHERE Nome = ? AND Cognome = ?";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // PRIMA imposta i parametri
            stmt.setString(1, nome);
            stmt.setString(2, cognome);
            
            // POI esegui la query
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore controllo esistenza calciatore: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Trova un calciatore per ID
     */
    public Optional<Calciatore> trovaCalciatorePerId(int idCalciatore) {
        String sql = "SELECT * FROM CALCIATORE WHERE ID_Calciatore = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idCalciatore);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaCalciatoreDaResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca calciatore per ID: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Trova tutti i calciatori con le loro squadre Serie A
     */
    public List<CalciatoreConSquadra> trovaTuttiICalciatoriConSquadra() {
        List<CalciatoreConSquadra> risultati = new ArrayList<>();
        String sql = "SELECT c.*, s.ID_SquadraA, s.Nome as NomeSquadra " +
                    "FROM CALCIATORE c " +
                    "LEFT JOIN APPARTENENZA_SQUADRA_SERIE_A ap ON c.ID_Calciatore = ap.ID_Calciatore " +
                    "LEFT JOIN SQUADRA_SERIE_A s ON ap.ID_SquadraA = s.ID_SquadraA " +
                    "ORDER BY s.Nome, c.Cognome, c.Nome";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Calciatore calciatore = creaCalciatoreDaResultSet(rs);
                
                SquadraSerieA squadra = null;
                if (rs.getInt("ID_SquadraA") != 0) {
                    squadra = new SquadraSerieA(
                        rs.getInt("ID_SquadraA"),
                        rs.getString("NomeSquadra")
                    );
                }
                
                risultati.add(new CalciatoreConSquadra(calciatore, squadra));
            }
            
            System.out.println("Trovati " + risultati.size() + " calciatori");
            
        } catch (SQLException e) {
            System.err.println("Errore recupero calciatori con squadra: " + e.getMessage());
        }
        return risultati;
    }
    
    /**
     * Trova calciatori per squadra Serie A
     */
    public List<Calciatore> trovaCalciatoriPerSquadra(int idSquadraSerieA) {
        List<Calciatore> calciatori = new ArrayList<>();
        String sql = "SELECT c.* " +
                    "FROM CALCIATORE c " +
                    "JOIN APPARTENENZA_SQUADRA_SERIE_A ap ON c.ID_Calciatore = ap.ID_Calciatore " +
                    "WHERE ap.ID_SquadraA = ? " +
                    "ORDER BY c.Ruolo, c.Cognome, c.Nome";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSquadraSerieA);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    calciatori.add(creaCalciatoreDaResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca calciatori per squadra: " + e.getMessage());
        }
        return calciatori;
    }
    
    /**
     * Elimina un calciatore
     */
    public boolean eliminaCalciatore(int idCalciatore) {
        String sql = "DELETE FROM CALCIATORE WHERE ID_Calciatore = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idCalciatore);
            
            int righeEliminate = stmt.executeUpdate();
            
            if (righeEliminate > 0) {
                System.out.println("Calciatore eliminato (ID: " + idCalciatore + ")");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore eliminazione calciatore: " + e.getMessage());
        }
        return false;
    }

    public List<Calciatore> trovaTuttiICalciatori() {
    List<Calciatore> calciatori = new ArrayList<>();
    String sql = "SELECT * FROM CALCIATORE";

    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            calciatori.add(creaCalciatoreDaResultSet(rs));
        }
    } catch (SQLException e) {
        System.err.println("Errore recupero calciatori: " + e.getMessage());
    }
        return calciatori;
    }

    
    /**
     * Conta calciatori per ruolo
     */
    public Map<Calciatore.Ruolo, Integer> contaCalciatoriPerRuolo() {
        Map<Calciatore.Ruolo, Integer> conteggi = new HashMap<>();
        String sql = "SELECT Ruolo, COUNT(*) as Conteggio FROM CALCIATORE GROUP BY Ruolo";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String ruoloDb = rs.getString("Ruolo");
                Calciatore.Ruolo ruolo;
                
                // Usa if-else invece di switch expression
                if ("P".equals(ruoloDb)) {
                    ruolo = Calciatore.Ruolo.PORTIERE;
                } else if ("D".equals(ruoloDb)) {
                    ruolo = Calciatore.Ruolo.DIFENSORE;
                } else if ("C".equals(ruoloDb)) {
                    ruolo = Calciatore.Ruolo.CENTROCAMPISTA;
                } else if ("A".equals(ruoloDb)) {
                    ruolo = Calciatore.Ruolo.ATTACCANTE;
                } else {
                    throw new IllegalArgumentException("Ruolo non valido: " + ruoloDb);
                }
                
                int conteggio = rs.getInt("Conteggio");
                conteggi.put(ruolo, conteggio);
            }
            
        } catch (SQLException e) {
            System.err.println("Errore conteggio calciatori per ruolo: " + e.getMessage());
        }
        return conteggi;
    }
    
    /**
     * Metodo helper per creare un oggetto Calciatore da un ResultSet
     */
    private Calciatore creaCalciatoreDaResultSet(ResultSet rs) throws SQLException {
        String ruoloDb = rs.getString("Ruolo");

        Calciatore.Ruolo ruolo;
        // Usa if-else invece di switch expression per compatibilità
        if ("P".equals(ruoloDb)) {
            ruolo = Calciatore.Ruolo.PORTIERE;
        } else if ("D".equals(ruoloDb)) {
            ruolo = Calciatore.Ruolo.DIFENSORE;
        } else if ("C".equals(ruoloDb)) {
            ruolo = Calciatore.Ruolo.CENTROCAMPISTA;
        } else if ("A".equals(ruoloDb)) {
            ruolo = Calciatore.Ruolo.ATTACCANTE;
        } else {
            throw new IllegalArgumentException("Ruolo non valido: " + ruoloDb);
        }

        return new Calciatore(
            rs.getInt("ID_Calciatore"),
            rs.getString("Nome"),
            rs.getString("Cognome"),
            ruolo,
            rs.getInt("Costo")
        );
    }

    
    /**
     * Classe helper per rappresentare un calciatore con la sua squadra
     */
    public static class CalciatoreConSquadra {
        private final Calciatore calciatore;
        private final SquadraSerieA squadra;
        
        public CalciatoreConSquadra(Calciatore calciatore, SquadraSerieA squadra) {
            this.calciatore = calciatore;
            this.squadra = squadra;
        }
        
        public Calciatore getCalciatore() {
            return calciatore;
        }
        
        public SquadraSerieA getSquadra() {
            return squadra;
        }
        
        public String getNomeSquadra() {
            return squadra != null ? squadra.getNome() : "Senza squadra";
        }
    }
}