package fantacalcio.model;

import java.util.Objects;

/**
 * Model per tabella BONUS_MALUS
 * Colonne: ID_Bonus_Malus (PK), Tipologia, Punteggio
 */
public class BonusMalus {

    private int idBonusMalus;   // ID_Bonus_Malus
    private String tipologia;   // Tipologia
    private double punteggio;   // Punteggio (DECIMAL(4,2))

    public BonusMalus() {}

    public BonusMalus(String tipologia, double punteggio) {
        this.tipologia = tipologia;
        this.punteggio = punteggio;
    }

    public BonusMalus(int idBonusMalus, String tipologia, double punteggio) {
        this.idBonusMalus = idBonusMalus;
        this.tipologia = tipologia;
        this.punteggio = punteggio;
    }

    public int getIdBonusMalus() { return idBonusMalus; }
    public void setIdBonusMalus(int idBonusMalus) { this.idBonusMalus = idBonusMalus; }

    public String getTipologia() { return tipologia; }
    public void setTipologia(String tipologia) { this.tipologia = tipologia; }

    public double getPunteggio() { return punteggio; }
    public void setPunteggio(double punteggio) { this.punteggio = punteggio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BonusMalus that)) return false;
        return idBonusMalus == that.idBonusMalus &&
               Objects.equals(tipologia, that.tipologia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idBonusMalus, tipologia);
    }

    @Override
    public String toString() {
        return "BonusMalus{" +
                "id=" + idBonusMalus +
                ", tipologia='" + tipologia + '\'' +
                ", punteggio=" + punteggio +
                '}';
    }
}
