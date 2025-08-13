package fantacalcio.model;

import java.util.Objects;

/**
 * Modello per i voti dei calciatori in ogni giornata
 */
public class VotoGiornata {
    
    private int idVoto;
    private int idCalciatore;
    private int numeroGiornata;
    private double votoBase;
    private int minutiGiocati;
    private int gol;
    private int assist;
    private int ammonizioni;
    private int espulsioni;
    private int rigoriParati;
    private int autogol;
    private double fantavoto; // Calcolato automaticamente
    
    // Costruttori
    public VotoGiornata() {}
    
    public VotoGiornata(int idCalciatore, int numeroGiornata, double votoBase) {
        this.idCalciatore = idCalciatore;
        this.numeroGiornata = numeroGiornata;
        this.votoBase = votoBase;
        this.minutiGiocati = 90;
        this.gol = 0;
        this.assist = 0;
        this.ammonizioni = 0;
        this.espulsioni = 0;
        this.rigoriParati = 0;
        this.autogol = 0;
        calcolaFantavoto();
    }
    
    // Costruttore completo
    public VotoGiornata(int idVoto, int idCalciatore, int numeroGiornata, double votoBase,
                       int minutiGiocati, int gol, int assist, int ammonizioni,
                       int espulsioni, int rigoriParati, int autogol) {
        this.idVoto = idVoto;
        this.idCalciatore = idCalciatore;
        this.numeroGiornata = numeroGiornata;
        this.votoBase = votoBase;
        this.minutiGiocati = minutiGiocati;
        this.gol = gol;
        this.assist = assist;
        this.ammonizioni = ammonizioni;
        this.espulsioni = espulsioni;
        this.rigoriParati = rigoriParati;
        this.autogol = autogol;
        calcolaFantavoto();
    }
    
    // Getters e Setters
    public int getIdVoto() {
        return idVoto;
    }
    
    public void setIdVoto(int idVoto) {
        this.idVoto = idVoto;
    }
    
    public int getIdCalciatore() {
        return idCalciatore;
    }
    
    public void setIdCalciatore(int idCalciatore) {
        this.idCalciatore = idCalciatore;
    }
    
    public int getNumeroGiornata() {
        return numeroGiornata;
    }
    
    public void setNumeroGiornata(int numeroGiornata) {
        this.numeroGiornata = numeroGiornata;
    }
    
    public double getVotoBase() {
        return votoBase;
    }
    
    public void setVotoBase(double votoBase) {
        this.votoBase = votoBase;
        calcolaFantavoto(); // Ricalcola quando cambia il voto base
    }
    
    public int getMinutiGiocati() {
        return minutiGiocati;
    }
    
    public void setMinutiGiocati(int minutiGiocati) {
        this.minutiGiocati = minutiGiocati;
    }
    
    public int getGol() {
        return gol;
    }
    
    public void setGol(int gol) {
        this.gol = gol;
        calcolaFantavoto();
    }
    
    public int getAssist() {
        return assist;
    }
    
    public void setAssist(int assist) {
        this.assist = assist;
        calcolaFantavoto();
    }
    
    public int getAmmonizioni() {
        return ammonizioni;
    }
    
    public void setAmmonizioni(int ammonizioni) {
        this.ammonizioni = ammonizioni;
        calcolaFantavoto();
    }
    
    public int getEspulsioni() {
        return espulsioni;
    }
    
    public void setEspulsioni(int espulsioni) {
        this.espulsioni = espulsioni;
        calcolaFantavoto();
    }
    
    public int getRigoriParati() {
        return rigoriParati;
    }
    
    public void setRigoriParati(int rigoriParati) {
        this.rigoriParati = rigoriParati;
        calcolaFantavoto();
    }
    
    public int getAutogol() {
        return autogol;
    }
    
    public void setAutogol(int autogol) {
        this.autogol = autogol;
        calcolaFantavoto();
    }
    
    public double getFantavoto() {
        return fantavoto;
    }
    
    public void setFantavoto(double fantavoto) {
        this.fantavoto = fantavoto;
    }
    
    // Metodi di utilità
    
    /**
     * Calcola il fantavoto secondo le regole standard
     */
    private void calcolaFantavoto() {
        this.fantavoto = votoBase + (gol * 3) + assist - (ammonizioni * 0.5) - espulsioni + rigoriParati - (autogol * 2);
    }
    
    /**
     * Verifica se il giocatore ha giocato (almeno 1 minuto)
     */
    public boolean haGiocato() {
        return minutiGiocati > 0;
    }
    
    /**
     * Verifica se è stato un'ottima prestazione (fantavoto >= 7.5)
     */
    public boolean isPrestazioneBuona() {
        return fantavoto >= 7.5;
    }
    
    /**
     * Verifica se è stata una prestazione negativa (fantavoto < 6.0)
     */
    public boolean isPrestazioneNegativa() {
        return fantavoto < 6.0;
    }
    
    /**
     * Restituisce una descrizione della prestazione
     */
    public String getDescrizionePrestazione() {
        StringBuilder sb = new StringBuilder();
        
        if (gol > 0) sb.append(gol).append(" gol ");
        if (assist > 0) sb.append(assist).append(" assist ");
        if (rigoriParati > 0) sb.append(rigoriParati).append(" rigori parati ");
        if (ammonizioni > 0) sb.append(ammonizioni).append(" ammonizioni ");
        if (espulsioni > 0) sb.append("espulso ");
        if (autogol > 0) sb.append(autogol).append(" autogol ");
        
        if (sb.length() == 0) {
            return "Prestazione normale";
        }
        
        return sb.toString().trim();
    }
    
    // equals, hashCode e toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VotoGiornata that = (VotoGiornata) o;
        return idCalciatore == that.idCalciatore && numeroGiornata == that.numeroGiornata;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idCalciatore, numeroGiornata);
    }
    
    @Override
    public String toString() {
        return String.format("VotoGiornata{calciatore=%d, giornata=%d, voto=%.1f, fantavoto=%.1f}", 
                           idCalciatore, numeroGiornata, votoBase, fantavoto);
    }
}