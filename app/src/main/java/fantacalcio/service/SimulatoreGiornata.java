package fantacalcio.service;

import java.util.List;

import fantacalcio.dao.CalciatoreDAO;
import fantacalcio.dao.FormazioneDAO;
import fantacalcio.dao.PunteggioGiocatoreDAO;
import fantacalcio.dao.VotoGiornataDAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.Calciatore.Ruolo;
import fantacalcio.model.VotoGiornata;

public class SimulatoreGiornata {
    private final CalciatoreDAO calciatoreDAO = new CalciatoreDAO();
    private final FormazioneDAO formazioneDAO = new FormazioneDAO();
    private final VotoGiornataDAO votoGiornataDAO = new VotoGiornataDAO();
    private final PunteggioGiocatoreDAO punteggioGiocatoreDAO = new PunteggioGiocatoreDAO();
    /**
     * Simula una giornata completa
     */
    public void simulaGiornata(int numeroGiornata) {
        List<Calciatore> calciatori = calciatoreDAO.trovaTuttiICalciatori();
        for (Calciatore calciatore : calciatori) {
            generaVotoCalciatore(calciatore, numeroGiornata);
        }
    }

    private void generaVotoCalciatore(Calciatore calciatore, int numeroGiornata) {

        // Generazione voto base (randomica per simulazione)
        double votoBase = 6.0 + Math.random() * 2.5;
        votoBase = Math.max(4.5, Math.min(10.0, votoBase));

        // Simulazione eventi
        int gol = simulaEvento(0.20);
        int assist = simulaEvento(0.15);
        int ammonizioni = simulaEvento(0.15);
        int rigoriParati = (calciatore.getRuolo() == Ruolo.PORTIERE) ? simulaEvento(0.05) : 0;

        // Salva il VOTO_GIORNATA
        VotoGiornata voto = new VotoGiornata();
        voto.setIdCalciatore(calciatore.getIdCalciatore());
        voto.setNumeroGiornata(numeroGiornata);
        voto.setVotoBase(votoBase);
        voto.setMinutiGiocati(90);

        votoGiornataDAO.inserisciVoto(voto);

        // Inserisci gli EVENTI nella tabella Punteggio_Giocatore
        if (gol > 0)
            punteggioGiocatoreDAO.inserisciEvento(calciatore.getIdCalciatore(), 1, numeroGiornata, gol); // 1=ID_GOL
        if (assist > 0)
            punteggioGiocatoreDAO.inserisciEvento(calciatore.getIdCalciatore(), 2, numeroGiornata, assist);
        if (ammonizioni > 0)
            punteggioGiocatoreDAO.inserisciEvento(calciatore.getIdCalciatore(), 4, numeroGiornata, ammonizioni);
        if (rigoriParati > 0)
            punteggioGiocatoreDAO.inserisciEvento(calciatore.getIdCalciatore(), 7, numeroGiornata, rigoriParati);
    }

    private int simulaEvento(double prob) {
        return Math.random() < prob ? 1 : 0;
    }
}
