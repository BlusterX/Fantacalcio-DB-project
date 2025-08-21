package fantacalcio.model;

import java.util.Random;

/**
 * Modello per le leghe fantacalcio
 */
public class Lega {

    private int idLega;
    private String nome;
    private String codiceAccesso;
    private int idAdmin;
    private String stato;          // "CREATA", "IN_CORSO", "TERMINATA"
    private Integer idCampionato;  // può essere null

    // Costruttori
    public Lega() {
        this.stato = "CREATA";
    }

    // Costruttore per creazione (senza campionato)
    public Lega(String nome, int idAdmin) {
        this.nome = nome;
        this.idAdmin = idAdmin;
        this.codiceAccesso = generaCodiceAccesso();
        this.stato = "CREATA";
        this.idCampionato = null;
    }

    // Costruttore completo (lettura da DB)
    public Lega(int idLega, String nome, String codiceAccesso, int idAdmin,
                String stato, Integer idCampionato) {
        this.idLega = idLega;
        this.nome = nome;
        this.codiceAccesso = codiceAccesso;
        this.idAdmin = idAdmin;
        this.stato = stato;
        this.idCampionato = idCampionato;
    }

    // Getters e Setters
    public int getIdLega() { return idLega; }
    public void setIdLega(int idLega) { this.idLega = idLega; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodiceAccesso() { return codiceAccesso; }
    public void setCodiceAccesso(String codiceAccesso) { this.codiceAccesso = codiceAccesso; }

    public int getIdAdmin() { return idAdmin; }
    public void setIdAdmin(int idAdmin) { this.idAdmin = idAdmin; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public Integer getIdCampionato() { return idCampionato; }
    public void setIdCampionato(Integer idCampionato) { this.idCampionato = idCampionato; }

    // Metodi di utilità

    private String generaCodiceAccesso() {
        String caratteri = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codice = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            codice.append(caratteri.charAt(random.nextInt(caratteri.length())));
        }
        return codice.toString();
    }

    public void rigeneraCodiceAccesso() {
        this.codiceAccesso = generaCodiceAccesso();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lega)) return false;
        return idLega == ((Lega) o).idLega;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idLega);
    }

    @Override
    public String toString() {
        return nome + " (" + codiceAccesso + ")";
    }
}
