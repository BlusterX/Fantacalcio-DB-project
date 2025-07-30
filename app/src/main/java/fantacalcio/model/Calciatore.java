package fantacalcio.model;

import java.util.Objects;

public class Calciatore {
    
    public enum Ruolo {
        PORTIERE("P"),
        DIFENSORE("D"), 
        CENTROCAMPISTA("C"),
        ATTACCANTE("A");
        
        private final String abbreviazione;
        
        Ruolo(String abbreviazione) {
            this.abbreviazione = abbreviazione;
        }
        
        public String getAbbreviazione() {
            return abbreviazione;
        }
        
        @Override
        public String toString() {
            return abbreviazione;
        }
    }
    
    private int idCalciatore;
    private String nome;
    private String cognome;
    private Ruolo ruolo;
    private int costo; // Costo in crediti
    
    // Costruttori
    public Calciatore() {}
    
    public Calciatore(String nome, String cognome, Ruolo ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
        this.costo = getCostoDefaultPerRuolo(ruolo);
    }
    
    public Calciatore(String nome, String cognome, Ruolo ruolo, int costo) {
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
        this.costo = costo;
    }
    
    // Costruttore completo (con ID per lettura da database)
    public Calciatore(int idCalciatore, String nome, String cognome, Ruolo ruolo, int costo) {
        this.idCalciatore = idCalciatore;
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
        this.costo = costo;
    }
    
    // Getters e Setters
    public int getIdCalciatore() {
        return idCalciatore;
    }
    
    public void setIdCalciatore(int idCalciatore) {
        this.idCalciatore = idCalciatore;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getCognome() {
        return cognome;
    }
    
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    
    public Ruolo getRuolo() {
        return ruolo;
    }
    
    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }
    
    public int getCosto() {
        return costo;
    }
    
    public void setCosto(int costo) {
        this.costo = costo;
    }
    
    // Metodi di utilità
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
    
    public String getNomeConRuolo() {
        return getNomeCompleto() + " (" + ruolo.getAbbreviazione() + ")";
    }
    
    public String getNomeConCosto() {
        return getNomeCompleto() + " - " + costo + " crediti";
    }
    
    /**
     * Restituisce il costo di default per un ruolo
     */
    public static int getCostoDefaultPerRuolo(Ruolo ruolo) {
        return switch (ruolo) {
            case PORTIERE -> 30;
            case DIFENSORE -> 40;
            case CENTROCAMPISTA -> 50;
            case ATTACCANTE -> 60;
        };
    }
    
    // equals, hashCode e toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Calciatore that = (Calciatore) o;
        return idCalciatore == that.idCalciatore &&
               Objects.equals(nome, that.nome) &&
               Objects.equals(cognome, that.cognome);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idCalciatore, nome, cognome);
    }
    
    @Override
    public String toString() {
        return String.format("Calciatore{id=%d, nome='%s', ruolo=%s, costo=%d}", 
                           idCalciatore, getNomeCompleto(), ruolo, costo);
    }
}