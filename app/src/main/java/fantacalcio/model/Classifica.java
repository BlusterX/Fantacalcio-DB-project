package fantacalcio.model;

import java.util.Objects;

public class Classifica {

    private int idClassifica;         // ID_Classifica (PK)
    private int punti;                // Punti
    private int vittorie;             // Vittorie
    private int pareggi;              // Pareggi
    private int sconfitte;            // Sconfitte
    private double punteggioTotale;   // Punteggio_totale DECIMAL(6,2)
    private double punteggioSubito;   // Punteggio_subito DECIMAL(6,2)

    // Costruttori
    public Classifica() {}

    public Classifica(int idClassifica, int punti, int vittorie, int pareggi,
                      int sconfitte, double punteggioTotale, double punteggioSubito) {
        this.idClassifica = idClassifica;
        this.punti = punti;
        this.vittorie = vittorie;
        this.pareggi = pareggi;
        this.sconfitte = sconfitte;
        this.punteggioTotale = punteggioTotale;
        this.punteggioSubito = punteggioSubito;
    }

    // Getters & Setters
    public int getIdClassifica() { return idClassifica; }
    public void setIdClassifica(int idClassifica) { this.idClassifica = idClassifica; }

    public int getPunti() { return punti; }
    public void setPunti(int punti) { this.punti = punti; }

    public int getVittorie() { return vittorie; }
    public void setVittorie(int vittorie) { this.vittorie = vittorie; }

    public int getPareggi() { return pareggi; }
    public void setPareggi(int pareggi) { this.pareggi = pareggi; }

    public int getSconfitte() { return sconfitte; }
    public void setSconfitte(int sconfitte) { this.sconfitte = sconfitte; }

    public double getPunteggioTotale() { return punteggioTotale; }
    public void setPunteggioTotale(double punteggioTotale) { this.punteggioTotale = punteggioTotale; }

    public double getPunteggioSubito() { return punteggioSubito; }
    public void setPunteggioSubito(double punteggioSubito) { this.punteggioSubito = punteggioSubito; }

    // equals/hashCode/toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Classifica)) return false;
        Classifica that = (Classifica) o;
        return idClassifica == that.idClassifica;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idClassifica);
    }

    @Override
    public String toString() {
        return "Classifica{" +
                "id=" + idClassifica +
                ", punti=" + punti +
                ", V=" + vittorie +
                ", P=" + pareggi +
                ", S=" + sconfitte +
                ", tot=" + punteggioTotale +
                ", sub=" + punteggioSubito +
                '}';
    }
}
