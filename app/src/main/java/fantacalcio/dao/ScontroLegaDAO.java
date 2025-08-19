package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fantacalcio.model.ScontroLega;
import fantacalcio.service.MatchSimulationService;
import fantacalcio.util.DatabaseConnection; 

public class ScontroLegaDAO {
    
    private final DatabaseConnection dbConnection;
    
    public ScontroLegaDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Genera il calendario per una lega (tutti contro tutti)
     */
    public boolean generaCalendario(int idLega, List<Integer> squadreIds) {
        if (squadreIds.size() % 2 != 0) {
            System.err.println("Numero squadre deve essere pari per generare calendario");
            return false;
        }
        
        if (squadreIds.size() < 2 || squadreIds.size() > 12) {
            System.err.println("Numero squadre deve essere tra 2 e 12");
            return false;
        }
        
        String sql = "INSERT INTO SCONTRO_LEGA (ID_Lega, ID_Squadra_1, ID_Squadra_2, Giornata) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                List<Integer> squadre = new ArrayList<>(squadreIds);
                Collections.shuffle(squadre); // Randomizza l'ordine
                
                int numSquadre = squadre.size();
                int numGiornate = numSquadre - 1;
                
                for (int giornata = 1; giornata <= numGiornate; giornata++) {
                    List<Integer> squadreGiornata = new ArrayList<>(squadre);
                    
                    // Algoritmo round-robin
                    for (int i = 0; i < numSquadre / 2; i++) {
                        int squadra1 = squadreGiornata.get(i);
                        int squadra2 = squadreGiornata.get(numSquadre - 1 - i);
                        
                        stmt.setInt(1, idLega);
                        stmt.setInt(2, squadra1);
                        stmt.setInt(3, squadra2);
                        stmt.setInt(4, giornata);
                        stmt.addBatch();
                    }
                    
                    // Ruota le squadre (tranne la prima)
                    if (giornata < numGiornate) {
                        Integer ultima = squadre.remove(squadre.size() - 1);
                        squadre.add(1, ultima);
                    }
                }
                
                stmt.executeBatch();
                conn.commit();
                
                System.out.println("Calendario generato: " + numGiornate + " giornate per " + numSquadre + " squadre");
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore generazione calendario: " + e.getMessage());
            return false;
        }
    }

    public boolean avviaLegaManualmente(int idLega) {
        // Verifica prerequisiti
        if (!verificaAvvioLega(idLega)) {
            return false;
        }
        
        // Genera calendario completo (tutte le 38 giornate se necessario)
        List<Integer> squadreIds = getSquadreCompleteLega(idLega);
        if (!generaCalendarioCompleto(idLega, squadreIds)) {
            return false;
        }
        
        // Avvia la lega
        return avviaLega(idLega);
    }

    /**
     * Genera calendario completo per tutte le giornate
     */
    private boolean generaCalendarioCompleto(int idLega, List<Integer> squadreIds) {
        if (squadreIds.size() % 2 != 0 || squadreIds.size() < 2) {
            return false;
        }
        
        String sql = "INSERT INTO SCONTRO_LEGA (ID_Lega, ID_Squadra_1, ID_Squadra_2, Giornata) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int numSquadre = squadreIds.size();
                int numGiornate = (numSquadre - 1) * 2; // Andata e ritorno
                
                // Andata
                for (int giornata = 1; giornata <= numSquadre - 1; giornata++) {
                    generaPartiteGiornata(stmt, idLega, squadreIds, giornata);
                }
                
                // Ritorno (squadre invertite)
                for (int giornata = numSquadre; giornata <= numGiornate; giornata++) {
                    int giornataAndata = giornata - (numSquadre - 1);
                    generaPartiteGiornataInvertite(stmt, idLega, squadreIds, giornata, giornataAndata);
                }
                
                stmt.executeBatch();
                conn.commit();
                
                System.out.println("Calendario completo generato: " + numGiornate + " giornate");
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore generazione calendario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera le partite per una singola giornata (andata)
     */
    private void generaPartiteGiornata(PreparedStatement stmt, int idLega, List<Integer> squadreIds, int giornata) throws SQLException {
        List<Integer> squadre = new ArrayList<>(squadreIds);
        Collections.shuffle(squadre); // Randomizza per variare gli accoppiamenti
        
        int numSquadre = squadre.size();
        
        // Algoritmo round-robin per generare gli accoppiamenti
        for (int i = 0; i < numSquadre / 2; i++) {
            int squadra1 = squadre.get(i);
            int squadra2 = squadre.get(numSquadre - 1 - i);
            
            stmt.setInt(1, idLega);
            stmt.setInt(2, squadra1);
            stmt.setInt(3, squadra2);
            stmt.setInt(4, giornata);
            stmt.addBatch();
        }
        
        // Ruota le squadre per la prossima giornata (tranne la prima)
        if (giornata < numSquadre - 1) {
            Integer ultima = squadre.remove(squadre.size() - 1);
            squadre.add(1, ultima);
        }
    }

    /**
     * Genera le partite per una giornata di ritorno (squadre invertite)
     */
    private void generaPartiteGiornataInvertite(PreparedStatement stmt, int idLega, List<Integer> squadreIds, int giornata, int giornataAndata) throws SQLException {
        // Ricrea lo stesso ordine dell'andata per questa giornata specifica
        List<Integer> squadre = new ArrayList<>(squadreIds);
        
        // Applica le stesse rotazioni dell'andata per ottenere gli stessi accoppiamenti
        for (int g = 1; g < giornataAndata; g++) {
            Integer ultima = squadre.remove(squadre.size() - 1);
            squadre.add(1, ultima);
        }
        
        int numSquadre = squadre.size();
        
        // Genera gli accoppiamenti con squadre invertite (ritorno)
        for (int i = 0; i < numSquadre / 2; i++) {
            int squadra1 = squadre.get(numSquadre - 1 - i); // Invertito rispetto all'andata
            int squadra2 = squadre.get(i);                   // Invertito rispetto all'andata
            
            stmt.setInt(1, idLega);
            stmt.setInt(2, squadra1);
            stmt.setInt(3, squadra2);
            stmt.setInt(4, giornata);
            stmt.addBatch();
        }
    }

    /**
     * Ottiene le squadre complete di una lega
     */
    public List<Integer> getSquadreCompleteLega(int idLega) {
        List<Integer> squadreIds = new ArrayList<>();
        String sql = """
            SELECT s.ID_Squadra 
            FROM SQUADRA_FANTACALCIO s
            JOIN PARTECIPA p ON s.ID_Squadra = p.ID_Squadra
            WHERE p.ID_Lega = ? AND s.Completata = true
            """;
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    squadreIds.add(rs.getInt("ID_Squadra"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore caricamento squadre complete: " + e.getMessage());
        }
        
        return squadreIds;
    }

    /**
     * Simula tutte le giornate in sequenza
     */
    public void simulaTutteLeGiornate(int idLega) {
        String statoLega = getStatoLega(idLega);
        if (!"IN_CORSO".equals(statoLega)) {
            System.out.println("La lega non è in corso");
            return;
        }
        
        MatchSimulationService simulator = new MatchSimulationService();
        int giornataCorrente = getGiornataCorrente(idLega);
        int maxGiornate = getMaxGiornate(idLega);
        
        // Simula dalla giornata corrente fino alla fine
        for (int giornata = giornataCorrente; giornata <= maxGiornate; giornata++) {
            simulator.simulaGiornata(idLega, giornata);
            aggiornaGiornataCorrente(idLega, giornata + 1);
            
            System.out.println("Simulata giornata " + giornata + "/" + maxGiornate);
        }
        
        // Termina la lega
        terminaLega(idLega);
    }

    private int getMaxGiornate(int idLega) {
        String sql = "SELECT MAX(Giornata) FROM SCONTRO_LEGA WHERE ID_Lega = ?";
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLega);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 38;
            }
        } catch (SQLException e) {
            return 38; // Default
        }
    }
        
    private void aggiornaGiornataCorrente(int idLega, int nuovaGiornata) {
        String sql = "UPDATE LEGA SET Giornata_Corrente = ? WHERE ID_Lega = ?";
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuovaGiornata);
            stmt.setInt(2, idLega);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento giornata: " + e.getMessage());
        }
    }

    private void terminaLega(int idLega) {
        String sql = "UPDATE LEGA SET Stato = 'TERMINATA' WHERE ID_Lega = ?";
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLega);
            stmt.executeUpdate();
            System.out.println("Lega " + idLega + " terminata");
        } catch (SQLException e) {
            System.err.println("Errore terminazione lega: " + e.getMessage());
        }
    }
    /**
     * Avvia la lega (imposta stato e data inizio)
     */
    public boolean avviaLega(int idLega) {
        String updateLegaSql = "UPDATE LEGA SET Stato = 'IN_CORSO', Data_Inizio = ? WHERE ID_Lega = ?";
        String updateScontroSql = "UPDATE SCONTRO_LEGA SET Data_Inizio = ?, Stato = 'PROGRAMMATO' " +
                                  "WHERE ID_Lega = ? AND Giornata = 1";
        
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime primaPartita = now.plusMinutes(1); // Prima partita tra 1 minuto
            
            try {
                // Aggiorna stato lega
                try (PreparedStatement stmt1 = conn.prepareStatement(updateLegaSql)) {
                    stmt1.setObject(1, now);
                    stmt1.setInt(2, idLega);
                    stmt1.executeUpdate();
                }
                
                // Imposta orario prima giornata
                try (PreparedStatement stmt2 = conn.prepareStatement(updateScontroSql)) {
                    stmt2.setObject(1, primaPartita);
                    stmt2.setInt(2, idLega);
                    stmt2.executeUpdate();
                }
                
                conn.commit();
                System.out.println("Lega avviata! Prima partita: " + primaPartita);
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore avvio lega: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Trova scontri per giornata
     */
    public List<ScontroLega> trovaSccontriGiornata(int idLega, int giornata) {
        List<ScontroLega> scontri = new ArrayList<>();
        String sql = """
            SELECT s.*, 
                   sq1.Nome as Nome_Squadra_1, 
                   sq2.Nome as Nome_Squadra_2
            FROM SCONTRO_LEGA s
            JOIN SQUADRA_FANTACALCIO sq1 ON s.ID_Squadra_1 = sq1.ID_Squadra
            JOIN SQUADRA_FANTACALCIO sq2 ON s.ID_Squadra_2 = sq2.ID_Squadra
            WHERE s.ID_Lega = ? AND s.Giornata = ?
            ORDER BY s.ID_Scontro
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            stmt.setInt(2, giornata);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ScontroLega scontro = new ScontroLega();
                    scontro.setIdScontro(rs.getInt("ID_Scontro"));
                    scontro.setIdLega(rs.getInt("ID_Lega"));
                    scontro.setIdSquadra1(rs.getInt("ID_Squadra_1"));
                    scontro.setIdSquadra2(rs.getInt("ID_Squadra_2"));
                    scontro.setGiornata(rs.getInt("Giornata"));
                    scontro.setPunteggio1(rs.getDouble("Punteggio_1"));
                    scontro.setPunteggio2(rs.getDouble("Punteggio_2"));
                    scontro.setStato(ScontroLega.StatoScontro.valueOf(rs.getString("Stato")));
                    
                    // Data può essere null
                    if (rs.getTimestamp("Data_Inizio") != null) {
                        scontro.setDataInizio(rs.getTimestamp("Data_Inizio").toLocalDateTime());
                    }
                    
                    scontro.setNomeSquadra1(rs.getString("Nome_Squadra_1"));
                    scontro.setNomeSquadra2(rs.getString("Nome_Squadra_2"));
                    
                    scontri.add(scontro);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore ricerca scontri giornata: " + e.getMessage());
        }
        
        return scontri;
    }
    
    /**
     * Aggiorna risultato scontro
     */
    public boolean aggiornaRisultato(int idScontro, double punteggio1, double punteggio2) {
        String sql = "UPDATE SCONTRO_LEGA SET Punteggio_1 = ?, Punteggio_2 = ?, Stato = 'COMPLETATO' " +
                     "WHERE ID_Scontro = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, punteggio1);
            stmt.setDouble(2, punteggio2);
            stmt.setInt(3, idScontro);
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Risultato aggiornato: " + punteggio1 + " - " + punteggio2);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento risultato: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Verifica se la lega può essere avviata
     */
    public boolean verificaAvvioLega(int idLega) {
        String sql = """
            SELECT COUNT(DISTINCT p.ID_Squadra) as num_squadre
            FROM PARTECIPA p
            JOIN SQUADRA_FANTACALCIO s ON p.ID_Squadra = s.ID_Squadra
            WHERE p.ID_Lega = ? AND s.Completata = true
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int numSquadre = rs.getInt("num_squadre");
                    return numSquadre >= 2 && numSquadre <= 12 && numSquadre % 2 == 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore verifica avvio lega: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Ottieni stato della lega
     */
    public String getStatoLega(int idLega) {
        String sql = "SELECT Stato FROM LEGA WHERE ID_Lega = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Stato");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore lettura stato lega: " + e.getMessage());
        }
        
        return "CREATA";
    }
    
    /**
     * Ottieni giornata corrente
     */
    public int getGiornataCorrente(int idLega) {
        String sql = "SELECT Giornata_Corrente FROM LEGA WHERE ID_Lega = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Giornata_Corrente");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore lettura giornata corrente: " + e.getMessage());
        }
        
        return 1;
    }
    
    /**
     * Avanza alla prossima giornata
     */
    public boolean avanzaGiornata(int idLega) {
        String sql = """
            UPDATE LEGA 
            SET Giornata_Corrente = Giornata_Corrente + 1 
            WHERE ID_Lega = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("Lega " + idLega + " avanzata alla prossima giornata");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore avanzamento giornata: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Programma la prossima giornata
     */
    public boolean programmaProssimaGiornata(int idLega, LocalDateTime dataInizio) {
        int prossimaGiornata = getGiornataCorrente(idLega);
        
        String sql = """
            UPDATE SCONTRO_LEGA 
            SET Data_Inizio = ?, Stato = 'PROGRAMMATO' 
            WHERE ID_Lega = ? AND Giornata = ? AND Stato = 'PROGRAMMATO'
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, dataInizio);
            stmt.setInt(2, idLega);
            stmt.setInt(3, prossimaGiornata);
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Prossima giornata programmata per: " + dataInizio);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Errore programmazione prossima giornata: " + e.getMessage());
        }
        
        return false;
    }
}