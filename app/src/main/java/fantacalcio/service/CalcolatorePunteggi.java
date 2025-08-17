package fantacalcio.service;

import java.util.List;

import fantacalcio.dao.FormazioneDAO;
import fantacalcio.dao.PunteggioGiocatoreDAO;
import fantacalcio.dao.VotoGiornataDAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.Formazione;
import fantacalcio.model.PunteggioGiocatore;
import fantacalcio.model.VotoGiornata;

public class CalcolatorePunteggi {

    private final FormazioneDAO formazioneDAO = new FormazioneDAO();
    private final VotoGiornataDAO votoGiornataDAO = new VotoGiornataDAO();
    private final PunteggioGiocatoreDAO punteggioGiocatoreDAO = new PunteggioGiocatoreDAO();

    /**
     * Calcola il fantavoto di un singolo calciatore
     */
    public double calcolaFantavotoCalciatore(int idCalciatore, int numeroGiornata) {
        VotoGiornata voto = votoGiornataDAO.trovaVoto(idCalciatore, numeroGiornata);
        if (voto == null) return 0;

        double fantavoto = voto.getVotoBase();

        List<PunteggioGiocatore> eventi = punteggioGiocatoreDAO.trovaEventiCalciatore(idCalciatore, numeroGiornata);
        for(PunteggioGiocatore e : eventi) {
            double valoreBonus = punteggioGiocatoreDAO.getValoreBonus(e.getIdBonusMalus());
            fantavoto += valoreBonus * e.getQuantita();
        }
        return fantavoto;
    }

    /**
     * Calcola il punteggio totale di una formazione
     */
    public double calcolaPunteggioFormazione(int idFormazione) {
        Formazione formazione = formazioneDAO.getFormazioneById(idFormazione);
        if (formazione == null) return 0;

        double totale = 0;
        List<Calciatore> titolari = formazione.getTitolari();
        for (Calciatore c : titolari) {
            totale += calcolaFantavotoCalciatore(c.getIdCalciatore(),
                                     formazione.getNumeroGiornata());
        }
        return totale;
    }
}
