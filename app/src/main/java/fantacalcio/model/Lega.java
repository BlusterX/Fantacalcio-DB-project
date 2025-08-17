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
    private int idAdmin;
    
    // Costruttori
    public Lega() {}
    
    public Lega(String nome, int idAdmin) {
        this.nome = nome;
        this.idAdmin = idAdmin;
        this.codiceAccesso = generaCodiceAccesso();
    }
    
    public Lega(int idLega, String nome, String codiceAccesso, int idAdmin) {
        this.idLega = idLega;
        this.nome = nome;
        this.codiceAccesso = codiceAccesso;
        this.idAdmin = idAdmin;
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
    
    public int getIdAdmin() {
        return idAdmin;
    }
    
    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
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