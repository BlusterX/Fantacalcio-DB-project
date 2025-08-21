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
import fantacalcio.dao.SquadraDAO;
import fantacalcio.dao.TipoCampionatoDAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.Squadra;
import fantacalcio.model.TipoCampionato;

/**
 * Popola DB da CSV:
 * - garantisce/crea TIPO_CAMPIONATO
 * - inserisce SQUADRA con ID_Calciatore=0 (placeholder)
 * - inserisce CALCIATORE con riferimento alla SQUADRA
 * - aggiorna SQUADRA.ID_Calciatore con un ID reale della stessa squadra
 *
 * CSV attesi in resources root:
 *   "/calciatori_serie_a.csv"
 *   "/calciatori_premier_league.csv"
 *
 * Colonne: Nome,Cognome,Ruolo,Squadra,Costo
 */
public class DataPopulator {

    public enum CampionatoPredefinito {
        SERIE_A_24_25("Serie A", 2024),
        PREMIER_LEAGUE_24_25("Premier League", 2024);

        private final String nome;
        private final int anno;
        CampionatoPredefinito(String nome, int anno) {
            this.nome = nome;
            this.anno = anno;
        }
        public String getNome() { return nome; }
        public int getAnno() { return anno; }
    }

    private final SquadraDAO squadraDAO = new SquadraDAO();
    private final CalciatoreDAO calciatoreDAO = new CalciatoreDAO();
    private final TipoCampionatoDAO tipoCampionatoDAO = new TipoCampionatoDAO();

    /**
     * Popola SQUADRA e CALCIATORE per il campionato selezionato leggendo il CSV.
     * @param campionato   SERIE_A_24_25 o PREMIER_LEAGUE_24_25
     * @param resourceCsv  "/calciatori_serie_a.csv" oppure "/calciatori_premier_league.csv"
     */
    public void popolaSquadreECalciatoriDaCsv(CampionatoPredefinito campionato, String resourceCsv) {
        System.out.println("=== POPOLAMENTO SQUADRE + CALCIATORI DA CSV ===");
        System.out.println("Campionato: " + campionato.getNome() + " " + campionato.getAnno());
        System.out.println("CSV: " + resourceCsv);

        // 1) ensure TipoCampionato (SOLO getOrCreate, non usiamo altri metodi)
        Integer idCampionato = ensureTipoCampionato(campionato.getNome(), campionato.getAnno());
        if (idCampionato == null) {
            System.err.println("Impossibile determinare ID_Campionato. Interrotto.");
            return;
        }

        // 2) Prima passata: raccogliere l'elenco delle squadre dal CSV
        Set<String> nomiSquadre = new LinkedHashSet<>();
        try (InputStream is = getResource(resourceCsv);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String header = reader.readLine();
            if (header == null) {
                System.err.println("CSV vuoto: " + resourceCsv);
                return;
            }

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

        // 3) Creare/mappare le squadre del campionato ESISTENTI
        Map<String, Integer> mappaSquadre = creaMappaSquadrePerCampionato(idCampionato);
        int createCount = 0, already = mappaSquadre.size();

        // Inserisci squadre mancanti usando SOLO inserisciSquadra(...) del tuo DAO
        for (String nomeSquadra : nomiSquadre) {
            if (!mappaSquadre.containsKey(nomeSquadra)) {
                // NOTA: usiamo 0 come placeholder per ID_Calciatore (coerente con il tuo modello int non-null)
                Squadra s = new Squadra(nomeSquadra, idCampionato, 0);
                boolean ok = squadraDAO.inserisciSquadra(s);
                if (ok) {
                    mappaSquadre.put(nomeSquadra, s.getIdSquadra());
                    createCount++;
                } else {
                    System.err.println("Impossibile inserire squadra: " + nomeSquadra);
                }
            }
        }
        System.out.println("Squadre esistenti: " + already + " | create ora: " + createCount + " | totali: " + mappaSquadre.size());

        // 4) Seconda passata: inserire i calciatori e ricordare il primo ID per ogni squadra
        Map<Integer, Integer> primoCalciatorePerSquadra = new LinkedHashMap<>(); // ID_Squadra -> ID_Calciatore (primo visto)
        int inseriti = 0, saltati = 0, riga = 0;

        try (InputStream is = getResource(resourceCsv);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String header = reader.readLine();
            if (header == null) {
                System.err.println("CSV vuoto alla seconda passata.");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                riga++;
                String[] campi = splitCsv(line);
                if (campi.length < 5) {
                    System.err.println("Riga " + riga + " non valida: " + line);
                    saltati++;
                    continue;
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
                        saltati++;
                        continue;
                    }

                    Calciatore c = new Calciatore();
                    c.setNome(nome); 
                    c.setCognome(cognome);
                    c.setRuolo(ruolo); 
                    c.setCosto(costo);
                    c.setInfortunato(false); 
                    c.setSqualificato(false);
                    c.setIdSquadra(idSquadra);
                    calciatoreDAO.inserisciCalciatore(c);
                    boolean ok = calciatoreDAO.inserisciCalciatore(c);
                    if (ok) {
                        inseriti++;
                        primoCalciatorePerSquadra.putIfAbsent(idSquadra, c.getIdCalciatore());
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

        // 5) Aggiornare le squadre: rimpiazza il placeholder 0 con un calciatore reale
        int aggiornate = 0;
        for (Map.Entry<Integer, Integer> e : primoCalciatorePerSquadra.entrySet()) {
            Integer idSquadra = e.getKey();
            Integer idCalciatore = e.getValue();

            Optional<Squadra> opt = squadraDAO.trovaSquadraPerId(idSquadra);
            if (opt.isPresent()) {
                Squadra s = opt.get();
                s.setIdCalciatore(idCalciatore);
                if (squadraDAO.aggiornaSquadra(s)) {
                    aggiornate++;
                }
            }
        }

        System.out.println("=== FINE POPOLAMENTO ===");
        System.out.println("Calciatori inseriti: " + inseriti + " | Calciatori saltati: " + saltati);
        System.out.println("Squadre aggiornate con ID_Calciatore reale: " + aggiornate + "/" + mappaSquadre.size());
    }

    // ----------------- Helper -----------------

    private Integer ensureTipoCampionato(String nome, int anno) {
        // Usa SOLO getOrCreate del tuo DAO
        TipoCampionato tc = tipoCampionatoDAO.getOrCreate(nome, anno);
        return (tc != null) ? tc.getIdCampionato() : null;
    }

    private Map<String, Integer> creaMappaSquadrePerCampionato(int idCampionato) {
        List<Squadra> tutte = squadraDAO.trovaTutteLeSquadre();
        return tutte.stream()
                .filter(s -> s.getIdCampionato() == idCampionato)
                .collect(Collectors.toMap(
                        Squadra::getNome,
                        Squadra::getIdSquadra,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private static Calciatore.Ruolo parseRuolo(String ruolostr) {
        String r = ruolostr.trim().toUpperCase();
        switch (r) {
            case "P": return Calciatore.Ruolo.PORTIERE;
            case "D": return Calciatore.Ruolo.DIFENSORE;
            case "C": return Calciatore.Ruolo.CENTROCAMPISTA;
            case "A": return Calciatore.Ruolo.ATTACCANTE;
            default: throw new IllegalArgumentException("Ruolo non valido: " + ruolostr);
        }
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
