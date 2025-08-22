package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.ScontroLega;
import fantacalcio.util.DatabaseConnection;

public class ScontroLegaDAO {

    private final DatabaseConnection dbConnection;

    public ScontroLegaDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Restituisce gli ID_Squadra_Fantacalcio completati di una lega
     */
    public List<Integer> getSquadreCompleteLega(int idLega) {
        List<Integer> ids = new ArrayList<>();
        final String sql = """
            SELECT ID_Squadra_Fantacalcio
            FROM SQUADRA_FANTACALCIO
            WHERE ID_Lega = ? AND Completata = TRUE
        """;
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Errore caricamento squadre complete: " + e.getMessage());
        }
        return ids;
    }

    // ScontroLegaDAO.java
    public List<ScontroLega> trovaScontriGiornata(int idLega, int giornata) {
        String sql = """
            SELECT s.ID_Scontro, s.ID_Formazione1, s.ID_Formazione2,
                s.Risultato, s.Stato, s.Data_inizio,
                sf1.Nome AS NomeSquadra1, sf2.Nome AS NomeSquadra2
            FROM SCONTRO s
            JOIN FORMAZIONE f1 ON f1.ID_Formazione = s.ID_Formazione1
            JOIN FORMAZIONE f2 ON f2.ID_Formazione = s.ID_Formazione2
            JOIN SQUADRA_FANTACALCIO sf1 ON sf1.ID_Squadra_Fantacalcio = f1.ID_Squadra_Fantacalcio
            JOIN SQUADRA_FANTACALCIO sf2 ON sf2.ID_Squadra_Fantacalcio = f2.ID_Squadra_Fantacalcio
            WHERE s.ID_Lega = ? AND s.Numero_Giornata = ?
            ORDER BY s.ID_Scontro
            """;

        List<ScontroLega> out = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            ps.setInt(2, giornata);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ScontroLega s = new ScontroLega();
                    s.setIdScontro(rs.getInt("ID_Scontro"));
                    s.setIdFormazione1(rs.getInt("ID_Formazione1"));
                    s.setIdFormazione2(rs.getInt("ID_Formazione2"));
                    s.setRisultato(rs.getString("Risultato"));
                    s.setStato(parseStato(rs.getString("Stato")));
                    Timestamp ts = rs.getTimestamp("Data_inizio");
                    s.setDataInizio(ts != null ? ts.toLocalDateTime() : null);
                    s.setNomeSquadra1(rs.getString("NomeSquadra1"));
                    s.setNomeSquadra2(rs.getString("NomeSquadra2"));
                    out.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore trovaScontriGiornata: " + e.getMessage());
        }
        return out;
    }
    
    /** true se ogni squadra COMPLETATA della lega ha una formazione con 11 titolari per la giornata indicata */
    public boolean tutteFormazioniPresentiPerGiornata(int idLega, int giornata) {
        final String sqlPartecipanti = """
            SELECT COUNT(*) 
            FROM SQUADRA_FANTACALCIO 
            WHERE ID_Lega = ? AND Completata = TRUE
        """;
        final String sqlConFormazione = """
            SELECT COUNT(DISTINCT f.ID_Squadra_Fantacalcio)
            FROM FORMAZIONE f
            JOIN SQUADRA_FANTACALCIO s ON s.ID_Squadra_Fantacalcio = f.ID_Squadra_Fantacalcio
            JOIN TITOLARI t ON t.ID_Formazione = f.ID_Formazione
            WHERE s.ID_Lega = ? AND f.Numero_Giornata = ?
            GROUP BY f.ID_Formazione
            HAVING COUNT(t.ID_Calciatore) = 11
        """;

        int partecipanti = 0, conFormazione = 0;
        try (var c = dbConnection.getConnection()) {
            try (var ps = c.prepareStatement(sqlPartecipanti)) {
                ps.setInt(1, idLega);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) partecipanti = rs.getInt(1);
                }
            }
            try (var ps = c.prepareStatement(sqlConFormazione)) {
                ps.setInt(1, idLega);
                ps.setInt(2, giornata);
                try (var rs = ps.executeQuery()) {
                    // somma quante FORM. distinte rispettano i 11 titolari
                    while (rs.next()) conFormazione++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore controllo formazioni: " + e.getMessage());
        }
        return partecipanti > 0 && conFormazione == partecipanti;
    }


    /** comodo per aprire il dettaglio */
    public Optional<ScontroLega> trovaScontroById(int idScontro) {
        String sql = """
            SELECT s.ID_Scontro, s.ID_Formazione1, s.ID_Formazione2, s.Risultato, s.Stato, s.Data_inizio,
                sf1.Nome AS NomeSquadra1, sf2.Nome AS NomeSquadra2
            FROM SCONTRO s
            JOIN FORMAZIONE f1 ON f1.ID_Formazione = s.ID_Formazione1
            JOIN FORMAZIONE f2 ON f2.ID_Formazione = s.ID_Formazione2
            JOIN SQUADRA_FANTACALCIO sf1 ON sf1.ID_Squadra_Fantacalcio = f1.ID_Squadra_Fantacalcio
            JOIN SQUADRA_FANTACALCIO sf2 ON sf2.ID_Squadra_Fantacalcio = f2.ID_Squadra_Fantacalcio
            WHERE s.ID_Scontro = ?
            """;
        try (Connection c = dbConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idScontro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ScontroLega s = new ScontroLega();
                    s.setIdScontro(rs.getInt("ID_Scontro"));
                    s.setIdFormazione1(rs.getInt("ID_Formazione1"));
                    s.setIdFormazione2(rs.getInt("ID_Formazione2"));
                    s.setRisultato(rs.getString("Risultato"));
                    s.setStato(parseStato(rs.getString("Stato")));
                    Timestamp ts = rs.getTimestamp("Data_inizio");
                    s.setDataInizio(ts != null ? ts.toLocalDateTime() : null);
                    s.setNomeSquadra1(rs.getString("NomeSquadra1"));
                    s.setNomeSquadra2(rs.getString("NomeSquadra2"));
                    return Optional.of(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore trovaScontroById: " + e.getMessage());
        }
        return Optional.empty();
    }

    private fantacalcio.model.ScontroLega.StatoScontro parseStato(String db) {
        if (db == null) return fantacalcio.model.ScontroLega.StatoScontro.PROGRAMMATO;
        try { return fantacalcio.model.ScontroLega.StatoScontro.valueOf(db.trim().toUpperCase()); }
        catch (Exception e) { return fantacalcio.model.ScontroLega.StatoScontro.PROGRAMMATO; }
    }


    public java.time.LocalDateTime getProssimaDataInizio(int idLega) {
        final String sql = """
            SELECT MIN(Data_inizio)
            FROM SCONTRO
            WHERE ID_Lega = ? AND Stato = 'PROGRAMMATO'
        """;
        try (var c = dbConnection.getConnection();
            var ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp ts = rs.getTimestamp(1);
                    return (ts != null) ? ts.toLocalDateTime() : null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore getProssimaDataInizio: " + e.getMessage());
        }
        return null;
    }


    /**
     * Crea (se mancante) la FORMAZIONE “scheletro” per una squadra/giornata.
     * Ritorna l'ID_Formazione.
     */
    private int ensureFormazione(Connection c, int idSquadraFantacalcio, int giornata) throws SQLException {
        // Prova a trovarla
        final String sel = """
            SELECT ID_Formazione
            FROM FORMAZIONE
            WHERE ID_Squadra_Fantacalcio = ? AND Numero_Giornata = ?
        """;
        try (PreparedStatement ps = c.prepareStatement(sel)) {
            ps.setInt(1, idSquadraFantacalcio);
            ps.setInt(2, giornata);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        // Altrimenti creala (modulo placeholder)
        final String ins = """
            INSERT INTO FORMAZIONE (Modulo, ID_Squadra_Fantacalcio, Punteggio, Numero_Giornata)
            VALUES ('4-4-2', ?, 0, ?)
        """;
        try (PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idSquadraFantacalcio);
            ps.setInt(2, giornata);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Impossibile creare/trovare FORMAZIONE (sq=" + idSquadraFantacalcio + ", g=" + giornata + ")");
    }

    /**
     * Genera calendario "tutti contro tutti" (solo girone di andata)
     * ATTENZIONE: lavora con ID_Squadra_Fantacalcio, non con ID_Formazione.
     */
    public boolean generaCalendario(int idLega, List<Integer> squadreFantIds) {
        if (squadreFantIds == null || squadreFantIds.size() < 2 || squadreFantIds.size() % 2 != 0 || squadreFantIds.size() > 12) {
            System.err.println("Numero squadre deve essere pari tra 2 e 12");
            return false;
        }

        try (Connection c = dbConnection.getConnection()) {
            c.setAutoCommit(false);

            try {
                List<Integer> squadre = new ArrayList<>(squadreFantIds);
                Collections.shuffle(squadre);

                int n = squadre.size();
                int giornate = n - 1;

                final String insScontro = """
                    INSERT INTO SCONTRO (ID_Formazione1, ID_Formazione2, Risultato, Stato, Data_inizio, Numero_Giornata, ID_Lega)
                    VALUES (?, ?, NULL, 'PROGRAMMATO', NULL, ?, ?)
                """;

                try (PreparedStatement psScontro = c.prepareStatement(insScontro)) {
                    for (int g = 1; g <= giornate; g++) {
                        // round-robin pairing
                        for (int i = 0; i < n / 2; i++) {
                            int sq1 = squadre.get(i);
                            int sq2 = squadre.get(n - 1 - i);

                            int f1 = ensureFormazione(c, sq1, g);
                            int f2 = ensureFormazione(c, sq2, g);

                            psScontro.setInt(1, f1);
                            psScontro.setInt(2, f2);
                            psScontro.setInt(3, g);
                            psScontro.setInt(4, idLega);
                            psScontro.addBatch();
                        }
                        // rotazione (tranne la prima)
                        if (g < giornate) {
                            Integer last = squadre.remove(squadre.size() - 1);
                            squadre.add(1, last);
                        }
                    }
                    psScontro.executeBatch();
                }

                c.commit();
                System.out.println("Calendario generato: " + (n - 1) + " giornate, " + n + " squadre");
                return true;

            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore generazione calendario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera calendario completo (andata+ritorno)
     */
    private boolean generaCalendarioCompleto(int idLega, List<Integer> squadreFantIds) {
        if (squadreFantIds == null || squadreFantIds.size() < 2 || squadreFantIds.size() % 2 != 0) return false;

        try (Connection c = dbConnection.getConnection()) {
            c.setAutoCommit(false);

            try {
                List<Integer> baseOrder = new ArrayList<>(squadreFantIds);

                int n = baseOrder.size();
                int giornateTot = (n - 1) * 2;

                final String insScontro = """
                    INSERT INTO SCONTRO (ID_Formazione1, ID_Formazione2, Risultato, Stato, Data_inizio, Numero_Giornata, ID_Lega)
                    VALUES (?, ?, NULL, 'PROGRAMMATO', NULL, ?, ?)
                """;

                try (PreparedStatement psScontro = c.prepareStatement(insScontro)) {
                    // ANDATA
                    List<Integer> order = new ArrayList<>(baseOrder);
                    for (int g = 1; g <= n - 1; g++) {
                        for (int i = 0; i < n / 2; i++) {
                            int sq1 = order.get(i);
                            int sq2 = order.get(n - 1 - i);
                            int f1 = ensureFormazione(c, sq1, g);
                            int f2 = ensureFormazione(c, sq2, g);

                            psScontro.setInt(1, f1);
                            psScontro.setInt(2, f2);
                            psScontro.setInt(3, g);
                            psScontro.setInt(4, idLega);
                            psScontro.addBatch();
                        }
                        // rotate (tranne la prima)
                        if (g < n - 1) {
                            Integer last = order.remove(order.size() - 1);
                            order.add(1, last);
                        }
                    }

                    // RITORNO (invertendo casa/trasferta)
                    order = new ArrayList<>(baseOrder);
                    for (int g = n; g <= giornateTot; g++) {
                        int gAndata = g - (n - 1);

                        // Applica rotazioni dell'andata fino alla giornata di riferimento
                        for (int rot = 1; rot < gAndata; rot++) {
                            Integer last = order.remove(order.size() - 1);
                            order.add(1, last);
                        }

                        for (int i = 0; i < n / 2; i++) {
                            int sq1 = order.get(n - 1 - i); // invertito
                            int sq2 = order.get(i);
                            int f1 = ensureFormazione(c, sq1, g);
                            int f2 = ensureFormazione(c, sq2, g);

                            psScontro.setInt(1, f1);
                            psScontro.setInt(2, f2);
                            psScontro.setInt(3, g);
                            psScontro.setInt(4, idLega);
                            psScontro.addBatch();
                        }

                        // ripristina ordine per la prossima giornata di ritorno
                        order = new ArrayList<>(baseOrder);
                    }

                    psScontro.executeBatch();
                }

                c.commit();
                System.out.println("Calendario completo generato: " + giornateTot + " giornate");
                return true;

            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore generazione calendario completo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Avvio manuale: verifica prerequisiti, genera calendario completo e mette la lega IN_CORSO.
     */
    public boolean avviaLegaManualmente(int idLega) {
        if (!verificaAvvioLega(idLega)) return false;
        if (!tutteFormazioniPresentiPerGiornata(idLega, 1)) {
            System.err.println("Non tutte le squadre hanno consegnato la formazione della 1ª giornata.");
            return false;
        }
        List<Integer> squadreIds = getSquadreCompleteLega(idLega);
        if (!generaCalendarioCompleto(idLega, squadreIds)) return false;
        return avviaLega(idLega);
    }


    /**
     * Avvia la lega: imposta Stato in LEGA e programma la prima giornata in SCONTRO.
     * (LEGA non ha Data_Inizio/Giornata_Corrente nello schema)
     */
    public boolean avviaLega(int idLega) {
        final String updLega = "UPDATE LEGA SET Stato = 'IN_CORSO' WHERE ID_Lega = ?";
        final String updScontriPrima = """
            UPDATE SCONTRO
            SET Data_inizio = ?, Stato = 'PROGRAMMATO'
            WHERE ID_Lega = ? AND Numero_Giornata = 1
        """;

        try (Connection c = dbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(updLega)) {
                    ps.setInt(1, idLega);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(updScontriPrima)) {
                    ps.setObject(1, LocalDateTime.now().plusMinutes(1));
                    ps.setInt(2, idLega);
                    ps.executeUpdate();
                }

                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore avvio lega: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se ci sono almeno 2 e al massimo 12 squadre completate, in numero pari.
     */
    public boolean verificaAvvioLega(int idLega) {
        final String sql = """
            SELECT COUNT(*) AS num_squadre
            FROM SQUADRA_FANTACALCIO
            WHERE ID_Lega = ? AND Completata = TRUE
        """;
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int n = rs.getInt(1);
                    return n >= 2 && n <= 12 && n % 2 == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore verifica avvio lega: " + e.getMessage());
        }
        return false;
    }

    /**
     * Stato lega
     */
    public String getStatoLega(int idLega) {
        final String sql = "SELECT Stato FROM LEGA WHERE ID_Lega = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            System.err.println("Errore lettura stato lega: " + e.getMessage());
        }
        return "CREATA";
    }

    /**
     * Giornata “corrente”: la minima Numero_Giornata con scontri non COMPLETATI per quella lega.
     */
    public int getGiornataCorrente(int idLega) {
        final String sql = """
            SELECT MIN(Numero_Giornata)
            FROM SCONTRO
            WHERE ID_Lega = ? AND Stato <> 'COMPLETATO'
        """;
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) != 0) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Errore lettura giornata corrente: " + e.getMessage());
        }
        return 1;
    }

    /**
     * Max giornate presenti in calendario per la lega.
     */
    private int getMaxGiornate(int idLega) {
        final String sql = "SELECT MAX(Numero_Giornata) FROM SCONTRO WHERE ID_Lega = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Programma una data di inizio per la prossima giornata (in base a getGiornataCorrente).
     */
    public boolean programmaProssimaGiornata(int idLega, LocalDateTime dataInizio) {
        int g = getGiornataCorrente(idLega);
        final String sql = """
            UPDATE SCONTRO
            SET Data_inizio = ?, Stato = 'PROGRAMMATO'
            WHERE ID_Lega = ? AND Numero_Giornata = ?
        """;
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, dataInizio);
            ps.setInt(2, idLega);
            ps.setInt(3, g);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Errore programmazione prossima giornata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Aggiorna il risultato (stringa, es. "2-1") e imposta COMPLETATO.
     */
    public boolean aggiornaRisultato(int idScontro, String risultato) {
        final String sql = """
            UPDATE SCONTRO
            SET Risultato = ?, Stato = 'COMPLETATO'
            WHERE ID_Scontro = ?
        """;
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, risultato);
            ps.setInt(2, idScontro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento risultato: " + e.getMessage());
            return false;
        }
    }

    /**
     * Avanza alla prossima giornata “logica”: qui non c'è un campo in LEGA,
     * quindi è un no-op che ritorna true se esistono ancora giornate da giocare.
     */
    public boolean avanzaGiornata(int idLega) {
        int corrente = getGiornataCorrente(idLega);
        int max = getMaxGiornate(idLega);
        return corrente <= max;
    }

    /**
     * (Opzionale) Simula tutte le giornate: qui ti lascio lo scheletro senza MatchSimulationService,
     * perché lo schema non conserva Giornata_Corrente su LEGA.
     */
    public void simulaTutteLeGiornate(int idLega) {
        if (!"IN_CORSO".equals(getStatoLega(idLega))) {
            System.out.println("La lega non è in corso");
            return;
        }
        int g = getGiornataCorrente(idLega);
        int max = getMaxGiornate(idLega);
        for (; g <= max; g++) {
            // qui richiamerai il tuo simulatore esterno passando idLega e g
            // es. simulator.simulaGiornata(idLega, g);
            System.out.println("Simulata giornata " + g + "/" + max);
        }
        terminaLega(idLega);
    }

    private void terminaLega(int idLega) {
        final String sql = "UPDATE LEGA SET Stato = 'TERMINATA' WHERE ID_Lega = ?";
        try (Connection c = dbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            ps.executeUpdate();
            System.out.println("Lega " + idLega + " terminata");
        } catch (SQLException e) {
            System.err.println("Errore terminazione lega: " + e.getMessage());
        }
    }
}
