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
                        
                        Calciatore.Ruolo ruolo = switch (ruoloStr.toUpperCase()) {
                            case "P" -> Calciatore.Ruolo.PORTIERE;
                            case "D" -> Calciatore.Ruolo.DIFENSORE;
                            case "C" -> Calciatore.Ruolo.CENTROCAMPISTA;
                            case "A" -> Calciatore.Ruolo.ATTACCANTE;
                            default  -> throw new IllegalArgumentException("Ruolo non valido: " + ruoloStr);
                        };
                        
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
            new CalciatoreInfo("Mike", "Maignan", Calciatore.Ruolo.PORTIERE, "AC Milan", 60),
            new CalciatoreInfo("Theo", "Hernandez", Calciatore.Ruolo.DIFENSORE, "AC Milan", 60),
            new CalciatoreInfo("Fikayo", "Tomori", Calciatore.Ruolo.DIFENSORE, "AC Milan", 50),
            new CalciatoreInfo("Davide", "Calabria", Calciatore.Ruolo.DIFENSORE, "AC Milan", 35),
            new CalciatoreInfo("Ruben", "Loftus-Cheek", Calciatore.Ruolo.CENTROCAMPISTA, "AC Milan", 50),
            new CalciatoreInfo("Ismael", "Bennacer", Calciatore.Ruolo.CENTROCAMPISTA, "AC Milan", 50),
            new CalciatoreInfo("Samuel", "Chukwueze", Calciatore.Ruolo.ATTACCANTE, "AC Milan", 50),
            new CalciatoreInfo("Rafael", "Leao", Calciatore.Ruolo.ATTACCANTE, "AC Milan", 80),
            new CalciatoreInfo("Olivier", "Giroud", Calciatore.Ruolo.ATTACCANTE, "AC Milan", 65),
            new CalciatoreInfo("Christian", "Pulisic", Calciatore.Ruolo.ATTACCANTE, "AC Milan", 50),
            new CalciatoreInfo("Malick","Thiaw",Calciatore.Ruolo.DIFENSORE,"AC Milan",35),
            new CalciatoreInfo("Tijjani","Reijnders",Calciatore.Ruolo.CENTROCAMPISTA,"AC Milan",40),
            new CalciatoreInfo("Rade","Krunic",Calciatore.Ruolo.CENTROCAMPISTA,"AC Milan",35),
            new CalciatoreInfo("Noah","Okafor",Calciatore.Ruolo.ATTACCANTE,"AC Milan",35),
            new CalciatoreInfo("Alessandro","Florenzi",Calciatore.Ruolo.DIFENSORE,"AC Milan",30),
            // Inter
            new CalciatoreInfo("Yann", "Sommer", Calciatore.Ruolo.PORTIERE, "Inter", 60),
            new CalciatoreInfo("Benjamin", "Pavard", Calciatore.Ruolo.DIFENSORE, "Inter", 60),
            new CalciatoreInfo("Francesco", "Acerbi", Calciatore.Ruolo.DIFENSORE, "Inter", 35),
            new CalciatoreInfo("Alessandro", "Bastoni", Calciatore.Ruolo.DIFENSORE, "Inter", 60),
            new CalciatoreInfo("Federico", "Dimarco", Calciatore.Ruolo.DIFENSORE, "Inter", 60),
            new CalciatoreInfo("Nicolo", "Barella", Calciatore.Ruolo.CENTROCAMPISTA, "Inter", 70),
            new CalciatoreInfo("Davide", "Frattesi", Calciatore.Ruolo.CENTROCAMPISTA, "Inter", 50),
            new CalciatoreInfo("Hakan", "Calhanoglu", Calciatore.Ruolo.CENTROCAMPISTA, "Inter", 60),
            new CalciatoreInfo("Lautaro", "Martinez", Calciatore.Ruolo.ATTACCANTE, "Inter", 85),
            new CalciatoreInfo("Marcus", "Thuram", Calciatore.Ruolo.ATTACCANTE, "Inter", 65),
            new CalciatoreInfo("Denzel","Dumfries",Calciatore.Ruolo.DIFENSORE,"Inter",50),
            new CalciatoreInfo("Henrikh","Mkhitaryan",Calciatore.Ruolo.CENTROCAMPISTA,"Inter",40),
            new CalciatoreInfo("Stefan","de Vrij",Calciatore.Ruolo.DIFENSORE,"Inter",35),
            new CalciatoreInfo("Juan","Cuadrado",Calciatore.Ruolo.ATTACCANTE,"Inter",30),
            new CalciatoreInfo("Alexis","Sánchez",Calciatore.Ruolo.ATTACCANTE,"Inter",30),

            // Juventus
            new CalciatoreInfo("Wojciech", "Szczesny", Calciatore.Ruolo.PORTIERE, "Juventus", 60),
            new CalciatoreInfo("Gleison", "Bremer", Calciatore.Ruolo.DIFENSORE, "Juventus", 50),
            new CalciatoreInfo("Federico", "Gatti", Calciatore.Ruolo.DIFENSORE, "Juventus", 35),
            new CalciatoreInfo("Manuel", "Locatelli", Calciatore.Ruolo.CENTROCAMPISTA, "Juventus", 55),
            new CalciatoreInfo("Adrien", "Rabiot", Calciatore.Ruolo.CENTROCAMPISTA, "Juventus", 50),
            new CalciatoreInfo("Federico", "Chiesa", Calciatore.Ruolo.ATTACCANTE, "Juventus", 75),
            new CalciatoreInfo("Dusan", "Vlahovic", Calciatore.Ruolo.ATTACCANTE, "Juventus", 85),
            new CalciatoreInfo("Moise", "Kean", Calciatore.Ruolo.ATTACCANTE, "Juventus", 40),
            new CalciatoreInfo("Danilo","Luiz",Calciatore.Ruolo.DIFENSORE,"Juventus",45),
            new CalciatoreInfo("Andrea","Cambiaso",Calciatore.Ruolo.DIFENSORE,"Juventus",30),
            new CalciatoreInfo("Timothy","Weah",Calciatore.Ruolo.ATTACCANTE,"Juventus",30),
            new CalciatoreInfo("Weston","McKennie",Calciatore.Ruolo.CENTROCAMPISTA,"Juventus",35),
            new CalciatoreInfo("Arkadiusz","Milik",Calciatore.Ruolo.ATTACCANTE,"Juventus",40),
            // Napoli
            new CalciatoreInfo("Alex", "Meret", Calciatore.Ruolo.PORTIERE, "Napoli", 35),
            new CalciatoreInfo("Giovanni", "Di Lorenzo", Calciatore.Ruolo.DIFENSORE, "Napoli", 50),
            new CalciatoreInfo("Amir", "Rrahmani", Calciatore.Ruolo.DIFENSORE, "Napoli", 35),
            new CalciatoreInfo("Stanislav", "Lobotka", Calciatore.Ruolo.CENTROCAMPISTA, "Napoli", 55),
            new CalciatoreInfo("Piotr", "Zielinski", Calciatore.Ruolo.CENTROCAMPISTA, "Napoli", 50),
            new CalciatoreInfo("Khvicha", "Kvaratskhelia", Calciatore.Ruolo.ATTACCANTE, "Napoli", 80),
            new CalciatoreInfo("Victor", "Osimhen", Calciatore.Ruolo.ATTACCANTE, "Napoli", 90),
            new CalciatoreInfo("Giacomo", "Raspadori", Calciatore.Ruolo.ATTACCANTE, "Napoli", 45),
            new CalciatoreInfo("Giovanni","Simeone",Calciatore.Ruolo.ATTACCANTE,"Napoli",40),
            new CalciatoreInfo("Eljif","Elmas",Calciatore.Ruolo.CENTROCAMPISTA,"Napoli",35),
            new CalciatoreInfo("Leo","Ostigard",Calciatore.Ruolo.DIFENSORE,"Napoli",30),
            new CalciatoreInfo("Amir","Zedadka",Calciatore.Ruolo.DIFENSORE,"Napoli",25),
            new CalciatoreInfo("Jesper","Lindstrøm",Calciatore.Ruolo.CENTROCAMPISTA,"Napoli",35),
            // AS Roma
            new CalciatoreInfo("Rui", "Patricio", Calciatore.Ruolo.PORTIERE, "AS Roma", 25),
            new CalciatoreInfo("Gianluca", "Mancini", Calciatore.Ruolo.DIFENSORE, "AS Roma", 40),
            new CalciatoreInfo("Chris", "Smalling", Calciatore.Ruolo.DIFENSORE, "AS Roma", 35),
            new CalciatoreInfo("Lorenzo", "Pellegrini", Calciatore.Ruolo.CENTROCAMPISTA, "AS Roma", 60),
            new CalciatoreInfo("Bryan", "Cristante", Calciatore.Ruolo.CENTROCAMPISTA, "AS Roma", 35),
            new CalciatoreInfo("Paulo", "Dybala", Calciatore.Ruolo.ATTACCANTE, "AS Roma", 75),
            new CalciatoreInfo("Romelu", "Lukaku", Calciatore.Ruolo.ATTACCANTE, "AS Roma", 65),
            new CalciatoreInfo("Evan","Ndicka",Calciatore.Ruolo.DIFENSORE,"AS Roma",35),
            new CalciatoreInfo("Leandro","Paredes",Calciatore.Ruolo.CENTROCAMPISTA,"AS Roma",40),
            new CalciatoreInfo("Renato","Sanches",Calciatore.Ruolo.CENTROCAMPISTA,"AS Roma",35),
            new CalciatoreInfo("Stephan","El Shaarawy",Calciatore.Ruolo.ATTACCANTE,"AS Roma",35),
            new CalciatoreInfo("Andrea","Belotti",Calciatore.Ruolo.ATTACCANTE,"AS Roma",35),
            // Lazio
            new CalciatoreInfo("Ivan", "Provedel", Calciatore.Ruolo.PORTIERE, "Lazio", 35),
            new CalciatoreInfo("Alessio", "Romagnoli", Calciatore.Ruolo.DIFENSORE, "Lazio", 35),
            new CalciatoreInfo("Nicolo", "Casale", Calciatore.Ruolo.DIFENSORE, "Lazio", 30),
            new CalciatoreInfo("Mattia", "Zaccagni", Calciatore.Ruolo.CENTROCAMPISTA, "Lazio", 50),
            new CalciatoreInfo("Luis", "Alberto", Calciatore.Ruolo.CENTROCAMPISTA, "Lazio", 50),
            new CalciatoreInfo("Sergej", "Milinkovic-Savic", Calciatore.Ruolo.CENTROCAMPISTA, "Lazio", 70),
            new CalciatoreInfo("Ciro", "Immobile", Calciatore.Ruolo.ATTACCANTE, "Lazio", 70),
            new CalciatoreInfo("Felipe", "Anderson", Calciatore.Ruolo.ATTACCANTE, "Lazio", 50),
            new CalciatoreInfo("Matias","Vecino",Calciatore.Ruolo.CENTROCAMPISTA,"Lazio",35),
            new CalciatoreInfo("Daichi","Kamada",Calciatore.Ruolo.CENTROCAMPISTA,"Lazio",40),
            new CalciatoreInfo("Patric","Gabarrón",Calciatore.Ruolo.DIFENSORE,"Lazio",30),
            new CalciatoreInfo("Gustav","Isaksen",Calciatore.Ruolo.ATTACCANTE,"Lazio",35),
            new CalciatoreInfo("Pedro","Rodriguez",Calciatore.Ruolo.ATTACCANTE,"Lazio",30),

            // Atalanta
            new CalciatoreInfo("Juan", "Musso", Calciatore.Ruolo.PORTIERE, "Atalanta", 35),
            new CalciatoreInfo("Giorgio", "Scalvini", Calciatore.Ruolo.DIFENSORE, "Atalanta", 45),
            new CalciatoreInfo("Rafael", "Toloi", Calciatore.Ruolo.DIFENSORE, "Atalanta", 35),
            new CalciatoreInfo("Teun", "Koopmeiners", Calciatore.Ruolo.CENTROCAMPISTA, "Atalanta", 60),
            new CalciatoreInfo("Marten", "De Roon", Calciatore.Ruolo.CENTROCAMPISTA, "Atalanta", 45),
            new CalciatoreInfo("Ademola", "Lookman", Calciatore.Ruolo.ATTACCANTE, "Atalanta", 55),
            new CalciatoreInfo("Gianluca", "Scamacca", Calciatore.Ruolo.ATTACCANTE, "Atalanta", 60),
            new CalciatoreInfo("Sead","Kolasinac",Calciatore.Ruolo.DIFENSORE,"Atalanta",30),
            new CalciatoreInfo("Charles","De Ketelaere",Calciatore.Ruolo.ATTACCANTE,"Atalanta",40),
            new CalciatoreInfo("Mario","Pasalic",Calciatore.Ruolo.CENTROCAMPISTA,"Atalanta",40),
            new CalciatoreInfo("Hans","Hateboer",Calciatore.Ruolo.DIFENSORE,"Atalanta",30),
            // Fiorentina
            new CalciatoreInfo("Pietro", "Terracciano", Calciatore.Ruolo.PORTIERE, "Fiorentina", 30),
            new CalciatoreInfo("Nikola", "Milenkovic", Calciatore.Ruolo.DIFENSORE, "Fiorentina", 35),
            new CalciatoreInfo("Cristiano", "Biraghi", Calciatore.Ruolo.DIFENSORE, "Fiorentina", 30),
            new CalciatoreInfo("Giacomo", "Bonaventura", Calciatore.Ruolo.CENTROCAMPISTA, "Fiorentina", 45),
            new CalciatoreInfo("Arthur", "Melo", Calciatore.Ruolo.CENTROCAMPISTA, "Fiorentina", 35),
            new CalciatoreInfo("Riccardo", "Sottil", Calciatore.Ruolo.ATTACCANTE, "Fiorentina", 30),
            new CalciatoreInfo("Nicolas", "Gonzalez", Calciatore.Ruolo.ATTACCANTE, "Fiorentina", 50),
            new CalciatoreInfo("Alfred","Duncan",Calciatore.Ruolo.CENTROCAMPISTA,"Fiorentina",30),
            new CalciatoreInfo("Rolando","Mandragora",Calciatore.Ruolo.CENTROCAMPISTA,"Fiorentina",30),
            new CalciatoreInfo("Lucas","Martinez Quarta",Calciatore.Ruolo.DIFENSORE,"Fiorentina",30),
            new CalciatoreInfo("Antonin","Barak",Calciatore.Ruolo.CENTROCAMPISTA,"Fiorentina",35),
            // Bologna
            new CalciatoreInfo("Lukasz", "Skorupski", Calciatore.Ruolo.PORTIERE, "Bologna", 30),
            new CalciatoreInfo("Stefan", "Posch", Calciatore.Ruolo.DIFENSORE, "Bologna", 30),
            new CalciatoreInfo("Jhon", "Lucumi", Calciatore.Ruolo.DIFENSORE, "Bologna", 30),
            new CalciatoreInfo("Lewis", "Ferguson", Calciatore.Ruolo.CENTROCAMPISTA, "Bologna", 40),
            new CalciatoreInfo("Remo", "Freuler", Calciatore.Ruolo.CENTROCAMPISTA, "Bologna", 35),
            new CalciatoreInfo("Riccardo", "Orsolini", Calciatore.Ruolo.ATTACCANTE, "Bologna", 45),
            new CalciatoreInfo("Joshua", "Zirkzee", Calciatore.Ruolo.ATTACCANTE, "Bologna", 50),
            new CalciatoreInfo("Nikola","Moro",Calciatore.Ruolo.CENTROCAMPISTA,"Bologna",30),
            new CalciatoreInfo("Michel","Aebischer",Calciatore.Ruolo.CENTROCAMPISTA,"Bologna",30),
            new CalciatoreInfo("Nicola","Sansone",Calciatore.Ruolo.ATTACCANTE,"Bologna",30),
            // Torino
            new CalciatoreInfo("Vanja", "Milinkovic-Savic", Calciatore.Ruolo.PORTIERE, "Torino", 30),
            new CalciatoreInfo("Alessandro", "Buongiorno", Calciatore.Ruolo.DIFENSORE, "Torino", 35),
            new CalciatoreInfo("Perr", "Schuurs", Calciatore.Ruolo.DIFENSORE, "Torino", 35),
            new CalciatoreInfo("Samuele", "Ricci", Calciatore.Ruolo.CENTROCAMPISTA, "Torino", 35),
            new CalciatoreInfo("Ivan", "Ilic", Calciatore.Ruolo.CENTROCAMPISTA, "Torino", 35),
            new CalciatoreInfo("Antonio", "Sanabria", Calciatore.Ruolo.ATTACCANTE, "Torino", 45),
            new CalciatoreInfo("Duvan", "Zapata", Calciatore.Ruolo.ATTACCANTE, "Torino", 45),
            new CalciatoreInfo("Gvidas","Gineitis",Calciatore.Ruolo.CENTROCAMPISTA,"Torino",25),
            new CalciatoreInfo("Karol","Linetty",Calciatore.Ruolo.CENTROCAMPISTA,"Torino",30),
            new CalciatoreInfo("Nemanja","Radonjic",Calciatore.Ruolo.ATTACCANTE,"Torino",30),
            // Udinese
            new CalciatoreInfo("Marco", "Silvestri", Calciatore.Ruolo.PORTIERE, "Udinese", 30),
            new CalciatoreInfo("Nehuen", "Perez", Calciatore.Ruolo.DIFENSORE, "Udinese", 30),
            new CalciatoreInfo("Jaka", "Bijol", Calciatore.Ruolo.DIFENSORE, "Udinese", 30),
            new CalciatoreInfo("Walace", "Souza", Calciatore.Ruolo.CENTROCAMPISTA, "Udinese", 30),
            new CalciatoreInfo("Sandi", "Lovric", Calciatore.Ruolo.CENTROCAMPISTA, "Udinese", 30),
            new CalciatoreInfo("Lazar", "Samardzic", Calciatore.Ruolo.CENTROCAMPISTA, "Udinese", 40),
            new CalciatoreInfo("Beto", "Betuncal", Calciatore.Ruolo.ATTACCANTE, "Udinese", 45),
            new CalciatoreInfo("Isaac", "Success", Calciatore.Ruolo.ATTACCANTE, "Udinese", 30),
            new CalciatoreInfo("Florian","Thauvin",Calciatore.Ruolo.ATTACCANTE,"Udinese",35),
            new CalciatoreInfo("Roberto","Pereyra",Calciatore.Ruolo.CENTROCAMPISTA,"Udinese",40),
            new CalciatoreInfo("Kingsley","Ehizibue",Calciatore.Ruolo.DIFENSORE,"Udinese",25),
            new CalciatoreInfo("Lorenzo","Lucca",Calciatore.Ruolo.ATTACCANTE,"Udinese",30),
            new CalciatoreInfo("Thomas","Kristensen",Calciatore.Ruolo.DIFENSORE,"Udinese",25),
            // Sassuolo
            new CalciatoreInfo("Andrea", "Consigli", Calciatore.Ruolo.PORTIERE, "Sassuolo", 30),
            new CalciatoreInfo("Gian Marco", "Ferrari", Calciatore.Ruolo.DIFENSORE, "Sassuolo", 30),
            new CalciatoreInfo("Ruan", "Tressoldi", Calciatore.Ruolo.DIFENSORE, "Sassuolo", 30),
            new CalciatoreInfo("Davide", "Frattesi", Calciatore.Ruolo.CENTROCAMPISTA, "Sassuolo", 50),
            new CalciatoreInfo("Nedim", "Bajrami", Calciatore.Ruolo.CENTROCAMPISTA, "Sassuolo", 35),
            new CalciatoreInfo("Domenico", "Berardi", Calciatore.Ruolo.ATTACCANTE, "Sassuolo", 60),
            new CalciatoreInfo("Andrea", "Pinamonti", Calciatore.Ruolo.ATTACCANTE, "Sassuolo", 40),
            new CalciatoreInfo("Kristian","Thorstvedt",Calciatore.Ruolo.CENTROCAMPISTA,"Sassuolo",35),
            new CalciatoreInfo("Jeremy","Toljan",Calciatore.Ruolo.DIFENSORE,"Sassuolo",30),
            new CalciatoreInfo("Kristian","Volpato",Calciatore.Ruolo.ATTACCANTE,"Sassuolo",30),
            new CalciatoreInfo("Gregoire","Defrel",Calciatore.Ruolo.ATTACCANTE,"Sassuolo",30),
            new CalciatoreInfo("Matheus","Henrique",Calciatore.Ruolo.CENTROCAMPISTA,"Sassuolo",35),
            // Empoli
            new CalciatoreInfo("Elia", "Caprile", Calciatore.Ruolo.PORTIERE, "Empoli", 25),
            new CalciatoreInfo("Sebastiano", "Luperto", Calciatore.Ruolo.DIFENSORE, "Empoli", 30),
            new CalciatoreInfo("Ardian", "Ismajli", Calciatore.Ruolo.DIFENSORE, "Empoli", 25),
            new CalciatoreInfo("Jacopo", "Fazzini", Calciatore.Ruolo.CENTROCAMPISTA, "Empoli", 25),
            new CalciatoreInfo("Filippo", "Ranocchia", Calciatore.Ruolo.CENTROCAMPISTA, "Empoli", 25),
            new CalciatoreInfo("Francesco", "Caputo", Calciatore.Ruolo.ATTACCANTE, "Empoli", 35),
            new CalciatoreInfo("Tommaso", "Baldanzi", Calciatore.Ruolo.ATTACCANTE, "Empoli", 35),
            new CalciatoreInfo("Alberto","Grassi",Calciatore.Ruolo.CENTROCAMPISTA,"Empoli",30),
            new CalciatoreInfo("Simone","Bastoni",Calciatore.Ruolo.DIFENSORE,"Empoli",25),
            new CalciatoreInfo("Giuseppe","Pezzella",Calciatore.Ruolo.DIFENSORE,"Empoli",25),
            new CalciatoreInfo("Razvan","Marin",Calciatore.Ruolo.CENTROCAMPISTA,"Empoli",30),
            new CalciatoreInfo("Nicolò","Cambagiorni",Calciatore.Ruolo.ATTACCANTE,"Empoli",25),
            // Verona
            new CalciatoreInfo("Lorenzo", "Montipò", Calciatore.Ruolo.PORTIERE, "Verona", 25),
            new CalciatoreInfo("Isak", "Hien", Calciatore.Ruolo.DIFENSORE, "Verona", 30),
            new CalciatoreInfo("Diego", "Coppola", Calciatore.Ruolo.DIFENSORE, "Verona", 25),
            new CalciatoreInfo("Ondrej", "Duda", Calciatore.Ruolo.CENTROCAMPISTA, "Verona", 35),
            new CalciatoreInfo("Adrien", "Tameze", Calciatore.Ruolo.CENTROCAMPISTA, "Verona", 30),
            new CalciatoreInfo("Cyril", "Ngonge", Calciatore.Ruolo.ATTACCANTE, "Verona", 35),
            new CalciatoreInfo("Milan", "Djuric", Calciatore.Ruolo.ATTACCANTE, "Verona", 30),
            new CalciatoreInfo("Josh","Doig",Calciatore.Ruolo.DIFENSORE,"Verona",30),
            new CalciatoreInfo("Darko","Lazovic",Calciatore.Ruolo.CENTROCAMPISTA,"Verona",35),
            new CalciatoreInfo("Filip","Kostic",Calciatore.Ruolo.CENTROCAMPISTA,"Verona",30),
            new CalciatoreInfo("Jayden","Braaf",Calciatore.Ruolo.ATTACCANTE,"Verona",25),
            new CalciatoreInfo("Thomas","Henry",Calciatore.Ruolo.ATTACCANTE,"Verona",30),
            // Salernitana
            new CalciatoreInfo("Guillermo", "Ochoa", Calciatore.Ruolo.PORTIERE, "Salernitana", 30),
            new CalciatoreInfo("Lorenzo", "Pirola", Calciatore.Ruolo.DIFENSORE, "Salernitana", 30),
            new CalciatoreInfo("Norbert", "Gyomber", Calciatore.Ruolo.DIFENSORE, "Salernitana", 25),
            new CalciatoreInfo("Emil", "Bohinen", Calciatore.Ruolo.CENTROCAMPISTA, "Salernitana", 25),
            new CalciatoreInfo("Lassana", "Coulibaly", Calciatore.Ruolo.CENTROCAMPISTA, "Salernitana", 30),
            new CalciatoreInfo("Boulaye", "Dia", Calciatore.Ruolo.ATTACCANTE, "Salernitana", 45),
            new CalciatoreInfo("Antonio", "Candreva", Calciatore.Ruolo.ATTACCANTE, "Salernitana", 35),
            new CalciatoreInfo("Marash","Kumbulla",Calciatore.Ruolo.DIFENSORE,"Salernitana",30),
            new CalciatoreInfo("Junior","Sambia",Calciatore.Ruolo.DIFENSORE,"Salernitana",25),
            new CalciatoreInfo("Mateusz","Legowski",Calciatore.Ruolo.CENTROCAMPISTA,"Salernitana",25),
            new CalciatoreInfo("Jovane","Cabral",Calciatore.Ruolo.ATTACCANTE,"Salernitana",30),
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

            if (calciatoreEsiste(nome, cognome)) {
                System.out.println("Calciatore già esistente: " + nome + " " + cognome + " - saltato");
                return false;
            }

            int idFascia;
            if (costo >= 70) {
                idFascia = 1;            // TOP
            } else if (costo >= 50) {
                idFascia = 2;            // SEMI-TOP
            } else if (costo >= 35) {
                idFascia = 3;            // BUONO
            } else if (costo >= 20) {
                idFascia = 4;            // MEDIO
            } else {
                idFascia = 5;            // SCARSO
            }
            calciatore.setIdFascia(idFascia);
            return calciatoreDAO.inserisciCalciatore(calciatore, idSquadra);
        } else {
            System.err.println("Squadra non trovata: " + nomeSquadra);
            return false;
        }
    }

    private boolean calciatoreEsiste(String nome, String cognome) {
        return calciatoreDAO.calciatoreEsiste(nome, cognome); // Usa il metodo del DAO
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