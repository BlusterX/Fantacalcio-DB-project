package fantacalcio.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SquadraFantacalcio {

    private int idSquadraFantacalcio;
    private String nomeSquadra;
    private int budgetTotale;
    private int budgetRimanente;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataUltimaModifica;
    private boolean completata;
    private Integer idClassifica;
    private Integer idUtente;
    private Integer idLega;
    private List<Calciatore> calciatori;

    // Costanti per le regole
    public static final int BUDGET_INIZIALE = 1000;
    public static final int NUM_PORTIERI_RICHIESTI = 3;
    public static final int NUM_DIFENSORI_RICHIESTI = 8;
    public static final int NUM_CENTROCAMPISTI_RICHIESTI = 8;
    public static final int NUM_ATTACCANTI_RICHIESTI = 6;
    public static final int TOTALE_GIOCATORI = NUM_PORTIERI_RICHIESTI + NUM_DIFENSORI_RICHIESTI +
                                               NUM_CENTROCAMPISTI_RICHIESTI + NUM_ATTACCANTI_RICHIESTI;

    public SquadraFantacalcio() {
        this.calciatori = new ArrayList<>();
        this.budgetTotale = BUDGET_INIZIALE;
        this.budgetRimanente = BUDGET_INIZIALE;
        this.dataCreazione = LocalDateTime.now();
        this.dataUltimaModifica = LocalDateTime.now();
        this.completata = false;
        this.idClassifica = null;
        this.idUtente = null;
        this.idLega = null;
    }

    public SquadraFantacalcio(String nome) {
        this();
        this.nomeSquadra = nome;
    }

    // Costruttore completo (lettura da DB)
    public SquadraFantacalcio(int idSquadraFantacalcio, String nomeSquadra, int budgetTotale,
                              int budgetRimanente, LocalDateTime dataCreazione,
                              LocalDateTime dataUltimaModifica, boolean completata,
                              Integer idClassifica, Integer idUtente, Integer idLega) {
        this.idSquadraFantacalcio = idSquadraFantacalcio;
        this.nomeSquadra = nomeSquadra;
        this.budgetTotale = budgetTotale;
        this.budgetRimanente = budgetRimanente;
        this.dataCreazione = dataCreazione;
        this.dataUltimaModifica = dataUltimaModifica;
        this.completata = completata;
        this.idClassifica = idClassifica;
        this.idUtente = idUtente;
        this.idLega = idLega;
        this.calciatori = new ArrayList<>();
    }

    // Getters e Setters
    public int getIdSquadraFantacalcio() { return idSquadraFantacalcio; }
    public void setIdSquadraFantacalcio(int idSquadraFantacalcio) { this.idSquadraFantacalcio = idSquadraFantacalcio; }

    public String getNomeSquadra() { return nomeSquadra; }
    public void setNomeSquadra(String nomeSquadra) { this.nomeSquadra = nomeSquadra; }

    public int getBudgetTotale() { return budgetTotale; }
    public void setBudgetTotale(int budgetTotale) { this.budgetTotale = budgetTotale; }

    public int getBudgetRimanente() { return budgetRimanente; }
    public void setBudgetRimanente(int budgetRimanente) { this.budgetRimanente = budgetRimanente; }

    public LocalDateTime getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(LocalDateTime dataCreazione) { this.dataCreazione = dataCreazione; }

    public LocalDateTime getDataUltimaModifica() { return dataUltimaModifica; }
    public void setDataUltimaModifica(LocalDateTime dataUltimaModifica) { this.dataUltimaModifica = dataUltimaModifica; }

    public boolean isCompletata() { return completata; }
    public void setCompletata(boolean completata) { this.completata = completata; }

    public Integer getIdClassifica() { return idClassifica; }
    public void setIdClassifica(Integer idClassifica) { this.idClassifica = idClassifica; }

    public Integer getIdUtente() { return idUtente; }
    public void setIdUtente(Integer idUtente) { this.idUtente = idUtente; }

    public Integer getIdLega() { return idLega; }
    public void setIdLega(Integer idLega) { this.idLega = idLega; }

    public List<Calciatore> getCalciatori() {
        return new ArrayList<>(calciatori);
    }
    public void setCalciatori(List<Calciatore> calciatori) {
        this.calciatori = new ArrayList<>(calciatori);
    }

    // Business logic
    public boolean aggiungiCalciatore(Calciatore calciatore) {
        if (calciatore.getCosto() > budgetRimanente) return false;
        if (!rispettaLimitiRuolo(calciatore.getRuolo())) return false;
        if (calciatori.stream().anyMatch(c -> c.getIdCalciatore() == calciatore.getIdCalciatore())) return false;

        calciatori.add(calciatore);
        budgetRimanente -= calciatore.getCosto();
        dataUltimaModifica = LocalDateTime.now();
        verificaCompletamento();
        return true;
    }

    public boolean rimuoviCalciatore(int idCalciatore) {
        for (int i = 0; i < calciatori.size(); i++) {
            if (calciatori.get(i).getIdCalciatore() == idCalciatore) {
                Calciatore removed = calciatori.remove(i);
                budgetRimanente += removed.getCosto();
                dataUltimaModifica = LocalDateTime.now();
                completata = false;
                return true;
            }
        }
        return false;
    }

    private boolean rispettaLimitiRuolo(Calciatore.Ruolo ruolo) {
        long count = calciatori.stream()
                .filter(c -> c.getRuolo() == ruolo)
                .count();

        return switch (ruolo) {
            case PORTIERE     -> count < NUM_PORTIERI_RICHIESTI;
            case DIFENSORE    -> count < NUM_DIFENSORI_RICHIESTI;
            case CENTROCAMPISTA -> count < NUM_CENTROCAMPISTI_RICHIESTI;
            case ATTACCANTE   -> count < NUM_ATTACCANTI_RICHIESTI;
        };
    }

    public int contaGiocatoriPerRuolo(Calciatore.Ruolo ruolo) {
        return (int) calciatori.stream()
                .filter(c -> c.getRuolo() == ruolo)
                .count();
    }

    private void verificaCompletamento() {
        completata =
            contaGiocatoriPerRuolo(Calciatore.Ruolo.PORTIERE)     == NUM_PORTIERI_RICHIESTI &&
            contaGiocatoriPerRuolo(Calciatore.Ruolo.DIFENSORE)    == NUM_DIFENSORI_RICHIESTI &&
            contaGiocatoriPerRuolo(Calciatore.Ruolo.CENTROCAMPISTA) == NUM_CENTROCAMPISTI_RICHIESTI &&
            contaGiocatoriPerRuolo(Calciatore.Ruolo.ATTACCANTE)   == NUM_ATTACCANTI_RICHIESTI;
    }

    public int getNumeroTotaleGiocatori() {
        return calciatori.size();
    }

    public int getBudgetSpeso() {
        return budgetTotale - budgetRimanente;
    }

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

    public boolean puoAcquistare(Calciatore calciatore) {
        return calciatore.getCosto() <= budgetRimanente &&
               rispettaLimitiRuolo(calciatore.getRuolo()) &&
               calciatori.stream().noneMatch(c -> c.getIdCalciatore() == calciatore.getIdCalciatore());
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
        return String.format(
            "SquadraFantacalcio{id=%d, nome='%s', giocatori=%d, budget=%d/%d, completata=%s}",
            idSquadraFantacalcio, nomeSquadra, getNumeroTotaleGiocatori(),
            getBudgetSpeso(), budgetTotale, completata
        );
    }
}
