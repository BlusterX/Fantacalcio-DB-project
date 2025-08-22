package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fantacalcio.model.Calciatore;
import fantacalcio.model.Formazione;
import fantacalcio.util.DatabaseConnection;

public class FormazioneDAO {

    private final DatabaseConnection dbConnection;

    public FormazioneDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Restituisce l'intera formazione (modulo, squadra, giornata, punteggio).
     */
    public Formazione getFormazioneById(int idFormazione) {
        String sql = "SELECT * FROM FORMAZIONE WHERE ID_Formazione = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFormazione);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Formazione f = new Formazione();
                    f.setIdFormazione(rs.getInt("ID_Formazione"));
                    f.setModulo(rs.getString("Modulo"));
                    f.setIdSquadraFantacalcio(rs.getInt("ID_Squadra_Fantacalcio"));
                    f.setNumeroGiornata(rs.getInt("Numero_Giornata"));
                    f.setPunteggio(rs.getDouble("Punteggio"));
                    return f;
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore lettura formazione: " + e.getMessage());
        }
        return null;
    }

    /**
     * Restituisce l'ID della formazione schierata da una squadra in una giornata.
     */
    public Integer getFormazioneSquadraGiornata(int idSquadraFantacalcio, int numeroGiornata) {
        String sql = """
            SELECT ID_Formazione
            FROM FORMAZIONE
            WHERE ID_Squadra_Fantacalcio = ? AND Numero_Giornata = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSquadraFantacalcio);
            stmt.setInt(2, numeroGiornata);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_Formazione");
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore ricerca formazione per giornata: " + e.getMessage());
        }
        return null;
    }

    public List<Calciatore> trovaTitolari(int idFormazione) {
        return trovaCalciatoriByFormazione(idFormazione, false);
    }

    public List<Calciatore> trovaPanchinari(int idFormazione) {
        return trovaCalciatoriByFormazione(idFormazione, true);
    }

    private List<Calciatore> trovaCalciatoriByFormazione(int idFormazione, boolean panchina) {
        List<Calciatore> calciatori = new ArrayList<>();

        String sql = """
            SELECT c.*
            FROM CALCIATORE c
                     JOIN FORMANO f ON c.ID_Calciatore = f.ID_Calciatore
            WHERE f.ID_Formazione = ? AND f.Panchina = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFormazione);
            stmt.setString(2, panchina ? "SI" : "NO");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Calciatore.Ruolo ruolo = switch (rs.getString("Ruolo")) {
                        case "P" -> Calciatore.Ruolo.PORTIERE;
                        case "D" -> Calciatore.Ruolo.DIFENSORE;
                        case "C" -> Calciatore.Ruolo.CENTROCAMPISTA;
                        case "A" -> Calciatore.Ruolo.ATTACCANTE;
                        default -> throw new IllegalArgumentException("Ruolo non valido: " + rs.getString("Ruolo"));
                    };

                    Calciatore c = new Calciatore(
                            rs.getInt("ID_Calciatore"),
                            rs.getString("Nome"),
                            rs.getString("Cognome"),
                            ruolo,
                            rs.getInt("Costo"),
                            rs.getBoolean("Infortunato"),
                            rs.getBoolean("Squalificato"),
                            rs.getInt("ID_Squadra")
                    );
                    calciatori.add(c);
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore recupero calciatori della formazione: " + e.getMessage());
        }

        return calciatori;
    }

    public boolean salvaFormazioneCompleta(int idSquadraFantacalcio,
                                        int numeroGiornata,
                                        String modulo,
                                        List<Integer> idsTitolari,
                                        List<Integer> idsPanchina) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            // 0) Assicura la riga in GIORNATA (vincolo FK su FORMAZIONE.Numero_Giornata) 
            ensureGiornataExists(conn, numeroGiornata);

            // 1) Cerca FORMAZIONE esistente per (squadra, giornata)
            Integer idFormazione = findFormazioneIdBySquadraGiornata(conn, idSquadraFantacalcio, numeroGiornata);

            if (idFormazione == null) {
                // 2a) INSERT nuova FORMAZIONE
                idFormazione = insertFormazione(conn, modulo, idSquadraFantacalcio, numeroGiornata);
            } else {
                // 2b) UPDATE modulo + pulizia FORMANO
                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE FORMAZIONE SET Modulo=? WHERE ID_Formazione=?")) {
                    up.setString(1, modulo);
                    up.setInt(2, idFormazione);
                    up.executeUpdate();
                }
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM FORMANO WHERE ID_Formazione=?")) {
                    del.setInt(1, idFormazione);
                    del.executeUpdate();
                }
            }

            // 3) Inserisci i TITOLARI
            if (idsTitolari != null && !idsTitolari.isEmpty()) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO FORMANO (ID_Calciatore, ID_Formazione, Panchina) VALUES (?, ?, 'NO')")) {
                    for (Integer idCal : idsTitolari) {
                        ins.setInt(1, idCal);
                        ins.setInt(2, idFormazione);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

            // 4) Inserisci la PANCHINA
            if (idsPanchina != null && !idsPanchina.isEmpty()) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO FORMANO (ID_Calciatore, ID_Formazione, Panchina) VALUES (?, ?, 'SI')")) {
                    for (Integer idCal : idsPanchina) {
                        ins.setInt(1, idCal);
                        ins.setInt(2, idFormazione);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignore) {}
            System.err.println("[FormazioneDAO] Errore salvaFormazioneCompleta: " + ex.getMessage());
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }

    // true se già esiste, altrimenti clona l’ultima e ritorna l'ID nuova
    public int ensureFormazionePerGiornata(int idSquadra, int giornata) {
        String qFind = "SELECT ID_Formazione FROM FORMAZIONE WHERE ID_Squadra_Fantacalcio=? AND Numero_Giornata=?";
        String qPrev = "SELECT ID_Formazione, Numero_Giornata FROM FORMAZIONE WHERE ID_Squadra_Fantacalcio=? AND Numero_Giornata < ? ORDER BY Numero_Giornata DESC LIMIT 1";
        String qInsF = "INSERT INTO FORMAZIONE (Modulo, ID_Squadra_Fantacalcio, Numero_Giornata, Punteggio) " +
                    "SELECT Modulo, ID_Squadra_Fantacalcio, ?, 0 FROM FORMAZIONE WHERE ID_Formazione=?";
        String qCopy = "INSERT INTO FORMANO (ID_Calciatore, ID_Formazione, Panchina) " +
                    "SELECT ID_Calciatore, ?, Panchina FROM FORMANO WHERE ID_Formazione=?";

        try (Connection c = dbConnection.getConnection()) {
            // esiste già?
            try (PreparedStatement ps = c.prepareStatement(qFind)) {
                ps.setInt(1, idSquadra); ps.setInt(2, giornata);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
            }
            // prendi l'ultima precedente
            int prevId;
            try (PreparedStatement ps = c.prepareStatement(qPrev)) {
                ps.setInt(1, idSquadra); ps.setInt(2, giornata);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalStateException("Nessuna formazione precedente da clonare.");
                    prevId = rs.getInt(1);
                }
            }
            c.setAutoCommit(false);
            int newId;
            try (PreparedStatement insF = c.prepareStatement(qInsF, Statement.RETURN_GENERATED_KEYS)) {
                insF.setInt(1, giornata);
                insF.setInt(2, prevId);
                insF.executeUpdate();
                try (ResultSet gk = insF.getGeneratedKeys()) { gk.next(); newId = gk.getInt(1); }
            }
            try (PreparedStatement copy = c.prepareStatement(qCopy)) {
                copy.setInt(1, newId);
                copy.setInt(2, prevId);
                copy.executeUpdate();
            }
            c.commit(); c.setAutoCommit(true);
            return newId;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }


    private Integer findFormazioneIdBySquadraGiornata(Connection conn, int idSquadra, int giornata) throws SQLException {
        final String q = "SELECT ID_Formazione FROM FORMAZIONE WHERE ID_Squadra_Fantacalcio=? AND Numero_Giornata=?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, idSquadra);
            ps.setInt(2, giornata);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    /** INSERT su FORMAZIONE secondo schema attuale (con Numero_Giornata nella stessa tabella). */
    private int insertFormazione(Connection conn, String modulo, int idSquadra, int giornata) throws SQLException {
        final String q = "INSERT INTO FORMAZIONE (Modulo, ID_Squadra_Fantacalcio, Numero_Giornata) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(q, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, modulo);
            ps.setInt(2, idSquadra);
            ps.setInt(3, giornata);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) return gk.getInt(1);
            }
        }
        throw new SQLException("Impossibile creare FORMAZIONE");
    }

    /** Assicura che la giornata esista in tabella GIORNATA (FK da FORMAZIONE.Numero_Giornata). */
    private void ensureGiornataExists(Connection conn, int numeroGiornata) throws SQLException {
        // tentativo di insert ignorando l'errore in caso esista già
        try (PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO GIORNATA (Numero) VALUES (?)")) {
            ps.setInt(1, numeroGiornata);
            ps.executeUpdate();
        }
    }
    /** Elimina interamente la formazione di (squadra, giornata). */
    public boolean eliminaFormazione(int idSquadraFantacalcio, int numeroGiornata) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            Integer idFormazione = findFormazioneId(conn, idSquadraFantacalcio, numeroGiornata);
            if (idFormazione == null) {
                conn.rollback();
                return false;
            }
            clearFormano(conn, idFormazione);

            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM FORMAZIONE WHERE ID_Formazione=?")) {
                del.setInt(1, idFormazione);
                del.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean copiaFormazioneDaGiornata(int idSquadraFantacalcio, int fromGiornata, int toGiornata) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            Integer idFrom = findFormazioneId(conn, idSquadraFantacalcio, fromGiornata);
            if (idFrom == null) { conn.rollback(); return false; }

            // leggo modulo di origine
            String modulo = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT Modulo FROM FORMAZIONE WHERE ID_Formazione=?")) {
                ps.setInt(1, idFrom);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) modulo = rs.getString(1);
                }
            }
            if (modulo == null) { conn.rollback(); return false; }

            // leggo componenti origine
            List<Integer> titolari = new ArrayList<>();
            List<Integer> panchina = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT ID_Calciatore, Panchina FROM FORMANO WHERE ID_Formazione=?")) {
                ps.setInt(1, idFrom);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if ("NO".equals(rs.getString("Panchina")))
                            titolari.add(rs.getInt("ID_Calciatore"));
                        else
                            panchina.add(rs.getInt("ID_Calciatore"));
                    }
                }
            }

            // upsert sulla giornata di destinazione
            Integer idTo = findFormazioneId(conn, idSquadraFantacalcio, toGiornata);
            if (idTo == null) {
                idTo = insertFormazione(conn, modulo, idSquadraFantacalcio, toGiornata);
            } else {
                updateModulo(conn, idTo, modulo);
                clearFormano(conn, idTo);
            }
            insertFormanoBatch(conn, idTo, titolari, false);
            insertFormanoBatch(conn, idTo, panchina, true);

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Ritorna l'ID_Formazione per (squadra, giornata) o null se non esiste. */
    private Integer findFormazioneId(Connection conn, int idSquadraFantacalcio, int numeroGiornata) throws SQLException {
        final String q = """
            SELECT ID_Formazione
            FROM FORMAZIONE
            WHERE ID_Squadra_Fantacalcio=? AND Numero_Giornata=?
            """;
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, idSquadraFantacalcio);
            ps.setInt(2, numeroGiornata);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    /** UPDATE del modulo su FORMAZIONE. */
    private void updateModulo(Connection conn, int idFormazione, String modulo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE FORMAZIONE SET Modulo=? WHERE ID_Formazione=?")) {
            ps.setString(1, modulo);
            ps.setInt(2, idFormazione);
            ps.executeUpdate();
        }
    }

    /** DELETE di tutti i componenti in FORMANO per quella formazione. */
    private void clearFormano(Connection conn, int idFormazione) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM FORMANO WHERE ID_Formazione=?")) {
            ps.setInt(1, idFormazione);
            ps.executeUpdate();
        }
    }

    /** INSERT batch in FORMANO per lista di calciatori e flag panchina. */
    private void insertFormanoBatch(Connection conn, int idFormazione, List<Integer> idCalciatori, boolean panchina) throws SQLException {
        if (idCalciatori == null || idCalciatori.isEmpty()) return;
        final String q = "INSERT INTO FORMANO (ID_Calciatore, ID_Formazione, Panchina) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            for (Integer idCal : idCalciatori) {
                ps.setInt(1, idCal);
                ps.setInt(2, idFormazione);
                ps.setString(3, panchina ? "SI" : "NO");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    }
