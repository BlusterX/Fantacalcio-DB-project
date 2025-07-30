package fantacalcio.model;

import java.util.Objects;

public class SquadraSerieA {
    
    private int idSquadraA;
    private String nome;
    
    // Costruttori
    public SquadraSerieA() {}
    
    public SquadraSerieA(String nome) {
        this.nome = nome;
    }
    
    public SquadraSerieA(int idSquadraA, String nome) {
        this.idSquadraA = idSquadraA;
        this.nome = nome;
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
        return nome; // Per ComboBox e visualizzazione semplice
    }
}