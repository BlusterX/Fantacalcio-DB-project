package fantacalcio.model;

import java.util.Objects;

/**
 * Model minimale per TIPO_CAMPIONATO (solo campi presenti nel DB).
 */
public class TipoCampionato {

    private int idCampionato;   // ID_Campionato (PK, AI)
    private String nome;        // Nome
    private int anno;           // Anno (YEAR)

    public TipoCampionato() {}

    // costruttore per insert (senza id)
    public TipoCampionato(String nome, int anno) {
        this.nome = nome;
        this.anno = anno;
    }

    // costruttore completo (lettura da DB)
    public TipoCampionato(int idCampionato, String nome, int anno) {
        this.idCampionato = idCampionato;
        this.nome = nome;
        this.anno = anno;
    }

    public int getIdCampionato() { return idCampionato; }
    public void setIdCampionato(int idCampionato) { this.idCampionato = idCampionato; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getAnno() { return anno; }
    public void setAnno(int anno) { this.anno = anno; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoCampionato)) return false;
        TipoCampionato that = (TipoCampionato) o;
        return idCampionato == that.idCampionato;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCampionato);
    }

    @Override
    public String toString() {
        return String.format("TipoCampionato{id=%d, nome='%s', anno=%d}", idCampionato, nome, anno);
    }
}
