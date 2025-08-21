package fantacalcio.model;

import java.util.Objects;

/**
 * Model minimale per la tabella FASCIA_GIOCATORE
 */
public class FasciaGiocatore {

    private int idFascia;                 // ID_Fascia (PK)
    private String nomeFascia;            // Nome_fascia
    private double probGolAttaccante;     // Prob_gol_attaccante
    private double probGolCentrocampista; // Prob_gol_centrocampista
    private double probGolDifensore;      // Prob_gol_difensore
    private double probAssist;            // Prob_assist
    private double probAmmonizione;       // Prob_ammonizione
    private double probEspulsione;        // Prob_espulsione
    private double probImbattibilita;     // Prob_imbattibilita
    private double votoBaseStandard;      // Voto_base_standard

    public FasciaGiocatore() {}

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

    public int getIdFascia() { return idFascia; }
    public void setIdFascia(int idFascia) { this.idFascia = idFascia; }

    public String getNomeFascia() { return nomeFascia; }
    public void setNomeFascia(String nomeFascia) { this.nomeFascia = nomeFascia; }

    public double getProbGolAttaccante() { return probGolAttaccante; }
    public void setProbGolAttaccante(double probGolAttaccante) { this.probGolAttaccante = probGolAttaccante; }

    public double getProbGolCentrocampista() { return probGolCentrocampista; }
    public void setProbGolCentrocampista(double probGolCentrocampista) { this.probGolCentrocampista = probGolCentrocampista; }

    public double getProbGolDifensore() { return probGolDifensore; }
    public void setProbGolDifensore(double probGolDifensore) { this.probGolDifensore = probGolDifensore; }

    public double getProbAssist() { return probAssist; }
    public void setProbAssist(double probAssist) { this.probAssist = probAssist; }

    public double getProbAmmonizione() { return probAmmonizione; }
    public void setProbAmmonizione(double probAmmonizione) { this.probAmmonizione = probAmmonizione; }

    public double getProbEspulsione() { return probEspulsione; }
    public void setProbEspulsione(double probEspulsione) { this.probEspulsione = probEspulsione; }

    public double getProbImbattibilita() { return probImbattibilita; }
    public void setProbImbattibilita(double probImbattibilita) { this.probImbattibilita = probImbattibilita; }

    public double getVotoBaseStandard() { return votoBaseStandard; }
    public void setVotoBaseStandard(double votoBaseStandard) { this.votoBaseStandard = votoBaseStandard; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FasciaGiocatore that)) return false;
        return idFascia == that.idFascia;
    }

    @Override
    public int hashCode() { return Objects.hash(idFascia); }
}
