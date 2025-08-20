package fantacalcio.model;

public class PunteggioGiocatore {

    private int idCalciatore;
    private int idBonusMalus;
    private int numeroGiornata;
    private int quantita;

    // Costruttore vuoto
    public PunteggioGiocatore() {}

    // Costruttore completo
    public PunteggioGiocatore(int idCalciatore, int idBonusMalus, int numeroGiornata, int quantita) {
        this.idCalciatore = idCalciatore;
        this.idBonusMalus = idBonusMalus;
        this.numeroGiornata = numeroGiornata;
        this.quantita = quantita;
    }

    public int getIdCalciatore() {
        return idCalciatore;
    }
    public void setIdCalciatore(int idCalciatore) {
        this.idCalciatore = idCalciatore;
    }

    public int getIdBonusMalus() {
        return idBonusMalus;
    }
    public void setIdBonusMalus(int idBonusMalus) {
        this.idBonusMalus = idBonusMalus;
    }

    public int getNumeroGiornata() {
        return numeroGiornata;
    }
    public void setNumeroGiornata(int numeroGiornata) {
        this.numeroGiornata = numeroGiornata;
    }

    public int getQuantita() {
        return quantita;
    }
    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }
}
