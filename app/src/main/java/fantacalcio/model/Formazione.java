package fantacalcio.model;

import java.util.ArrayList;
import java.util.List;

public class Formazione {
    private int idFormazione;
    private String modulo; // "3-4-3", "4-4-2", ecc
    private int idSquadra;
    private int numeroGiornata;
    private List<CalciatoreInFormazione> calciatori;
    
    // Classe interna per gestire calciatori con info panchina
    public static class CalciatoreInFormazione {
        private final Calciatore calciatore;
        private final boolean panchina;
        private final Integer ordinePanchina; // null per titolari, 1,2,3 per panchinari
        
        public CalciatoreInFormazione(Calciatore calciatore, boolean panchina, Integer ordinePanchina) {
            this.calciatore = calciatore;
            this.panchina = panchina;
            this.ordinePanchina = ordinePanchina;
        }
        
        // Getters/Setters
        public Calciatore getCalciatore() { return calciatore; }
        public boolean isPanchina() { return panchina; }
        public Integer getOrdinePanchina() { return ordinePanchina; }
    }
    
    public Formazione() {
        this.calciatori = new ArrayList<>();
    }
    
    public Formazione(String modulo, int idSquadra, int numeroGiornata) {
        this.modulo = modulo;
        this.idSquadra = idSquadra;
        this.numeroGiornata = numeroGiornata;
        this.calciatori = new ArrayList<>();
    }
    
    // Metodi utility
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
        // Verifica se la formazione rispetta il modulo
        int numTitolari = (int) calciatori.stream().filter(cf -> !cf.isPanchina()).count();
        return numTitolari == 11;
    }
    
    // Getters/Setters standard
    public int getIdFormazione() { return idFormazione; }
    public void setIdFormazione(int idFormazione) { this.idFormazione = idFormazione; }
    
    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    
    public int getIdSquadra() { return idSquadra; }
    public void setIdSquadra(int idSquadra) { this.idSquadra = idSquadra; }
    
    public int getNumeroGiornata() { return numeroGiornata; }
    public void setNumeroGiornata(int numeroGiornata) { this.numeroGiornata = numeroGiornata; }
}