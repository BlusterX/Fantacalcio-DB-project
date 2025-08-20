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
    private int costo;
    private Ruolo ruolo;
    private boolean infortunato;
    private boolean squalificato;
    private int idSquadra;

    // Costruttori
    public Calciatore() {}

    // costruttore senza ID (uso per insert)
    public Calciatore(String nome, String cognome, Ruolo ruolo, int costo, boolean infortunato,
                      boolean squalificato, int idSquadra) {
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
        this.costo = costo;
        this.infortunato = infortunato;
        this.squalificato = squalificato;
        this.idSquadra = idSquadra;
    }

    // costruttore completo (lettura da DB)
    public Calciatore(int idCalciatore, String nome, String cognome, Ruolo ruolo, int costo,
                      boolean infortunato, boolean squalificato, int idSquadra) {
        this.idCalciatore = idCalciatore;
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
        this.costo = costo;
        this.infortunato = infortunato;
        this.squalificato = squalificato;
        this.idSquadra = idSquadra;
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

    public int getCosto() {
        return costo;
    }

    public void setCosto(int costo) {
        this.costo = costo;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    public boolean isInfortunato() {
        return infortunato;
    }

    public void setInfortunato(boolean infortunato) {
        this.infortunato = infortunato;
    }

    public boolean isSqualificato() {
        return squalificato;
    }

    public void setSqualificato(boolean squalificato) {
        this.squalificato = squalificato;
    }

    public int getIdSquadra() {
        return idSquadra;
    }

    public void setIdSquadra(int idSquadra) {
        this.idSquadra = idSquadra;
    }

    // Utilità
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    public String getNomeConRuolo() {
        return getNomeCompleto() + " (" + ruolo.getAbbreviazione() + ")";
    }

    public String getNomeConCosto() {
        return getNomeCompleto() + " - " + costo + " crediti";
    }

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
        return String.format("Calciatore{id=%d, nome='%s', ruolo=%s, costo=%d, infortunato=%s, squalificato=%s, idSquadra=%d}",
                idCalciatore, getNomeCompleto(), ruolo, costo, infortunato, squalificato, idSquadra);
    }
}
