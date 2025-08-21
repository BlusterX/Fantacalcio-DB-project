package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fantacalcio.model.Calciatore;
import fantacalcio.util.DatabaseConnection;

/**
 * Data Access Object per la gestione dei calciatori
 */
public class CalciatoreDAO {

    private final DatabaseConnection dbConnection;

    public CalciatoreDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inserisce un nuovo calciatore
     */
    public boolean inserisciCalciatore(Calciatore calciatore) {
        String sql = "INSERT INTO CALCIATORE (Nome, Cognome, Ruolo, Costo, Infortunato, Squalificato, ID_Squadra) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getNewConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (calciatoreEsiste(calciatore.getNome(), calciatore.getCognome())) {
                System.out.println("Calciatore già esistente: " + calciatore.getNomeCompleto() + " - saltato");
                return false;
            }

            stmt.setString(1, calciatore.getNome());
            stmt.setString(2, calciatore.getCognome());
            stmt.setString(3, calciatore.getRuolo().getAbbreviazione());
            stmt.setInt(4, calciatore.getCosto());
            stmt.setBoolean(5, calciatore.isInfortunato());
            stmt.setBoolean(6, calciatore.isSqualificato());
            stmt.setInt(7, calciatore.getIdSquadra());

            int righe = stmt.executeUpdate();
            if (righe > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) calciatore.setIdCalciatore(keys.getInt(1));
                }
                System.out.println("Calciatore inserito: " + calciatore.getNomeCompleto() + " (costo: " + calciatore.getCosto() + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore inserimento calciatore: " + e.getMessage());
        }
        return false;
    }

    public List<CalciatoreConSquadra> trovaTuttiICalciatoriConSquadraPerCampionato(int idCampionato) {
        List<CalciatoreConSquadra> out = new ArrayList<>();
        String sql = """
            SELECT 
                c.ID_Calciatore, c.Nome, c.Cognome, c.Ruolo, c.Costo,
                c.Infortunato, c.Squalificato, c.ID_Squadra,
                s.ID_Squadra AS S_ID_Squadra, s.Nome AS S_Nome
            FROM CALCIATORE c
            JOIN SQUADRA s ON c.ID_Squadra = s.ID_Squadra
            WHERE s.ID_Campionato = ?
            ORDER BY c.Cognome, c.Nome
            """;
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCampionato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Calciatore c = creaCalciatoreDaResultSet(rs);
                    Integer sId = (Integer) rs.getObject("S_ID_Squadra");
                    String sNome = rs.getString("S_Nome");
                    out.add(new CalciatoreConSquadra(c, sId, sNome));
                }
            }
        } catch (SQLException e) {
            System.err.println("trovaTuttiICalciatoriConSquadraPerCampionato: " + e.getMessage());
        }
        return out;
    }


    public List<CalciatoreConSquadra> trovaTuttiICalciatoriConSquadra() {
        List<CalciatoreConSquadra> out = new ArrayList<>();
        String sql = """
            SELECT 
                c.ID_Calciatore, c.Nome, c.Cognome, c.Ruolo, c.Costo,
                c.Infortunato, c.Squalificato, c.ID_Squadra,
                s.ID_Squadra AS S_ID_Squadra, s.Nome AS S_Nome
            FROM CALCIATORE c
            LEFT JOIN SQUADRA s ON c.ID_Squadra = s.ID_Squadra
            ORDER BY c.Cognome, c.Nome
            """;

        try (Connection conn = dbConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Calciatore c = creaCalciatoreDaResultSet(rs);

                Integer sId = (Integer) rs.getObject("S_ID_Squadra"); // null-safe
                String sNome = rs.getString("S_Nome");

                out.add(new CalciatoreConSquadra(c, sId, sNome));
            }
        } catch (SQLException e) {
            System.err.println("Errore trovaTuttiICalciatoriConSquadra: " + e.getMessage());
        }
        return out;
    }

    public boolean calciatoreEsiste(String nome, String cognome) {
        String sql = "SELECT COUNT(*) FROM CALCIATORE WHERE Nome = ? AND Cognome = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            stmt.setString(2, cognome);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Errore controllo esistenza calciatore: " + e.getMessage());
        }
        return false;
    }

    public Optional<Calciatore> trovaCalciatorePerId(int idCalciatore) {
        String sql = "SELECT * FROM CALCIATORE WHERE ID_Calciatore = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCalciatore);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaCalciatoreDaResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca calciatore per ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Calciatore> trovaTuttiICalciatori() {
        List<Calciatore> calciatori = new ArrayList<>();
        String sql = "SELECT * FROM CALCIATORE";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                calciatori.add(creaCalciatoreDaResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Errore recupero calciatori: " + e.getMessage());
        }
        return calciatori;
    }

    public Map<Calciatore.Ruolo, Integer> contaCalciatoriPerRuolo() {
        Map<Calciatore.Ruolo, Integer> conteggi = new HashMap<>();
        String sql = "SELECT Ruolo, COUNT(*) as Conteggio FROM CALCIATORE GROUP BY Ruolo";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String ruoloDb = rs.getString("Ruolo");
                Calciatore.Ruolo ruolo = switch (ruoloDb) {
                    case "P" -> Calciatore.Ruolo.PORTIERE;
                    case "D" -> Calciatore.Ruolo.DIFENSORE;
                    case "C" -> Calciatore.Ruolo.CENTROCAMPISTA;
                    case "A" -> Calciatore.Ruolo.ATTACCANTE;
                    default -> throw new IllegalArgumentException("Ruolo non valido: " + ruoloDb);
                };
                conteggi.put(ruolo, rs.getInt("Conteggio"));
            }

        } catch (SQLException e) {
            System.err.println("Errore conteggio calciatori per ruolo: " + e.getMessage());
        }
        return conteggi;
    }

    /**
     * Metodo helper per creare un oggetto Calciatore da un ResultSet
     */
    private Calciatore creaCalciatoreDaResultSet(ResultSet rs) throws SQLException {
        Calciatore.Ruolo ruolo = switch (rs.getString("Ruolo")) {
            case "P" -> Calciatore.Ruolo.PORTIERE;
            case "D" -> Calciatore.Ruolo.DIFENSORE;
            case "C" -> Calciatore.Ruolo.CENTROCAMPISTA;
            case "A" -> Calciatore.Ruolo.ATTACCANTE;
            default -> throw new IllegalArgumentException("Ruolo non valido: " + rs.getString("Ruolo"));
        };

        return new Calciatore(
            rs.getInt("ID_Calciatore"),
            rs.getString("Nome"),
            rs.getString("Cognome"),
            ruolo,
            rs.getInt("Costo"),
            rs.getBoolean("Infortunato"),
            rs.getBoolean("Squalificato"),
            rs.getInt("ID_Squadra")
        );
    }

    public int contaCalciatoriPerSquadra(int idSquadra) {
        final String sql = "SELECT COUNT(*) FROM CALCIATORE WHERE ID_Squadra = ?";
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSquadra);
            try (ResultSet rs = ps.executeQuery()) {
                return (rs.next() ? rs.getInt(1) : 0);
            }
        } catch (SQLException e) {
            System.err.println("Errore contaCalciatoriPerSquadra: " + e.getMessage());
            return 0;
        }
    }



    // Classe helper (usata eventualmente in altri DAO o GUI)
    public static class CalciatoreConSquadra {
        private final Calciatore calciatore;
        private final Integer idSquadra;     // può essere null
        private final String nomeSquadra;    // può essere null

        public CalciatoreConSquadra(Calciatore calciatore, Integer idSquadra, String nomeSquadra) {
            this.calciatore = calciatore;
            this.idSquadra = idSquadra;
            this.nomeSquadra = nomeSquadra;
        }

        public Calciatore getCalciatore() { return calciatore; }
        public Integer getIdSquadra() { return idSquadra; }
        public String getNomeSquadra() { return (nomeSquadra != null) ? nomeSquadra : "Senza squadra"; }
    }
}
