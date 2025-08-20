package fantacalcio.model;

import java.util.Objects;

public class SquadraSerieA {

    private int idSquadraA;
    private String nome;
    private int idCampionato;
    private int idCalciatore;

    // Costruttori
    public SquadraSerieA() {}

    // usato per creazione (senza id PK)
    public SquadraSerieA(String nome, int idCampionato, int idCalciatore) {
        this.nome = nome;
        this.idCampionato = idCampionato;
        this.idCalciatore = idCalciatore;
    }

    // costruttore completo (lettura da DB)
    public SquadraSerieA(int idSquadraA, String nome, int idCampionato, int idCalciatore) {
        this.idSquadraA = idSquadraA;
        this.nome = nome;
        this.idCampionato = idCampionato;
        this.idCalciatore = idCalciatore;
    }

    // Getters e Setters
    public int getIdSquadraA() {
        return idSquadraA;
    }

    public void setIdSquadraA(int idSquadraA) {
        this.idSquadraA = idSquadraA;
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
        SquadraSerieA that = (SquadraSerieA) o;
        return idSquadraA == that.idSquadraA && Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSquadraA, nome);
    }

    @Override
    public String toString() {
        return nome;
    }
}
