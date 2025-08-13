package fantacalcio.service;

import java.util.List;

import fantacalcio.model.Calciatore;
import fantacalcio.model.Calciatore.Ruolo;

/**
 * ALGORITMO DI SIMULAZIONE GIORNATA
 * Usa il tuo schema esistente senza modifiche strutturali
 */
public class SimulatoreGiornata {
    
    /**
     * Simula una giornata completa
     */
    public void simulaGiornata(int numeroGiornata) {
        
        // 1. SIMULA LE PARTITE SERIE A
        List<PartitaSerieA> partite = creaPartiteGiornata(numeroGiornata);
        for (PartitaSerieA partita : partite) {
            simulaPartitaSerieA(partita);
        }
        
        // 2. GENERA VOTI PER OGNI CALCIATORE
        List<Calciatore> calciatori = calciatoreDAO.trovaTuttiICalciatori();
        for (Calciatore calciatore : calciatori) {
            if (calciatore.isDisponibile()) { // Non infortunato/squalificato
                generaVotoCalciatore(calciatore, numeroGiornata);
            }
        }
        
        // 3. CALCOLA PUNTEGGI SQUADRE FANTACALCIO
        List<Squadra> squadreFantacalcio = squadraDAO.trovaTutteLeSquadre();
        for (Squadra squadra : squadreFantacalcio) {
            calcolaPunteggioSquadra(squadra, numeroGiornata);
        }
    }
    
    /**
     * Genera il voto di un singolo calciatore per la giornata
     */
    private void generaVotoCalciatore(Calciatore calciatore, int numeroGiornata) {
        FasciaBravura fascia = calciatore.getFascia();
        
        // 1. VOTO BASE (4.5-8.5) * moltiplicatore fascia
        double votoBase = 6.0 + (Math.random() * 2.5 - 1.0); // 5.0-8.5
        votoBase *= fascia.getMoltiplicatoreVoto();
        votoBase = Math.max(4.5, Math.min(10.0, votoBase)); // Clamp 4.5-10.0
        
        // 2. EVENTI STATISTICI basati su probabilità fascia
        int gol = simulaEvento(fascia.getProbGolPerRuolo(calciatore.getRuolo()));
        int assist = simulaEvento(fascia.getProbAssist());
        int ammonizioni = simulaEvento(fascia.getProbAmmonizione());
        int espulsioni = simulaEvento(fascia.getProbEspulsione());
        int rigoriParati = 0;
        
        if (calciatore.getRuolo() == Ruolo.PORTIERE) {
            rigoriParati = simulaEvento(fascia.getProbRigoreParato());
        }
        
        // 3. SE ESPULSO → voto molto basso
        if (espulsioni > 0) {
            votoBase = 4.0 + Math.random(); // 4.0-5.0
        }
        
        // 4. SALVA NEL DATABASE (nella tua tabella VOTO_GIORNATA)
        VotoGiornata voto = new VotoGiornata();
        voto.setIdCalciatore(calciatore.getIdCalciatore());
        voto.setNumeroGiornata(numeroGiornata);
        voto.setVotoBase(votoBase);
        voto.setGol(gol);
        voto.setAssist(assist);
        voto.setAmmonizioni(ammonizioni);
        voto.setEspulsioni(espulsioni);
        voto.setRigoriParati(rigoriParati);
        voto.setMinutiGiocati(90); // Assumiamo tutti giocano 90'
        
        votiDAO.inserisciVoto(voto);
        
        // 5. BONUS/MALUS AGGIUNTIVI (usando il tuo sistema esistente)
        if (gol > 0) {
            aggiungiBonus(calciatore.getIdCalciatore(), numeroGiornata, "GOL", gol);
        }
        if (assist > 0) {
            aggiungiBonus(calciatore.getIdCalciatore(), numeroGiornata, "ASSIST", assist);
        }
        if (ammonizioni > 0) {
            aggiungiMalus(calciatore.getIdCalciatore(), numeroGiornata, "AMMONIZIONE", ammonizioni);
        }
        // ... altri bonus/malus
    }
    
    /**
     * Simula un evento basato su probabilità
     * @param probabilita da 0.0 a 1.0
     * @return numero di volte che l'evento si verifica (di solito 0 o 1)
     */
    private int simulaEvento(double probabilita) {
        return Math.random() < probabilita ? 1 : 0;
    }
    
    /**
     * Aggiunge bonus usando il tuo sistema BONUS_MALUS + PUNTEGGIO
     */
    private void aggiungiBonus(int idCalciatore, int numeroGiornata, String tipoBonus, int quantita) {
        // Trova l'ID del bonus nel tuo sistema
        int idBonus = bonusDAO.trovaIdBonusPerTipo(tipoBonus);
        
        // Inserisci nella tabella PUNTEGGIO
        Punteggio punteggio = new Punteggio();
        punteggio.setIdCalciatore(idCalciatore);
        punteggio.setIdBonusMalus(idBonus);
        punteggio.setNumeroGiornata(numeroGiornata);
        punteggio.setQuantita(quantita);
        
        punteggioDAO.inserisciPunteggio(punteggio);
    }
    
    /**
     * Calcola il punteggio di una squadra fantacalcio per la giornata
     */
    private void calcolaPunteggioSquadra(Squadra squadra, int numeroGiornata) {
        // 1. Trova la formazione schierata per questa giornata
        Formazione formazione = formazioneDAO.trovaFormazionePerGiornata(squadra.getIdSquadra(), numeroGiornata);
        
        if (formazione == null) {
            System.out.println("Squadra " + squadra.getNome() + " non ha schierato formazione per giornata " + numeroGiornata);
            return;
        }
        
        // 2. Calcola punteggio sommando i fantavoti dei titolari
        List<Calciatore> titolari = formazioneDAO.trovaTitolari(formazione.getIdFormazione());
        double punteggioTotale = 0.0;
        
        for (Calciatore calciatore : titolari) {
            VotoGiornata voto = votiDAO.trovaVotoPer(calciatore.getIdCalciatore(), numeroGiornata);
            if (voto != null) {
                punteggioTotale += voto.getFantavoto();
            } else {
                punteggioTotale += 6.0; // Voto default se non ha giocato
            }
        }
        
        // 3. Bonus portiere se voto >= 6.5
        Calciatore portiere = trovaPor­tiere(titolari);
        if (portiere != null) {
            VotoGiornata votoPortiere = votiDAO.trovaVotoPer(portiere.getIdCalciatore(), numeroGiornata);
            if (votoPortiere != null && votoPortiere.getVotoBase() >= 6.5) {
                punteggioTotale += 1.0;
            }
        }
        
        System.out.println("Squadra " + squadra.getNome() + " - Punteggio giornata " + numeroGiornata + ": " + punteggioTotale);
    }
}
