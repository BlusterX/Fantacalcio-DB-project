package fantacalcio.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import fantacalcio.dao.CalciatoreDAO;
import fantacalcio.dao.FasciaAppartenenzaDAO;
import fantacalcio.dao.FasciaGiocatoreDAO;
import fantacalcio.dao.SquadraDAO;
import fantacalcio.dao.TipoCampionatoDAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.Squadra;
import fantacalcio.model.TipoCampionato;

public class DataPopulator {

    public enum CampionatoPredefinito {
        SERIE_A_24_25("Serie A", 2024),
        PREMIER_LEAGUE_24_25("Premier League", 2024);

        private final String nome;
        private final int anno;
        CampionatoPredefinito(String nome, int anno) { this.nome = nome; this.anno = anno; }
        public String getNome() { return nome; }
        public int getAnno() { return anno; }
    }

    private final SquadraDAO squadraDAO = new SquadraDAO();
    private final CalciatoreDAO calciatoreDAO = new CalciatoreDAO();
    private final TipoCampionatoDAO tipoCampionatoDAO = new TipoCampionatoDAO();
    private final FasciaGiocatoreDAO fasciaGiocatoreDAO = new FasciaGiocatoreDAO();
    private final FasciaAppartenenzaDAO fasciaAppDAO = new FasciaAppartenenzaDAO();

    /** Soglie costo → nome fascia (adattale come vuoi) */
    private static String nomeFasciaPerCosto(int costo) {
        if (costo >= 65) return "TOP";
        if (costo >= 50) return "SEMI-TOP";
        if (costo >= 35) return "TITOLARE";
        if (costo >= 26) return "ROTAZIONE";
        return "SCOMMESSA";
    }

    public void popolaSquadreECalciatoriDaCsv(CampionatoPredefinito campionato, String resourceCsv) {
        System.out.println("=== POPOLAMENTO SQUADRE + CALCIATORI DA CSV ===");
        System.out.println("Campionato: " + campionato.getNome() + " " + campionato.getAnno());
        System.out.println("CSV: " + resourceCsv);

        Integer idCampionato = ensureTipoCampionato(campionato.getNome(), campionato.getAnno());
        if (idCampionato == null) {
            System.err.println("Impossibile determinare ID_Campionato. Interrotto.");
            return;
        }

        // Precarico fasce in memoria (Nome_fascia -> ID_Fascia)
        Map<String,Integer> nomeFasciaToId = fasciaGiocatoreDAO.mappaNomeToId();
        if (nomeFasciaToId.isEmpty()) {
            System.err.println("ATTENZIONE: FASCIA_GIOCATORE è vuota. Inserisci prima le fasce!");
        }

        // 1) passata: raccogli nomi squadre
        Set<String> nomiSquadre = new LinkedHashSet<>();
        try (InputStream is = getResource(resourceCsv);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String header = reader.readLine();
            if (header == null) { System.err.println("CSV vuoto: " + resourceCsv); return; }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] campi = splitCsv(line);
                if (campi.length < 5) continue;
                String squadraNome = campi[3].trim();
                if (!squadraNome.isEmpty()) nomiSquadre.add(squadraNome);
            }
        } catch (Exception e) {
            System.err.println("Errore lettura CSV (passata 1): " + e.getMessage());
            return;
        }

        // 2) crea/mappa squadre del campionato
        Map<String, Integer> mappaSquadre = creaMappaSquadrePerCampionato(idCampionato);
        int createCount = 0, already = mappaSquadre.size();

        for (String nomeSquadra : nomiSquadre) {
            if (!mappaSquadre.containsKey(nomeSquadra)) {
                Squadra s = new Squadra(nomeSquadra, idCampionato, 0);
                if (squadraDAO.inserisciSquadra(s)) {
                    mappaSquadre.put(nomeSquadra, s.getIdSquadra());
                    createCount++;
                } else {
                    System.err.println("Impossibile inserire squadra: " + nomeSquadra);
                }
            }
        }
        System.out.println("Squadre esistenti: " + already + " | create ora: " + createCount +
                           " | totali: " + mappaSquadre.size());

        // 3) seconda passata: inserisci calciatori + assegna fascia
        Map<Integer, Integer> primoCalciatorePerSquadra = new LinkedHashMap<>();
        int inseriti = 0, saltati = 0, riga = 0;

        try (InputStream is = getResource(resourceCsv);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String header = reader.readLine();
            if (header == null) { System.err.println("CSV vuoto alla seconda passata."); return; }

            String line;
            while ((line = reader.readLine()) != null) {
                riga++;
                String[] campi = splitCsv(line);
                if (campi.length < 5) {
                    System.err.println("Riga " + riga + " non valida: " + line);
                    saltati++; continue;
                }

                String nome = campi[0].trim();
                String cognome = campi[1].trim();
                String ruoloStr = campi[2].trim();
                String nomeSquadra = campi[3].trim();
                String costoStr = campi[4].trim();

                try {
                    Calciatore.Ruolo ruolo = parseRuolo(ruoloStr);
                    int costo = Integer.parseInt(costoStr);

                    Integer idSquadra = mappaSquadre.get(nomeSquadra);
                    if (idSquadra == null) {
                        System.err.println("Riga " + riga + " - squadra '" + nomeSquadra + "' non mappata. Calciatore saltato.");
                        saltati++; continue;
                    }

                    Calciatore c = new Calciatore();
                    c.setNome(nome);
                    c.setCognome(cognome);
                    c.setRuolo(ruolo);
                    c.setCosto(costo);
                    c.setInfortunato(false);
                    c.setSqualificato(false);
                    c.setIdSquadra(idSquadra);

                    boolean ok = calciatoreDAO.inserisciCalciatore(c); // <-- SOLO una volta
                    if (ok) {
                        inseriti++;
                        primoCalciatorePerSquadra.putIfAbsent(idSquadra, c.getIdCalciatore());

                        // Assegna fascia in base al costo
                        String nomeFascia = nomeFasciaPerCosto(costo);
                        Integer idFascia = nomeFasciaToId.get(nomeFascia.toUpperCase());
                        if (idFascia != null) {
                            fasciaAppDAO.assegnaFasciaUnica(c.getIdCalciatore(), idFascia);
                        } else {
                            System.err.println("Fascia non trovata in FASCIA_GIOCATORE: " + nomeFascia);
                        }
                    } else {
                        saltati++;
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Riga " + riga + " - errore parsing/inserimento: " + ex.getMessage());
                    saltati++;
                }
            }
        } catch (Exception e) {
            System.err.println("Errore lettura CSV (passata 2): " + e.getMessage());
            return;
        }

        // 4) aggiorna ID_Calciatore di riferimento in SQUADRA (se ti serve)
        int aggiornate = 0;
        for (Map.Entry<Integer, Integer> e : primoCalciatorePerSquadra.entrySet()) {
            Integer idSquadra = e.getKey();
            Integer idCalciatore = e.getValue();
            Optional<Squadra> opt = squadraDAO.trovaSquadraPerId(idSquadra);
            if (opt.isPresent()) {
                Squadra s = opt.get();
                s.setIdCalciatore(idCalciatore);
                if (squadraDAO.aggiornaSquadra(s)) aggiornate++;
            }
        }

        System.out.println("=== FINE POPOLAMENTO ===");
        System.out.println("Calciatori inseriti: " + inseriti + " | Calciatori saltati: " + saltati);
        System.out.println("Squadre aggiornate con ID_Calciatore reale: " + aggiornate + "/" + mappaSquadre.size());
    }
    private Integer ensureTipoCampionato(String nome, int anno) {
        TipoCampionato tc = tipoCampionatoDAO.getOrCreate(nome, anno);
        return (tc != null) ? tc.getIdCampionato() : null;
    }
    private Map<String, Integer> creaMappaSquadrePerCampionato(int idCampionato) {
        List<Squadra> tutte = squadraDAO.trovaTutteLeSquadre();
        return tutte.stream()
                .filter(s -> s.getIdCampionato() == idCampionato)
                .collect(Collectors.toMap(
                        Squadra::getNome, Squadra::getIdSquadra, (a,b)->a, LinkedHashMap::new
                ));
    }
    private static Calciatore.Ruolo parseRuolo(String ruolostr) {
        return switch (ruolostr.trim().toUpperCase()) {
            case "P" -> Calciatore.Ruolo.PORTIERE;
            case "D" -> Calciatore.Ruolo.DIFENSORE;
            case "C" -> Calciatore.Ruolo.CENTROCAMPISTA;
            case "A" -> Calciatore.Ruolo.ATTACCANTE;
            default -> throw new IllegalArgumentException("Ruolo non valido: " + ruolostr);
        };
    }
    private static String[] splitCsv(String line) {
        String[] semi = line.split(";");
        if (semi.length >= 5) return semi;
        return line.split(",");
    }

    private static InputStream getResource(String resPath) {
        InputStream is = DataPopulator.class.getResourceAsStream(resPath);
        if (is == null) throw new IllegalArgumentException("Resource non trovata sul classpath: " + resPath);
        return is;
    }
}
