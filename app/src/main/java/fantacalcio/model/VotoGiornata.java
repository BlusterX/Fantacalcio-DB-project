package fantacalcio.model;

import java.util.Objects;

/**
 * Model minimale per la tabella VOTO_GIORNATA
 * Colonne: Numero_Giornata (PK), ID_Calciatore (PK), Voto_base (NOT NULL)
 */
public class VotoGiornata {

    private int numeroGiornata;   // Numero_Giornata (PK)
    private int idCalciatore;     // ID_Calciatore (PK)
    private double votoBase;      // Voto_base

    public VotoGiornata() {}

    public VotoGiornata(int numeroGiornata, int idCalciatore, double votoBase) {
        this.numeroGiornata = numeroGiornata;
        this.idCalciatore = idCalciatore;
        this.votoBase = votoBase;
    }

    public int getNumeroGiornata() { return numeroGiornata; }
    public void setNumeroGiornata(int numeroGiornata) { this.numeroGiornata = numeroGiornata; }

    public int getIdCalciatore() { return idCalciatore; }
    public void setIdCalciatore(int idCalciatore) { this.idCalciatore = idCalciatore; }

    public double getVotoBase() { return votoBase; }
    public void setVotoBase(double votoBase) { this.votoBase = votoBase; }

    // equals/hashCode sulla PK composta
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VotoGiornata that)) return false;
        return numeroGiornata == that.numeroGiornata && idCalciatore == that.idCalciatore;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroGiornata, idCalciatore);
    }

    @Override
    public String toString() {
        return "VotoGiornata{" +
                "giornata=" + numeroGiornata +
                ", idCalciatore=" + idCalciatore +
                ", votoBase=" + votoBase +
                '}';
    }
}
