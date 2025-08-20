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
    private int idFormazione1;
    private int idFormazione2;
    private int giornata;
    private String risultato;         // ex punteggio1/punteggio2
    private StatoScontro stato;
    private LocalDateTime dataInizio;

    // Campi opzionali (non presenti nel DB, utili per la GUI)
    private String nomeSquadra1;
    private String nomeSquadra2;

    // Costruttori
    public ScontroLega() {
        this.stato = StatoScontro.PROGRAMMATO;
        this.risultato = null;
    }

    public ScontroLega(int idLega, int idFormazione1, int idFormazione2, int giornata) {
        this();
        this.idLega = idLega;
        this.idFormazione1 = idFormazione1;
        this.idFormazione2 = idFormazione2;
        this.giornata = giornata;
    }

    // Getters e Setters
    public int getIdScontro() { return idScontro; }
    public void setIdScontro(int idScontro) { this.idScontro = idScontro; }

    public int getIdLega() { return idLega; }
    public void setIdLega(int idLega) { this.idLega = idLega; }

    public int getIdFormazione1() { return idFormazione1; }
    public void setIdFormazione1(int idFormazione1) { this.idFormazione1 = idFormazione1; }

    public int getIdFormazione2() { return idFormazione2; }
    public void setIdFormazione2(int idFormazione2) { this.idFormazione2 = idFormazione2; }

    public int getGiornata() { return giornata; }
    public void setGiornata(int giornata) { this.giornata = giornata; }

    public String getRisultato() { return risultato; }
    public void setRisultato(String risultato) { this.risultato = risultato; }

    public StatoScontro getStato() { return stato; }
    public void setStato(StatoScontro stato) { this.stato = stato; }

    public LocalDateTime getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDateTime dataInizio) { this.dataInizio = dataInizio; }

    public String getNomeSquadra1() { return nomeSquadra1; }
    public void setNomeSquadra1(String nomeSquadra1) { this.nomeSquadra1 = nomeSquadra1; }

    public String getNomeSquadra2() { return nomeSquadra2; }
    public void setNomeSquadra2(String nomeSquadra2) { this.nomeSquadra2 = nomeSquadra2; }

    // Metodi utilità

    /** Restituisce il nome dell'incontro */
    public String getNomeIncontro() {
        String n1 = (nomeSquadra1 != null ? nomeSquadra1 : "Squadra " + idFormazione1);
        String n2 = (nomeSquadra2 != null ? nomeSquadra2 : "Squadra " + idFormazione2);
        return n1 + " vs " + n2;
    }

    /** Restituisce il risultato formattato o "vs" se non ancora completato */
    public String getRisultatoFormattato() {
        return (stato == StatoScontro.COMPLETATO && risultato != null) ? risultato : "vs";
    }

    /** Verifica se la partita è completata */
    public boolean isCompletata() {
        return stato == StatoScontro.COMPLETATO;
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
                giornata, getNomeIncontro(), getRisultatoFormattato(), stato);
    }
}
