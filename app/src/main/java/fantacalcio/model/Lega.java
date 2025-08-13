package fantacalcio.model;

import java.util.Objects;
import java.util.Random;

/**
 * Modello per le leghe fantacalcio
 */
public class Lega {
    
    private int idLega;
    private String nome;
    private String codiceAccesso;
    
    // Costruttori
    public Lega() {}
    
    public Lega(String nome) {
        this.nome = nome;
        this.codiceAccesso = generaCodiceAccesso();
    }
    
    public Lega(int idLega, String nome, String codiceAccesso) {
        this.idLega = idLega;
        this.nome = nome;
        this.codiceAccesso = codiceAccesso;
    }
    
    // Getters e Setters
    public int getIdLega() {
        return idLega;
    }
    
    public void setIdLega(int idLega) {
        this.idLega = idLega;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getCodiceAccesso() {
        return codiceAccesso;
    }
    
    public void setCodiceAccesso(String codiceAccesso) {
        this.codiceAccesso = codiceAccesso;
    }
    
    // Metodi di utilità
    
    /**
     * Genera un codice di accesso casuale per la lega
     */
    private String generaCodiceAccesso() {
        String caratteri = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codice = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 6; i++) {
            codice.append(caratteri.charAt(random.nextInt(caratteri.length())));
        }
        
        return codice.toString();
    }
    
    /**
     * Rigenera un nuovo codice di accesso
     */
    public void rigeneraCodiceAccesso() {
        this.codiceAccesso = generaCodiceAccesso();
    }
    
    // equals, hashCode e toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lega lega = (Lega) o;
        return idLega == lega.idLega && Objects.equals(nome, lega.nome);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idLega, nome);
    }
    
    @Override
    public String toString() {
        return nome + " (" + codiceAccesso + ")";
    }
}