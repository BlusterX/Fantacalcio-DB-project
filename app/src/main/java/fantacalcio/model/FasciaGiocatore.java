package fantacalcio.model;

import java.util.Objects;

/**
 * Modello per le fasce dei giocatori (TOP, SEMI-TOP, ecc.)
 */
public class FasciaGiocatore {

    public enum TipoFascia {
        TOP("TOP"),
        SEMI_TOP("SEMI-TOP"),
        BUONO("BUONO"),
        MEDIO("MEDIO"),
        SCARSO("SCARSO");

        private final String nome;

        TipoFascia(String nome) {
            this.nome = nome;
        }

        public String getNome() {
            return nome;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    private int idFascia;
    private String nomeFascia;
    private double probGolAttaccante;
    private double probGolCentrocampista;
    private double probGolDifensore;
    private double probAssist;
    private double probAmmonizione;
    private double probEspulsione;
    private double probImbattibilita;
    private double votoBaseStandard;

    // Costruttori
    public FasciaGiocatore() {}

    public FasciaGiocatore(String nomeFascia) {
        this.nomeFascia = nomeFascia;
        impostaValoriDefault();
    }

    public FasciaGiocatore(int idFascia, String nomeFascia, double probGolAttaccante,
                           double probGolCentrocampista, double probGolDifensore,
                           double probAssist, double probAmmonizione, double probEspulsione,
                           double probImbattibilita, double votoBaseStandard) {
        this.idFascia = idFascia;
        this.nomeFascia = nomeFascia;
        this.probGolAttaccante = probGolAttaccante;
        this.probGolCentrocampista = probGolCentrocampista;
        this.probGolDifensore = probGolDifensore;
        this.probAssist = probAssist;
        this.probAmmonizione = probAmmonizione;
        this.probEspulsione = probEspulsione;
        this.probImbattibilita = probImbattibilita;
        this.votoBaseStandard = votoBaseStandard;
    }

    private void impostaValoriDefault() {
        this.probGolAttaccante = 0.15;
        this.probGolCentrocampista = 0.08;
        this.probGolDifensore = 0.03;
        this.probAssist = 0.12;
        this.probAmmonizione = 0.20;
        this.probEspulsione = 0.02;
        this.probImbattibilita = 0.25;
        this.votoBaseStandard = 0.0;
    }

    // Getters e Setters
    public int getIdFascia() {
        return idFascia;
    }

    public void setIdFascia(int idFascia) {
        this.idFascia = idFascia;
    }

    public String getNomeFascia() {
        return nomeFascia;
    }

    public void setNomeFascia(String nomeFascia) {
        this.nomeFascia = nomeFascia;
    }

    public double getProbGolAttaccante() {
        return probGolAttaccante;
    }

    public void setProbGolAttaccante(double probGolAttaccante) {
        this.probGolAttaccante = probGolAttaccante;
    }

    public double getProbGolCentrocampista() {
        return probGolCentrocampista;
    }

    public void setProbGolCentrocampista(double probGolCentrocampista) {
        this.probGolCentrocampista = probGolCentrocampista;
    }

    public double getProbGolDifensore() {
        return probGolDifensore;
    }

    public void setProbGolDifensore(double probGolDifensore) {
        this.probGolDifensore = probGolDifensore;
    }

    public double getProbAssist() {
        return probAssist;
    }

    public void setProbAssist(double probAssist) {
        this.probAssist = probAssist;
    }

    public double getProbAmmonizione() {
        return probAmmonizione;
    }

    public void setProbAmmonizione(double probAmmonizione) {
        this.probAmmonizione = probAmmonizione;
    }

    public double getProbEspulsione() {
        return probEspulsione;
    }

    public void setProbEspulsione(double probEspulsione) {
        this.probEspulsione = probEspulsione;
    }

    public double getProbImbattibilita() {
        return probImbattibilita;
    }

    public void setProbImbattibilita(double probImbattibilita) {
        this.probImbattibilita = probImbattibilita;
    }

    public double getVotoBaseStandard() {
        return votoBaseStandard;
    }

    public void setVotoBaseStandard(double votoBaseStandard) {
        this.votoBaseStandard = votoBaseStandard;
    }

    // Metodi di utilità
    public double getProbGolPerRuolo(Calciatore.Ruolo ruolo) {
        return switch (ruolo) {
            case ATTACCANTE -> probGolAttaccante;
            case CENTROCAMPISTA -> probGolCentrocampista;
            case DIFENSORE -> probGolDifensore;
            case PORTIERE -> 0.0;
        };
    }

    public boolean isFasciaAlta() {
        return "TOP".equals(nomeFascia) || "SEMI-TOP".equals(nomeFascia);
    }

    // equals, hashCode e toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FasciaGiocatore that = (FasciaGiocatore) o;
        return idFascia == that.idFascia && Objects.equals(nomeFascia, that.nomeFascia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFascia, nomeFascia);
    }

    @Override
    public String toString() {
        return nomeFascia;
    }
}
