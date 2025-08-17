package fantacalcio.model;

public class PunteggioGiocatore {

    private int id; // id_punteggio (PK)
    private int idCalciatore;
    private int idBonusMalus;
    private int numeroGiornata;
    private int quantita;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
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
