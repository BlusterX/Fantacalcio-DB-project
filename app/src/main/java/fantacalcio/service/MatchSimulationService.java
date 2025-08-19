package fantacalcio.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import fantacalcio.dao.FormazioneDAO;
import fantacalcio.dao.ScontroLegaDAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.ScontroLega;
import fantacalcio.util.DatabaseConnection;

/**
 * Service per simulare le partite basandosi sulle fasce dei giocatori
 */
public class MatchSimulationService {
    
    private final ScontroLegaDAO scontroDAO;
    private final FormazioneDAO formazioneDAO;
    private final Random random;
    
    public MatchSimulationService() {
        this.scontroDAO = new ScontroLegaDAO();
        this.formazioneDAO = new FormazioneDAO();
        this.random = new Random();
    }
    
    /**
     * Simula tutti gli scontri di una giornata
     */
    public void simulaGiornata(int idLega, int numeroGiornata) {
        List<ScontroLega> scontri = scontroDAO.trovaSccontriGiornata(idLega, numeroGiornata);
        
        for (ScontroLega scontro : scontri) {
            if (scontro.getStato() == ScontroLega.StatoScontro.PROGRAMMATO) {
                simulaScontro(scontro, numeroGiornata);
            }
        }
        
        // Avanza alla prossima giornata se tutti gli scontri sono completati
        if (tuttiScontriCompletati(scontri)) {
            scontroDAO.avanzaGiornata(idLega);
        }
    }
    
    /**
     * Simula un singolo scontro
     */
    private void simulaScontro(ScontroLega scontro, int numeroGiornata) {
        try {
            // Calcola punteggi per entrambe le squadre
            double punteggio1 = calcolaPunteggioSquadra(scontro.getIdSquadra1(), numeroGiornata);
            double punteggio2 = calcolaPunteggioSquadra(scontro.getIdSquadra2(), numeroGiornata);
            
            // Aggiorna risultato
            scontroDAO.aggiornaRisultato(scontro.getIdScontro(), punteggio1, punteggio2);
            
            System.out.println("Scontro simulato: " + scontro.getNomeSquadra1() + " " + 
                             punteggio1 + " - " + punteggio2 + " " + scontro.getNomeSquadra2());
            
        } catch (Exception e) {
            System.err.println("Errore simulazione scontro: " + e.getMessage());
        }
    }
    
    /**
     * Calcola il punteggio di una squadra basandosi sulla formazione e le fasce
     */
    private double calcolaPunteggioSquadra(int idSquadra, int numeroGiornata) {
        // Trova la formazione per questa giornata
        Integer idFormazione = formazioneDAO.getFormazioneSquadraGiornata(idSquadra, numeroGiornata);
        
        if (idFormazione == null) {
            System.out.println("Nessuna formazione trovata per squadra " + idSquadra + 
                             " giornata " + numeroGiornata + " - punteggio 0");
            return 0.0; // Nessuna formazione = 0 punti
        }
        
        // Prendi solo i titolari
        List<Calciatore> titolari = formazioneDAO.trovaTitolari(idFormazione);
        
        if (titolari.size() != 11) {
            System.out.println("Formazione incompleta per squadra " + idSquadra + 
                             " (solo " + titolari.size() + " titolari) - punteggio ridotto");
            return 30.0; // Formazione incompleta = punteggio molto basso
        }
        
        double punteggioTotale = 0.0;
        
        for (Calciatore calciatore : titolari) {
            double punteggioCalciatore = simulaPrestazioneCalciatore(calciatore);
            punteggioTotale += punteggioCalciatore;
        }
        
        return Math.round(punteggioTotale * 10.0) / 10.0; // Arrotonda a 1 decimale
    }
    
    /**
     * Simula la prestazione di un singolo calciatore basandosi sulla sua fascia
     */
    private double simulaPrestazioneCalciatore(Calciatore calciatore) {
        // Voto base tra 4.5 e 8.5
        double votoBase = 4.5 + (random.nextDouble() * 4.0);
        
        // Bonus/malus basati sulla fascia
        ProbabilitaFascia prob = getProbabilitaPerFascia(calciatore.getIdFascia());
        if (prob == null) {
            return votoBase; // Fascia non trovata
        }
        
        double fantavoto = votoBase;
        
        // Simula eventi basati sul ruolo e fascia
        double probGol = getProbabilitaGolPerRuolo(calciatore.getRuolo(), prob);
        
        // Gol (+3 punti)
        if (random.nextDouble() < probGol) {
            fantavoto += 3.0;
        }
        
        // Assist (+1 punto)
        if (random.nextDouble() < prob.probAssist) {
            fantavoto += 1.0;
        }
        
        // Imbattibilità per portieri (+1 punto)
        if (calciatore.getRuolo() == Calciatore.Ruolo.PORTIERE && 
            random.nextDouble() < prob.probImbattibilita) {
            fantavoto += 1.0;
        }
        
        // Ammonizione (-0.5 punti)
        if (random.nextDouble() < prob.probAmmonizione) {
            fantavoto -= 0.5;
        }
        
        // Espulsione (-3 punti)
        if (random.nextDouble() < prob.probEspulsione) {
            fantavoto -= 3.0;
        }
        
        // Assicurati che il voto non vada sotto 1.0
        return Math.max(1.0, fantavoto);
    }
    
    /**
     * Ottiene le probabilità per una fascia specifica
     */
    private ProbabilitaFascia getProbabilitaPerFascia(int idFascia) {
        String sql = "SELECT * FROM FASCIA_GIOCATORE WHERE ID_Fascia = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idFascia);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ProbabilitaFascia(
                        rs.getDouble("Prob_gol_attaccante"),
                        rs.getDouble("Prob_gol_centrocampista"),
                        rs.getDouble("Prob_gol_difensore"),
                        rs.getDouble("Prob_assist"),
                        rs.getDouble("Prob_ammonizione"),
                        rs.getDouble("Prob_espulsione"),
                        rs.getDouble("Prob_imbattibilita")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore lettura fascia: " + e.getMessage());
        }
        
        // Valori di default se non trova la fascia
        return new ProbabilitaFascia(0.15, 0.08, 0.03, 0.12, 0.20, 0.02, 0.25);
    }
    
    /**
     * Calcola la probabilità di gol basata sul ruolo
     */
    private double getProbabilitaGolPerRuolo(Calciatore.Ruolo ruolo, ProbabilitaFascia prob) {
        return switch (ruolo) {
            case ATTACCANTE -> prob.probGolAttaccante;
            case CENTROCAMPISTA -> prob.probGolCentrocampista;
            case DIFENSORE -> prob.probGolDifensore;
            case PORTIERE -> 0.0; // I portieri non segnano
        };
    }
    
    /**
     * Verifica se tutti gli scontri di una lista sono completati
     */
    private boolean tuttiScontriCompletati(List<ScontroLega> scontri) {
        return scontri.stream().allMatch(s -> s.getStato() == ScontroLega.StatoScontro.COMPLETATO);
    }
    
    /**
     * Classe helper per le probabilità delle fasce
     */
    private static class ProbabilitaFascia {
        final double probGolAttaccante;
        final double probGolCentrocampista;
        final double probGolDifensore;
        final double probAssist;
        final double probAmmonizione;
        final double probEspulsione;
        final double probImbattibilita;
        
        ProbabilitaFascia(double probGolAttaccante, double probGolCentrocampista, 
                         double probGolDifensore, double probAssist, double probAmmonizione, 
                         double probEspulsione, double probImbattibilita) {
            this.probGolAttaccante = probGolAttaccante;
            this.probGolCentrocampista = probGolCentrocampista;
            this.probGolDifensore = probGolDifensore;
            this.probAssist = probAssist;
            this.probAmmonizione = probAmmonizione;
            this.probEspulsione = probEspulsione;
            this.probImbattibilita = probImbattibilita;
        }
    }
}