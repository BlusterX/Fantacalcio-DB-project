package fantacalcio.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SquadraFantacalcio {
    
    private int idSquadraFantacalcio;
    private String nomeSquadra;
    private int idUtente;
    private int budgetTotale;
    private int budgetRimanente;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataUltimaModifica;
    private boolean completata; // true se ha tutti i giocatori richiesti
    
    // Lista calciatori nella squadra (per uso in memoria)
    private List<Calciatore> calciatori;
    
    // Costanti per regole fantacalcio
    public static final int BUDGET_INIZIALE = 500; // crediti
    public static final int NUM_PORTIERI_RICHIESTI = 3;
    public static final int NUM_DIFENSORI_RICHIESTI = 8;
    public static final int NUM_CENTROCAMPISTI_RICHIESTI = 8;
    public static final int NUM_ATTACCANTI_RICHIESTI = 6;
    public static final int TOTALE_GIOCATORI = NUM_PORTIERI_RICHIESTI + NUM_DIFENSORI_RICHIESTI + 
                                              NUM_CENTROCAMPISTI_RICHIESTI + NUM_ATTACCANTI_RICHIESTI;
    
    // Costruttori
    public SquadraFantacalcio() {
        this.calciatori = new ArrayList<>();
        this.budgetTotale = BUDGET_INIZIALE;
        this.budgetRimanente = BUDGET_INIZIALE;
        this.dataCreazione = LocalDateTime.now();
        this.dataUltimaModifica = LocalDateTime.now();
        this.completata = false;
    }
    
    public SquadraFantacalcio(String nomeSquadra, int idUtente) {
        this();
        this.nomeSquadra = nomeSquadra;
        this.idUtente = idUtente;
    }
    
    // Costruttore completo (per lettura da database)
    public SquadraFantacalcio(int idSquadraFantacalcio, String nomeSquadra, int idUtente, 
                             int budgetTotale, int budgetRimanente, LocalDateTime dataCreazione,
                             LocalDateTime dataUltimaModifica, boolean completata) {
        this.idSquadraFantacalcio = idSquadraFantacalcio;
        this.nomeSquadra = nomeSquadra;
        this.idUtente = idUtente;
        this.budgetTotale = budgetTotale;
        this.budgetRimanente = budgetRimanente;
        this.dataCreazione = dataCreazione;
        this.dataUltimaModifica = dataUltimaModifica;
        this.completata = completata;
        this.calciatori = new ArrayList<>();
    }
    
    // Getters e Setters
    public int getIdSquadraFantacalcio() {
        return idSquadraFantacalcio;
    }
    
    public void setIdSquadraFantacalcio(int idSquadraFantacalcio) {
        this.idSquadraFantacalcio = idSquadraFantacalcio;
    }
    
    public String getNomeSquadra() {
        return nomeSquadra;
    }
    
    public void setNomeSquadra(String nomeSquadra) {
        this.nomeSquadra = nomeSquadra;
    }
    
    public int getIdUtente() {
        return idUtente;
    }
    
    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }
    
    public int getBudgetTotale() {
        return budgetTotale;
    }
    
    public void setBudgetTotale(int budgetTotale) {
        this.budgetTotale = budgetTotale;
    }
    
    public int getBudgetRimanente() {
        return budgetRimanente;
    }
    
    public void setBudgetRimanente(int budgetRimanente) {
        this.budgetRimanente = budgetRimanente;
    }
    
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }
    
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
    
    public LocalDateTime getDataUltimaModifica() {
        return dataUltimaModifica;
    }
    
    public void setDataUltimaModifica(LocalDateTime dataUltimaModifica) {
        this.dataUltimaModifica = dataUltimaModifica;
    }
    
    public boolean isCompletata() {
        return completata;
    }
    
    public void setCompletata(boolean completata) {
        this.completata = completata;
    }
    
    public List<Calciatore> getCalciatori() {
        return new ArrayList<>(calciatori); // Copia defensiva
    }
    
    public void setCalciatori(List<Calciatore> calciatori) {
        this.calciatori = new ArrayList<>(calciatori);
    }
    
    // Metodi di business logic
    
    /**
     * Aggiunge un calciatore alla squadra se possibile
     */
    public boolean aggiungiCalciatore(Calciatore calciatore) {
        // Verifica budget
        if (calciatore.getCosto() > budgetRimanente) {
            return false; // Budget insufficiente
        }
        
        // Verifica limiti per ruolo
        if (!rispettaLimitiRuolo(calciatore.getRuolo())) {
            return false; // Troppi giocatori di questo ruolo
        }
        
        // Verifica che il calciatore non sia già presente
        if (calciatori.stream().anyMatch(c -> c.getIdCalciatore() == calciatore.getIdCalciatore())) {
            return false; // Calciatore già presente
        }
        
        calciatori.add(calciatore);
        budgetRimanente -= calciatore.getCosto();
        dataUltimaModifica = LocalDateTime.now();
        
        // Verifica se la squadra è completa
        verificaCompletamento();
        
        return true;
    }
    
    /**
     * Rimuove un calciatore dalla squadra
     */
    public boolean rimuoviCalciatore(int idCalciatore) {
        for (int i = 0; i < calciatori.size(); i++) {
            if (calciatori.get(i).getIdCalciatore() == idCalciatore) {
                Calciatore rimosso = calciatori.remove(i);
                budgetRimanente += rimosso.getCosto();
                dataUltimaModifica = LocalDateTime.now();
                
                // La squadra non può più essere completa se rimuoviamo un giocatore
                completata = false;
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica se si può aggiungere un altro giocatore del ruolo specificato
     */
    private boolean rispettaLimitiRuolo(Calciatore.Ruolo ruolo) {
        long count = calciatori.stream()
                               .filter(c -> c.getRuolo() == ruolo)
                               .count();
        
        return switch (ruolo) {
            case PORTIERE -> count < NUM_PORTIERI_RICHIESTI;
            case DIFENSORE -> count < NUM_DIFENSORI_RICHIESTI;
            case CENTROCAMPISTA -> count < NUM_CENTROCAMPISTI_RICHIESTI;
            case ATTACCANTE -> count < NUM_ATTACCANTI_RICHIESTI;
        };
    }
    
    /**
     * Conta i giocatori per ogni ruolo
     */
    public int contaGiocatoriPerRuolo(Calciatore.Ruolo ruolo) {
        return (int) calciatori.stream()
                              .filter(c -> c.getRuolo() == ruolo)
                              .count();
    }
    
    /**
     * Verifica se la squadra è completa (ha tutti i giocatori richiesti)
     */
    private void verificaCompletamento() {
        completata = contaGiocatoriPerRuolo(Calciatore.Ruolo.PORTIERE) == NUM_PORTIERI_RICHIESTI &&
                    contaGiocatoriPerRuolo(Calciatore.Ruolo.DIFENSORE) == NUM_DIFENSORI_RICHIESTI &&
                    contaGiocatoriPerRuolo(Calciatore.Ruolo.CENTROCAMPISTA) == NUM_CENTROCAMPISTI_RICHIESTI &&
                    contaGiocatoriPerRuolo(Calciatore.Ruolo.ATTACCANTE) == NUM_ATTACCANTI_RICHIESTI;
    }
    
    /**
     * Restituisce il numero totale di giocatori nella squadra
     */
    public int getNumeroTotaleGiocatori() {
        return calciatori.size();
    }
    
    /**
     * Restituisce il budget speso
     */
    public int getBudgetSpeso() {
        return budgetTotale - budgetRimanente;
    }
    
    /**
     * Restituisce una stringa con le statistiche della squadra
     */
    public String getStatistiche() {
        return String.format(
            "Giocatori: %d/%d | Budget: %d/%d | P:%d D:%d C:%d A:%d",
            getNumeroTotaleGiocatori(), TOTALE_GIOCATORI,
            getBudgetSpeso(), budgetTotale,
            contaGiocatoriPerRuolo(Calciatore.Ruolo.PORTIERE),
            contaGiocatoriPerRuolo(Calciatore.Ruolo.DIFENSORE),
            contaGiocatoriPerRuolo(Calciatore.Ruolo.CENTROCAMPISTA),
            contaGiocatoriPerRuolo(Calciatore.Ruolo.ATTACCANTE)
        );
    }
    
    /**
     * Verifica se è possibile acquistare un calciatore
     */
    public boolean puoAcquistare(Calciatore calciatore) {
        return calciatore.getCosto() <= budgetRimanente && 
               rispettaLimitiRuolo(calciatore.getRuolo()) &&
               !calciatori.stream().anyMatch(c -> c.getIdCalciatore() == calciatore.getIdCalciatore());
    }
    
    // equals, hashCode e toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SquadraFantacalcio that = (SquadraFantacalcio) o;
        return idSquadraFantacalcio == that.idSquadraFantacalcio &&
               Objects.equals(nomeSquadra, that.nomeSquadra);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idSquadraFantacalcio, nomeSquadra);
    }
    
    @Override
    public String toString() {
        return String.format("SquadraFantacalcio{id=%d, nome='%s', giocatori=%d, budget=%d/%d, completa=%s}", 
                           idSquadraFantacalcio, nomeSquadra, getNumeroTotaleGiocatori(), 
                           getBudgetSpeso(), budgetTotale, completata);
    }
}