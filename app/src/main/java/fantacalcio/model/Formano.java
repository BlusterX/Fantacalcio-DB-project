package fantacalcio.model;

import java.util.Objects;

/** Tabella FORMANO: (ID_Formazione, ID_Calciatore) PK + Panchina ENUM('SI','NO') */
public class Formano {
    public enum PanchinaFlag { SI, NO }

    private int idFormazione;
    private int idCalciatore;
    private PanchinaFlag panchina;

    public Formano(int idFormazione, int idCalciatore, PanchinaFlag panchina) {
        this.idFormazione = idFormazione;
        this.idCalciatore = idCalciatore;
        this.panchina = panchina;
    }

    public int getIdFormazione() { return idFormazione; }
    public int getIdCalciatore() { return idCalciatore; }
    public PanchinaFlag getPanchina() { return panchina; }

    public void setIdFormazione(int idFormazione) { this.idFormazione = idFormazione; }
    public void setIdCalciatore(int idCalciatore) { this.idCalciatore = idCalciatore; }
    public void setPanchina(PanchinaFlag panchina) { this.panchina = panchina; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Formano)) return false;
        Formano f = (Formano) o;
        return idFormazione == f.idFormazione && idCalciatore == f.idCalciatore;
    }
    @Override public int hashCode() {
        return Objects.hash(idFormazione, idCalciatore);
    }
}
