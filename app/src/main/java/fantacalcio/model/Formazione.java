package fantacalcio.model;

import java.util.ArrayList;
import java.util.List;

public class Formazione {

    private int idFormazione;
    private String modulo;
    private int idSquadraFantacalcio;
    private int numeroGiornata;
    private double punteggio;
    final private List<CalciatoreInFormazione> calciatori;

    // Classe interna per calciatori in formazione (titolare/panchina)
    public static class CalciatoreInFormazione {
        private final Calciatore calciatore;
        private final boolean panchina;
        private final Integer ordinePanchina;

        public CalciatoreInFormazione(Calciatore calciatore, boolean panchina, Integer ordinePanchina) {
            this.calciatore = calciatore;
            this.panchina = panchina;
            this.ordinePanchina = ordinePanchina;
        }

        public Calciatore getCalciatore() { return calciatore; }
        public boolean isPanchina() { return panchina; }
        public Integer getOrdinePanchina() { return ordinePanchina; }
    }

    public Formazione() {
        this.calciatori = new ArrayList<>();
        this.punteggio = 0.0;
    }

    public Formazione(String modulo, int idSquadraFantacalcio, int numeroGiornata) {
        this.modulo = modulo;
        this.idSquadraFantacalcio = idSquadraFantacalcio;
        this.numeroGiornata = numeroGiornata;
        this.punteggio = 0.0;
        this.calciatori = new ArrayList<>();
    }

    // Metodi utilità
    public void aggiungiTitolare(Calciatore c) {
        calciatori.add(new CalciatoreInFormazione(c, false, null));
    }

    public void aggiungiPanchinaro(Calciatore c, int ordine) {
        calciatori.add(new CalciatoreInFormazione(c, true, ordine));
    }

    public List<Calciatore> getTitolari() {
        return calciatori.stream()
                .filter(cf -> !cf.isPanchina())
                .map(CalciatoreInFormazione::getCalciatore)
                .toList();
    }

    public List<Calciatore> getPanchinari() {
        return calciatori.stream()
                .filter(CalciatoreInFormazione::isPanchina)
                .sorted((a, b) -> Integer.compare(
                        a.getOrdinePanchina() != null ? a.getOrdinePanchina() : 999,
                        b.getOrdinePanchina() != null ? b.getOrdinePanchina() : 999
                ))
                .map(CalciatoreInFormazione::getCalciatore)
                .toList();
    }

    public boolean isValida() {
        return calciatori.stream().filter(cf -> !cf.isPanchina()).count() == 11;
    }

    // Getters/Setters
    public int getIdFormazione() { return idFormazione; }
    public void setIdFormazione(int idFormazione) { this.idFormazione = idFormazione; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public int getIdSquadraFantacalcio() { return idSquadraFantacalcio; }
    public void setIdSquadraFantacalcio(int idSquadraFantacalcio) { this.idSquadraFantacalcio = idSquadraFantacalcio; }

    public int getNumeroGiornata() { return numeroGiornata; }
    public void setNumeroGiornata(int numeroGiornata) { this.numeroGiornata = numeroGiornata; }

    public double getPunteggio() { return punteggio; }
    public void setPunteggio(double punteggio) { this.punteggio = punteggio; }
}
