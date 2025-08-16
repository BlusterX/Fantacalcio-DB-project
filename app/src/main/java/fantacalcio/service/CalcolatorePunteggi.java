package fantacalcio.service;

import java.util.List;
import java.util.Map;

import fantacalcio.model.Calciatore;
import fantacalcio.model.Formazione;

public class CalcolatorePunteggi {
    
    private final PunteggioDAO punteggioDAO;
    private final VotoBaseDAO votoBaseDAO;
    private final FormazioneDAO formazioneDAO;
    
    public CalcolatorePunteggi() {
        this.punteggioDAO = new PunteggioDAO();
        this.votoBaseDAO = new VotoBaseDAO();
        this.formazioneDAO = new FormazioneDAO();
    }
    
    /**
     * Calcola il fantavoto di un calciatore per una giornata
     */
    public double calcolaFantavotoCalciatore(int idCalciatore, int numeroGiornata) {
        // Prendi voto base
        double votoBase = votoBaseDAO.getVotoBase(idCalciatore, numeroGiornata);
        if (votoBase == 0) return 0; // Non ha giocato
        
        // Prendi tutti i bonus/malus dalla tabella PUNTEGGIO
        Map<String, Integer> bonusMalus = punteggioDAO.getBonusMalusCalciatore(idCalciatore, numeroGiornata);
        
        double fantavoto = votoBase;
        
        // Applica bonus/malus
        for (Map.Entry<String, Integer> entry : bonusMalus.entrySet()) {
            String tipo = entry.getKey();
            int quantita = entry.getValue();
            double valore = punteggioDAO.getValoreBonusMalus(tipo);
            
            fantavoto += (valore * quantita);
        }
        
        return fantavoto;
    }
    
    /**
     * Calcola il punteggio totale di una formazione
     */
    public double calcolaPunteggioFormazione(int idFormazione) {
        Formazione formazione = formazioneDAO.getFormazioneById(idFormazione);
        if (formazione == null) return 0;
        
        double punteggioTotale = 0;
        List<Calciatore> titolari = formazione.getTitolari();
        List<Calciatore> panchinari = formazione.getPanchinari();
        
        // Calcola punteggio titolari con gestione sostituzioni
        for (Calciatore titolare : titolari) {
            double voto = calcolaFantavotoCalciatore(titolare.getIdCalciatore(), formazione.getNumeroGiornata());
            
            // Se non ha giocato (voto = 0), cerca sostituto
            if (voto == 0 && titolare.isInfortunato()) {
                Calciatore sostituto = trovaSostituto(titolare, panchinari, formazione.getNumeroGiornata());
                if (sostituto != null) {
                    voto = calcolaFantavotoCalciatore(sostituto.getIdCalciatore(), formazione.getNumeroGiornata());
                    System.out.println("Sostituzione: " + titolare.getNomeCompleto() + " -> " + sostituto.getNomeCompleto());
                }
            }
            
            punteggioTotale += voto;
        }
        
        // Aggiungi bonus difesa se il portiere ha preso >= 6
        double bonusDifesa = calcolaBonusDifesa(titolari, formazione.getNumeroGiornata());
        punteggioTotale += bonusDifesa;
        
        return punteggioTotale;
    }
    
    /**
     * Calcola il bonus difesa (modificatore)
     */
    private double calcolaBonusDifesa(List<Calciatore> titolari, int numeroGiornata) {
        // Trova il portiere
        Calciatore portiere = titolari.stream()
            .filter(c -> c.getRuolo() == Calciatore.Ruolo.PORTIERE)
            .findFirst()
            .orElse(null);
            
        if (portiere == null) return 0;
        
        double votoPortiere = votoBaseDAO.getVotoBase(portiere.getIdCalciatore(), numeroGiornata);
        
        // Se il portiere ha preso almeno 6 e non ha subito gol (imbattibilità)
        Map<String, Integer> bonusPortiere = punteggioDAO.getBonusMalusCalciatore(
            portiere.getIdCalciatore(), numeroGiornata
        );
        
        boolean imbattibilita = bonusPortiere.containsKey("IMBATTIBILITA");
        
        if (votoPortiere >= 6.0 && imbattibilita) {
            return 1.0 + ((votoPortiere - 6.0) * 0.5);
        }
        
        return 0;
    }
    
    /**
     * Trova un sostituto dalla panchina
     */
    private Calciatore trovaSostituto(Calciatore titolare, List<Calciatore> panchinari, int numeroGiornata) {
        // Cerca il primo panchinaro dello stesso ruolo che ha giocato
        for (Calciatore panchinaro : panchinari) {
            if (panchinaro.getRuolo() == titolare.getRuolo()) {
                double voto = votoBaseDAO.getVotoBase(panchinaro.getIdCalciatore(), numeroGiornata);
                if (voto > 0) { // Ha giocato
                    return panchinaro;
                }
            }
        }
        
        // Se non c'è nessuno dello stesso ruolo, prendi il primo che ha giocato
        for (Calciatore panchinaro : panchinari) {
            double voto = votoBaseDAO.getVotoBase(panchinaro.getIdCalciatore(), numeroGiornata);
            if (voto > 0) {
                return panchinaro;
            }
        }
        
        return null;
    }
    
    /**
     * Calcola il risultato di uno scontro
     */
    public String calcolaRisultatoScontro(int idScontro) {
        // Recupera le due squadre dello scontro
        ScontroDAO scontroDAO = new ScontroDAO();
        int[] squadre = scontroDAO.getSquadreScontro(idScontro);
        int numeroGiornata = scontroDAO.getGiornataScontro(idScontro);
        
        if (squadre == null || squadre.length != 2) return "0-0";
        
        // Calcola punteggi delle due formazioni
        int idFormazione1 = formazioneDAO.getFormazioneSquadraGiornata(squadre[0], numeroGiornata);
        int idFormazione2 = formazioneDAO.getFormazioneSquadraGiornata(squadre[1], numeroGiornata);
        
        double punteggio1 = calcolaPunteggioFormazione(idFormazione1);
        double punteggio2 = calcolaPunteggioFormazione(idFormazione2);
        
        // Aggiorna risultato nello scontro
        String risultato = String.format("%.1f-%.1f", punteggio1, punteggio2);
        scontroDAO.aggiornaRisultato(idScontro, risultato);
        
        return risultato;
    }
}