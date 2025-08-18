package fantacalcio.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modello per gli scontri tra squadre in una lega
 */
public class ScontroLega {
    
    public enum StatoScontro {
        PROGRAMMATO, IN_CORSO, COMPLETATO
    }
    
    private int idScontro;
    private int idLega;
    private int idSquadra1;
    private int idSquadra2;
    private int giornata;
    private double punteggio1;
    private double punteggio2;
    private StatoScontro stato;
    private LocalDateTime dataInizio;
    
    // Campi aggiuntivi per visualizzazione
    private String nomeSquadra1;
    private String nomeSquadra2;
    
    // Costruttori
    public ScontroLega() {
        this.stato = StatoScontro.PROGRAMMATO;
        this.punteggio1 = 0.0;
        this.punteggio2 = 0.0;
    }
    
    public ScontroLega(int idLega, int idSquadra1, int idSquadra2, int giornata) {
        this();
        this.idLega = idLega;
        this.idSquadra1 = idSquadra1;
        this.idSquadra2 = idSquadra2;
        this.giornata = giornata;
    }
    
    // Getters e Setters
    public int getIdScontro() {
        return idScontro;
    }
    
    public void setIdScontro(int idScontro) {
        this.idScontro = idScontro;
    }
    
    public int getIdLega() {
        return idLega;
    }
    
    public void setIdLega(int idLega) {
        this.idLega = idLega;
    }
    
    public int getIdSquadra1() {
        return idSquadra1;
    }
    
    public void setIdSquadra1(int idSquadra1) {
        this.idSquadra1 = idSquadra1;
    }
    
    public int getIdSquadra2() {
        return idSquadra2;
    }
    
    public void setIdSquadra2(int idSquadra2) {
        this.idSquadra2 = idSquadra2;
    }
    
    public int getGiornata() {
        return giornata;
    }
    
    public void setGiornata(int giornata) {
        this.giornata = giornata;
    }
    
    public double getPunteggio1() {
        return punteggio1;
    }
    
    public void setPunteggio1(double punteggio1) {
        this.punteggio1 = punteggio1;
    }
    
    public double getPunteggio2() {
        return punteggio2;
    }
    
    public void setPunteggio2(double punteggio2) {
        this.punteggio2 = punteggio2;
    }
    
    public StatoScontro getStato() {
        return stato;
    }
    
    public void setStato(StatoScontro stato) {
        this.stato = stato;
    }
    
    public LocalDateTime getDataInizio() {
        return dataInizio;
    }
    
    public void setDataInizio(LocalDateTime dataInizio) {
        this.dataInizio = dataInizio;
    }
    
    public String getNomeSquadra1() {
        return nomeSquadra1;
    }
    
    public void setNomeSquadra1(String nomeSquadra1) {
        this.nomeSquadra1 = nomeSquadra1;
    }
    
    public String getNomeSquadra2() {
        return nomeSquadra2;
    }
    
    public void setNomeSquadra2(String nomeSquadra2) {
        this.nomeSquadra2 = nomeSquadra2;
    }
    
    // Metodi di utilità
    
    /**
     * Restituisce il risultato formattato
     */
    public String getRisultato() {
        if (stato == StatoScontro.COMPLETATO) {
            return String.format("%.1f - %.1f", punteggio1, punteggio2);
        } else {
            return "vs";
        }
    }
    
    /**
     * Restituisce la squadra vincitrice (null se pareggio o non completato)
     */
    public Integer getSquadraVincitrice() {
        if (stato != StatoScontro.COMPLETATO) {
            return null;
        }
        
        if (punteggio1 > punteggio2) {
            return idSquadra1;
        } else if (punteggio2 > punteggio1) {
            return idSquadra2;
        } else {
            return null; // Pareggio
        }
    }
    
    /**
     * Verifica se è un pareggio
     */
    public boolean isPareggio() {
        return stato == StatoScontro.COMPLETATO && 
               Math.abs(punteggio1 - punteggio2) < 0.01;
    }
    
    /**
     * Restituisce il nome dell'incontro
     */
    public String getNomeIncontro() {
        String nome1 = nomeSquadra1 != null ? nomeSquadra1 : "Squadra " + idSquadra1;
        String nome2 = nomeSquadra2 != null ? nomeSquadra2 : "Squadra " + idSquadra2;
        return nome1 + " vs " + nome2;
    }
    
    /**
     * Verifica se la partita è in corso
     */
    public boolean isInCorso() {
        return stato == StatoScontro.IN_CORSO;
    }
    
    /**
     * Verifica se la partita è completata
     */
    public boolean isCompletata() {
        return stato == StatoScontro.COMPLETATO;
    }
    
    /**
     * Calcola i punti per ogni squadra (3 vittoria, 1 pareggio, 0 sconfitta)
     */
    public int[] calcolaPuntiClassifica() {
        if (stato != StatoScontro.COMPLETATO) {
            return new int[]{0, 0};
        }
        
        if (punteggio1 > punteggio2) {
            return new int[]{3, 0}; // Squadra 1 vince
        } else if (punteggio2 > punteggio1) {
            return new int[]{0, 3}; // Squadra 2 vince
        } else {
            return new int[]{1, 1}; // Pareggio
        }
    }
    
    /**
     * Restituisce l'emoji dello stato
     */
    public String getStatoEmoji() {
        return switch (stato) {
            case PROGRAMMATO -> "⏳";
            case IN_CORSO -> "⚽";
            case COMPLETATO -> "✅";
        };
    }
    
    /**
     * Restituisce una descrizione dello stato
     */
    public String getDescrizioneStato() {
        return switch (stato) {
            case PROGRAMMATO -> "Da giocare";
            case IN_CORSO -> "In corso";
            case COMPLETATO -> "Completata";
        };
    }
    
    // equals, hashCode e toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScontroLega that = (ScontroLega) o;
        return idScontro == that.idScontro && 
               idLega == that.idLega && 
               giornata == that.giornata;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idScontro, idLega, giornata);
    }
    
    @Override
    public String toString() {
        return String.format("ScontroLega{giornata=%d, %s, risultato=%s, stato=%s}", 
                           giornata, getNomeIncontro(), getRisultato(), stato);
    }
}