package fantacalcio.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import fantacalcio.dao.ClassificaDAO;
import fantacalcio.dao.FasciaAppartenenzaDAO;
import fantacalcio.dao.FormazioneDAO;
import fantacalcio.dao.PunteggioGiocatoreDAO;
import fantacalcio.dao.ScontroLegaDAO;
import fantacalcio.dao.VotoGiornataDAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.Calciatore.Ruolo;
import fantacalcio.model.FasciaGiocatore;
import fantacalcio.model.ScontroLega;
import fantacalcio.model.VotoGiornata;
import fantacalcio.util.DatabaseConnection;

public class MatchSimulationService {
    
    private static final int BM_GOL           = 1;
    private static final int BM_ASSIST        = 2;
    private static final int BM_AMMONIZIONE   = 3;
    private static final int BM_ESPULSIONE    = 4;
    private static final int BM_IMBATTIBILITA = 5;

    private final PunteggioGiocatoreDAO punteggioDAO = new PunteggioGiocatoreDAO();
    private final ScontroLegaDAO scontroDAO = new ScontroLegaDAO();
    private final FormazioneDAO formazioneDAO = new FormazioneDAO();
    private final VotoGiornataDAO votoDAO = new VotoGiornataDAO();
    private final PunteggioGiocatoreDAO eventiDAO = new PunteggioGiocatoreDAO();
    private final FasciaAppartenenzaDAO fasciaDAO = new FasciaAppartenenzaDAO();
    private final ClassificaDAO classificaDAO = new ClassificaDAO();

    private final Random rnd = new Random();
    private final double VAL_GOL, VAL_ASSIST, VAL_AMM, VAL_ESP, VAL_CS;

    public MatchSimulationService() {
        VAL_GOL    = eventiDAO.getValoreBonus(BM_GOL);           // +3.00
        VAL_ASSIST = eventiDAO.getValoreBonus(BM_ASSIST);        // +1.00
        VAL_AMM    = eventiDAO.getValoreBonus(BM_AMMONIZIONE);   // -0.50
        VAL_ESP    = eventiDAO.getValoreBonus(BM_ESPULSIONE);    // -1.00
        VAL_CS     = eventiDAO.getValoreBonus(BM_IMBATTIBILITA); // +1.00
    }

    private static double rnd1(double v){ return Math.round(v*10.0)/10.0; }

    public void simulaGiornata(int idLega, int numeroGiornata) {
        List<ScontroLega> scontri = scontroDAO.trovaScontriGiornata(idLega, numeroGiornata);
        for (ScontroLega s : scontri) {
            if (s.getStato() == ScontroLega.StatoScontro.PROGRAMMATO
            || s.getStato() == ScontroLega.StatoScontro.IN_CORSO) {
                simulaScontro(s, numeroGiornata);
            }
        }
        if (!scontroDAO.esistonoScontriDaGiocare(idLega)) {
            scontroDAO.setStatoLega(idLega, "TERMINATA");
        }
    }

    public void simulaTuttaLaLega(int idLega) {
        int max = scontroDAO.getMaxGiornatePublic(idLega);
        if (max <= 0) return;

        for (int g = 1; g <= max; g++) {
            var scontri = scontroDAO.trovaScontriGiornata(idLega, g);
            if (scontri == null || scontri.isEmpty()) continue;

            boolean daGiocare = scontri.stream().anyMatch(s ->
                s.getStato() == ScontroLega.StatoScontro.PROGRAMMATO
            || s.getStato() == ScontroLega.StatoScontro.IN_CORSO);

            if (daGiocare) simulaGiornata(idLega, g);
        }
        if (!scontroDAO.esistonoScontriDaGiocare(idLega)) {
            scontroDAO.setStatoLega(idLega, "TERMINATA");
        }
    }

    


    private void simulaScontro(ScontroLega s, int giornata) {
        simulaFormazione(s.getIdFormazione1(), giornata);
        simulaFormazione(s.getIdFormazione2(), giornata);

        double punti1 = punteggioDAO.calcolaTotaleFantapuntiFormazione(s.getIdFormazione1(), giornata);
        double punti2 = punteggioDAO.calcolaTotaleFantapuntiFormazione(s.getIdFormazione2(), giornata);

        punti1 = rnd1(punti1); punti2 = rnd1(punti2);

        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            formazioneDAO.aggiornaPunteggio(c, s.getIdFormazione1(), punti1);
            formazioneDAO.aggiornaPunteggio(c, s.getIdFormazione2(), punti2);
        } catch (SQLException ex) {
            System.err.println("Update punteggio formazione: " + ex.getMessage());
        }
        scontroDAO.aggiornaRisultato(s.getIdScontro(), punti1, punti2);
        aggiornaClassificaPerScontro(s, punti1, punti2);
    }

    private void aggiornaClassificaPerScontro(ScontroLega s, double p1, double p2) {
        Integer idSq1 = formazioneDAO.getSquadraByFormazione(s.getIdFormazione1());
        Integer idSq2 = formazioneDAO.getSquadraByFormazione(s.getIdFormazione2());
        if (idSq1 == null || idSq2 == null) return;

        Integer idC1 = classificaDAO.ensureIdClassificaPerSquadra(idSq1);
        Integer idC2 = classificaDAO.ensureIdClassificaPerSquadra(idSq2);

        int g1 = puntiToGolForClassifica(p1);
        int g2 = puntiToGolForClassifica(p2);

        if (g1 > g2) {
            classificaDAO.registraVittoria(idC1, p1, p2);
            classificaDAO.registraSconfitta(idC2, p2, p1);
        } else if (g1 < g2) {
            classificaDAO.registraSconfitta(idC1, p1, p2);
            classificaDAO.registraVittoria(idC2, p2, p1);
        } else {
            classificaDAO.registraPareggio(idC1, p1, p2);
            classificaDAO.registraPareggio(idC2, p2, p1);
        }
    }

    // Stessa logica usata per scrivere lo "x-y", qui solo per stabilire W/D/L
    private static int puntiToGolForClassifica(double punti) {
        if (punti < 66.0) return 0;
        return 1 + (int) Math.floor((punti - 66.0) / 6.0);
    }




    /** Simula i titolari, scrive voti+eventi per ciascuno e ritorna la somma fantapunti. */
    private double simulaFormazione(int idFormazione, int giornata) {
        List<Calciatore> titolari = formazioneDAO.trovaTitolari(idFormazione);
        if (titolari == null || titolari.isEmpty()) return 0.0;

        double totale = 0.0;
        boolean portaInviolataTeam = rnd.nextDouble() < 0.35;

        for (Calciatore c : titolari) {
            FasciaGiocatore fg = fasciaDAO.trovaFasciaPerCalciatore(c.getIdCalciatore())
                    .orElseGet(() -> {
                        FasciaGiocatore def = new FasciaGiocatore();
                        def.setVotoBaseStandard(6.0);
                        def.setProbGolAttaccante(0.18);
                        def.setProbGolCentrocampista(0.10);
                        def.setProbGolDifensore(0.03);
                        def.setProbAssist(0.12);
                        def.setProbAmmonizione(0.22);
                        def.setProbEspulsione(0.03);
                        def.setProbImbattibilita(0.25);
                        return def;
                    });

            PlayerPerf p = simulaPrestazione(c, fg, portaInviolataTeam);

            // reset & write via DAO
            votoDAO.cancellaVoto(c.getIdCalciatore(), giornata);
            eventiDAO.cancellaEventi(c.getIdCalciatore(), giornata);

            VotoGiornata voto = new VotoGiornata();
            voto.setIdCalciatore(c.getIdCalciatore());
            voto.setNumeroGiornata(giornata);
            voto.setVotoBase(p.votoBase);
            votoDAO.inserisciVoto(voto);

            if (p.gol    > 0) eventiDAO.inserisciEvento(c.getIdCalciatore(), BM_GOL,           giornata, p.gol);
            if (p.assist > 0) eventiDAO.inserisciEvento(c.getIdCalciatore(), BM_ASSIST,        giornata, p.assist);
            if (p.amm    > 0) eventiDAO.inserisciEvento(c.getIdCalciatore(), BM_AMMONIZIONE,   giornata, p.amm);
            if (p.esp    > 0) eventiDAO.inserisciEvento(c.getIdCalciatore(), BM_ESPULSIONE,    giornata, p.esp);
            if (c.getRuolo()==Ruolo.PORTIERE && p.cs)
                               eventiDAO.inserisciEvento(c.getIdCalciatore(), BM_IMBATTIBILITA, giornata, 1);

            totale += calcFantavoto(p, c.getRuolo());
        }
        return Math.round(totale * 10.0) / 10.0;
    }

    private PlayerPerf simulaPrestazione(Calciatore c, FasciaGiocatore fg, boolean csTeam) {
        double votoBase = clamp(fg.getVotoBaseStandard() + rnd.nextGaussian()*0.6, 4.5, 9.5);

        int gol=0, assist=0, amm=0, esp=0; boolean cs=false;
        double pGol = switch (c.getRuolo()) {
            case ATTACCANTE     -> fg.getProbGolAttaccante();
            case CENTROCAMPISTA -> fg.getProbGolCentrocampista();
            case DIFENSORE      -> fg.getProbGolDifensore();
            case PORTIERE       -> 0.0;
        };

        if (rnd.nextDouble() < pGol)                  gol++;
        if (rnd.nextDouble() < fg.getProbAssist())    assist++;
        if (rnd.nextDouble() < fg.getProbAmmonizione()) amm++;
        if (rnd.nextDouble() < fg.getProbEspulsione())  esp++;
        if (c.getRuolo()==Ruolo.PORTIERE && csTeam && rnd.nextDouble() < fg.getProbImbattibilita()) cs = true;

        return new PlayerPerf(votoBase, gol, assist, amm, esp, cs);
    }

    private void ensureXIOrCopyPrevious(int idFormazione, int giornata) {
        // se già 11 titolari -> ok
        if (formazioneDAO.countTitolari(idFormazione) == 11) return;

        // prendi squadra e ultima formazione completa < giornata
        Integer idSquadra = formazioneDAO.getSquadraByFormazione(idFormazione);
        Integer prevForm = formazioneDAO.findLastFormazioneCompleta(idSquadra, giornata);
        if (prevForm != null) {
            formazioneDAO.copiaXI(prevForm, idFormazione);  // copia 11 titolari
        }
        // (opzionale) else: auto-pick 11 dalla rosa con regole per ruolo
    }


    private double calcFantavoto(PlayerPerf p, Ruolo ruolo) {
        double fv = p.votoBase + p.gol*3 + p.assist*1 - p.amm*0.5 - p.esp*3;
        if (ruolo==Ruolo.PORTIERE && p.cs) fv += 1;
        return Math.max(1.0, fv);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private record PlayerPerf(double votoBase, int gol, int assist, int amm, int esp, boolean cs) {}
}
