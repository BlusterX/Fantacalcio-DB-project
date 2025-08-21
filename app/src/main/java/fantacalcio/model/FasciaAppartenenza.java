package fantacalcio.model;

import java.util.Objects;

/**
 * Model minimale per la tabella FASCIA_APPARTENENZA (tabella di relazione)
 * PK composta: (ID_Calciatore, ID_Fascia)
 */
public class FasciaAppartenenza {

    private int idCalciatore; // ID_Calciatore (PK/FK)
    private int idFascia;     // ID_Fascia (PK/FK)

    public FasciaAppartenenza() {}

    public FasciaAppartenenza(int idCalciatore, int idFascia) {
        this.idCalciatore = idCalciatore;
        this.idFascia = idFascia;
    }

    public int getIdCalciatore() { return idCalciatore; }
    public void setIdCalciatore(int idCalciatore) { this.idCalciatore = idCalciatore; }

    public int getIdFascia() { return idFascia; }
    public void setIdFascia(int idFascia) { this.idFascia = idFascia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FasciaAppartenenza that)) return false;
        return idCalciatore == that.idCalciatore && idFascia == that.idFascia;
    }

    @Override
    public int hashCode() { return Objects.hash(idCalciatore, idFascia); }
}
