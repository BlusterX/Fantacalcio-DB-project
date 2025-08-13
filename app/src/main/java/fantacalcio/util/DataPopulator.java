package fantacalcio.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fantacalcio.dao.CalciatoreDAO;
import fantacalcio.dao.SquadraSerieADAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraSerieA;

/**
 * Classe dedicata al popolamento del database con dati realistici
 */
public class DataPopulator {
    
    private final SquadraSerieADAO squadraDAO;
    private final CalciatoreDAO calciatoreDAO;
    
    public DataPopulator() {
        this.squadraDAO = new SquadraSerieADAO();
        this.calciatoreDAO = new CalciatoreDAO();
    }
    
    /**
     * Popola squadre e calciatori da file CSV
     */
    public void popolaCompletamente() {
        System.out.println("=== POPOLAMENTO COMPLETO DATABASE ===");
        
        // 1. Popola squadre
        popolaSquadreSerieA();
        
        // 2. Popola calciatori da CSV
        popolaCalciatoriDaCSV();
        
        System.out.println("=== POPOLAMENTO COMPLETATO ===");
        stampaStatistiche();
    }
    
    /**
     * Popola solo le squadre
     */
    public void popolaSquadreSerieA() {
        System.out.println("Popolamento squadre Serie A...");
        squadraDAO.popolaSquadreSerieA();
    }
    
    /**
     * Popola calciatori da file CSV
     */
    public void popolaCalciatoriDaCSV() {
        System.out.println("Popolamento calciatori da CSV...");
        
        // Crea mappa squadre
        Map<String, Integer> mappaSquadre = creaMappaSquadre();
        if (mappaSquadre.isEmpty()) {
            System.err.println("Nessuna squadra trovata! Popola prima le squadre.");
            return;
        }
        
        // Leggi CSV calciatori
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("calciatori_serie_a.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                System.out.println("File CSV non trovato, uso dati hardcoded...");
                popolaCalciatoriFamosi(mappaSquadre);
                return;
            }
            
            String line;
            int lineNumber = 0;
            int inseriti = 0;
            
            // Salta header
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    String[] dati = line.split(",");
                    if (dati.length >= 5) {
                        String nome = dati[0].trim();
                        String cognome = dati[1].trim();
                        String ruoloStr = dati[2].trim();
                        String squadra = dati[3].trim();
                        int costo = Integer.parseInt(dati[4].trim());
                        
                        Calciatore.Ruolo ruolo = Calciatore.Ruolo.valueOf(ruoloStr.toUpperCase());
                        
                        if (inserisciCalciatoreSicuro(nome, cognome, ruolo, squadra, costo, mappaSquadre)) {
                            inseriti++;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Errore riga " + lineNumber + ": " + e.getMessage());
                }
            }
            
            System.out.println("Inseriti " + inseriti + " calciatori da CSV");
            
        } catch (Exception e) {
            System.err.println("Errore lettura CSV: " + e.getMessage());
            System.out.println("Fallback a dati hardcoded...");
            popolaCalciatoriFamosi(mappaSquadre);
        }
    }
    
    /**
     * Popola calciatori famosi (fallback se CSV non disponibile)
     */
    private void popolaCalciatoriFamosi(Map<String, Integer> mappaSquadre) {
        System.out.println("Popolamento calciatori famosi (hardcoded)...");
        
        // Top players per squadra
        CalciatoreInfo[] topPlayers = {
            // AC Milan
            new CalciatoreInfo("Mike", "Maignan", Calciatore.Ruolo.PORTIERE, "AC Milan", 35),
            new CalciatoreInfo("Theo", "Hernandez", Calciatore.Ruolo.DIFENSORE, "AC Milan", 55),
            new CalciatoreInfo("Rafael", "Leao", Calciatore.Ruolo.ATTACCANTE, "AC Milan", 80),
            new CalciatoreInfo("Olivier", "Giroud", Calciatore.Ruolo.ATTACCANTE, "AC Milan", 65),
            
            // Inter
            new CalciatoreInfo("Andre", "Onana", Calciatore.Ruolo.PORTIERE, "Inter", 40),
            new CalciatoreInfo("Alessandro", "Bastoni", Calciatore.Ruolo.DIFENSORE, "Inter", 60),
            new CalciatoreInfo("Nicolo", "Barella", Calciatore.Ruolo.CENTROCAMPISTA, "Inter", 70),
            new CalciatoreInfo("Lautaro", "Martinez", Calciatore.Ruolo.ATTACCANTE, "Inter", 85),
            new CalciatoreInfo("Marcus", "Thuram", Calciatore.Ruolo.ATTACCANTE, "Inter", 65),
            
            // Juventus
            new CalciatoreInfo("Wojciech", "Szczesny", Calciatore.Ruolo.PORTIERE, "Juventus", 35),
            new CalciatoreInfo("Gleison", "Bremer", Calciatore.Ruolo.DIFENSORE, "Juventus", 50),
            new CalciatoreInfo("Manuel", "Locatelli", Calciatore.Ruolo.CENTROCAMPISTA, "Juventus", 55),
            new CalciatoreInfo("Dusan", "Vlahovic", Calciatore.Ruolo.ATTACCANTE, "Juventus", 85),
            new CalciatoreInfo("Federico", "Chiesa", Calciatore.Ruolo.ATTACCANTE, "Juventus", 75),
            
            // Napoli
            new CalciatoreInfo("Alex", "Meret", Calciatore.Ruolo.PORTIERE, "Napoli", 30),
            new CalciatoreInfo("Kim", "Min-jae", Calciatore.Ruolo.DIFENSORE, "Napoli", 65),
            new CalciatoreInfo("Stanislav", "Lobotka", Calciatore.Ruolo.CENTROCAMPISTA, "Napoli", 55),
            new CalciatoreInfo("Khvicha", "Kvaratskhelia", Calciatore.Ruolo.ATTACCANTE, "Napoli", 80),
            new CalciatoreInfo("Victor", "Osimhen", Calciatore.Ruolo.ATTACCANTE, "Napoli", 90),
            
            // AS Roma
            new CalciatoreInfo("Rui", "Patricio", Calciatore.Ruolo.PORTIERE, "AS Roma", 25),
            new CalciatoreInfo("Gianluca", "Mancini", Calciatore.Ruolo.DIFENSORE, "AS Roma", 40),
            new CalciatoreInfo("Lorenzo", "Pellegrini", Calciatore.Ruolo.CENTROCAMPISTA, "AS Roma", 60),
            new CalciatoreInfo("Paulo", "Dybala", Calciatore.Ruolo.ATTACCANTE, "AS Roma", 75),
            
            // Lazio
            new CalciatoreInfo("Ivan", "Provedel", Calciatore.Ruolo.PORTIERE, "Lazio", 30),
            new CalciatoreInfo("Alessio", "Romagnoli", Calciatore.Ruolo.DIFENSORE, "Lazio", 35),
            new CalciatoreInfo("Sergej", "Milinkovic-Savic", Calciatore.Ruolo.CENTROCAMPISTA, "Lazio", 70),
            new CalciatoreInfo("Ciro", "Immobile", Calciatore.Ruolo.ATTACCANTE, "Lazio", 70),
            
            // Atalanta
            new CalciatoreInfo("Juan", "Musso", Calciatore.Ruolo.PORTIERE, "Atalanta", 35),
            new CalciatoreInfo("Giorgio", "Scalvini", Calciatore.Ruolo.DIFENSORE, "Atalanta", 45),
            new CalciatoreInfo("Teun", "Koopmeiners", Calciatore.Ruolo.CENTROCAMPISTA, "Atalanta", 60),
            new CalciatoreInfo("Ademola", "Lookman", Calciatore.Ruolo.ATTACCANTE, "Atalanta", 55),
            new CalciatoreInfo("Gianluca", "Scamacca", Calciatore.Ruolo.ATTACCANTE, "Atalanta", 60)
        };
        
        int inseriti = 0;
        for (CalciatoreInfo info : topPlayers) {
            if (inserisciCalciatoreSicuro(info.nome, info.cognome, info.ruolo, 
                                        info.squadra, info.costo, mappaSquadre)) {
                inseriti++;
            }
        }
        
        System.out.println("Inseriti " + inseriti + "/" + topPlayers.length + " calciatori famosi");
    }
    
    /**
     * Inserisce un calciatore con controllo sicurezza
     */
    private boolean inserisciCalciatoreSicuro(String nome, String cognome, Calciatore.Ruolo ruolo,
                                            String nomeSquadra, int costo, Map<String, Integer> mappaSquadre) {
        Integer idSquadra = mappaSquadre.get(nomeSquadra);
        if (idSquadra != null) {
            Calciatore calciatore = new Calciatore(nome, cognome, ruolo, costo);
            return calciatoreDAO.inserisciCalciatore(calciatore, idSquadra);
        } else {
            System.err.println("Squadra non trovata: " + nomeSquadra);
            return false;
        }
    }
    
    /**
     * Crea mappa nome squadra -> ID
     */
    private Map<String, Integer> creaMappaSquadre() {
        Map<String, Integer> mappa = new HashMap<>();
        List<SquadraSerieA> squadre = squadraDAO.trovaTutteLeSquadre();
        
        for (SquadraSerieA squadra : squadre) {
            mappa.put(squadra.getNome(), squadra.getIdSquadraA());
        }
        
        return mappa;
    }
    
    /**
     * Stampa statistiche finali
     */
    private void stampaStatistiche() {
        int numSquadre = squadraDAO.contaSquadre();
        Map<Calciatore.Ruolo, Integer> conteggioRuoli = calciatoreDAO.contaCalciatoriPerRuolo();
        int totalCalciatori = conteggioRuoli.values().stream().mapToInt(Integer::intValue).sum();
        
        System.out.println("\n📊 STATISTICHE DATABASE:");
        System.out.println("• Squadre: " + numSquadre);
        System.out.println("• Calciatori totali: " + totalCalciatori);
        System.out.println("  - Portieri: " + conteggioRuoli.getOrDefault(Calciatore.Ruolo.PORTIERE, 0));
        System.out.println("  - Difensori: " + conteggioRuoli.getOrDefault(Calciatore.Ruolo.DIFENSORE, 0));
        System.out.println("  - Centrocampisti: " + conteggioRuoli.getOrDefault(Calciatore.Ruolo.CENTROCAMPISTA, 0));
        System.out.println("  - Attaccanti: " + conteggioRuoli.getOrDefault(Calciatore.Ruolo.ATTACCANTE, 0));
    }
    
    /**
     * Classe helper per info calciatori
     */
    private static class CalciatoreInfo {
        final String nome;
        final String cognome;
        final Calciatore.Ruolo ruolo;
        final String squadra;
        final int costo;
        
        CalciatoreInfo(String nome, String cognome, Calciatore.Ruolo ruolo, String squadra, int costo) {
            this.nome = nome;
            this.cognome = cognome;
            this.ruolo = ruolo;
            this.squadra = squadra;
            this.costo = costo;
        }
    }
}