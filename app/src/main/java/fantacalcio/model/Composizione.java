package fantacalcio.model;

import java.util.Objects;

/** Tabella COMPOSIZIONE: (ID_Calciatore, ID_Squadra_Fantacalcio) PK composta */
public class Composizione {
    private int idCalciatore;
    private int idSquadraFantacalcio;

    public Composizione(int idCalciatore, int idSquadraFantacalcio) {
        this.idCalciatore = idCalciatore;
        this.idSquadraFantacalcio = idSquadraFantacalcio;
    }

    public int getIdCalciatore() { return idCalciatore; }
    public int getIdSquadraFantacalcio() { return idSquadraFantacalcio; }

    public void setIdCalciatore(int idCalciatore) { this.idCalciatore = idCalciatore; }
    public void setIdSquadraFantacalcio(int idSquadraFantacalcio) { this.idSquadraFantacalcio = idSquadraFantacalcio; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Composizione)) return false;
        Composizione that = (Composizione) o;
        return idCalciatore == that.idCalciatore &&
               idSquadraFantacalcio == that.idSquadraFantacalcio;
    }
    @Override public int hashCode() {
        return Objects.hash(idCalciatore, idSquadraFantacalcio);
    }
}
