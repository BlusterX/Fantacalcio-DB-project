package fantacalcio.model;

import java.util.Objects;

public class Squadra {

    private int idSquadra;
    private String nome;
    private int idCampionato;
    private int idCalciatore;

    // Costruttori
    public Squadra() {}

    // usato per creazione (senza id PK)
    public Squadra(String nome, int idCampionato, int idCalciatore) {
        this.nome = nome;
        this.idCampionato = idCampionato;
        this.idCalciatore = idCalciatore;
    }

    // costruttore completo (lettura da DB)
    public Squadra(int idSquadra, String nome, int idCampionato, int idCalciatore) {
        this.idSquadra = idSquadra;
        this.nome = nome;
        this.idCampionato = idCampionato;
        this.idCalciatore = idCalciatore;
    }

    // Getters e Setters
    public int getIdSquadra() {
        return idSquadra;
    }

    public void setIdSquadra(int idSquadra) {
        this.idSquadra = idSquadra;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getIdCampionato() {
        return idCampionato;
    }

    public void setIdCampionato(int idCampionato) {
        this.idCampionato = idCampionato;
    }

    public int getIdCalciatore() {
        return idCalciatore;
    }

    public void setIdCalciatore(int idCalciatore) {
        this.idCalciatore = idCalciatore;
    }

    // equals, hashCode e toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Squadra that = (Squadra) o;
        return idSquadra == that.idSquadra && Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSquadra, nome);
    }

    @Override
    public String toString() {
        return nome;
    }
}
