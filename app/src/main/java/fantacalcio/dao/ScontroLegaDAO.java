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
            JOIN FORMANO fo ON fo.ID_Formazione = f.ID_Formazione
            WHERE s.ID_Lega = ? AND f.Numero_Giornata = ? AND fo.Panchina = 'NO'
            GROUP BY f.ID_Formazione
            HAVING COUNT(fo.ID_Calciatore) = 11
        """;

        int partecipanti = 0, conFormazione = 0;
        try (var c = dbConnection.getConnection()) {
            try (var ps = c.prepareStatement(sqlPartecipanti)) {
                ps.setInt(1, idLega);
                try (var rs = ps.executeQuery()) { if (rs.next()) partecipanti = rs.getInt(1); }
            }
            try (var ps = c.prepareStatement(sqlConFormazione)) {
                ps.setInt(1, idLega);
                ps.setInt(2, giornata);
                try (var rs = ps.executeQuery()) { while (rs.next()) conFormazione++; }
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
                    VALUES (?, ?, '', 'PROGRAMMATO', NULL, ?, ?)
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


    /** quante giornate sono già presenti nel calendario di questa lega */
    public int contaGiornateCalendario(int idLega) {
        final String sql = "SELECT COUNT(DISTINCT Numero_Giornata) FROM SCONTRO WHERE ID_Lega = ?";
        try (Connection c = dbConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** elimina tutti gli scontri della lega (utile per ripartenze da calendario parziale) */
    public int svuotaCalendarioLega(int idLega) {
        final String del = "DELETE FROM SCONTRO WHERE ID_Lega = ?";
        try (Connection c = dbConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(del)) {
            ps.setInt(1, idLega);
            return ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
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
        if (!generaCalendario38(idLega, squadreIds)) return false;

        return avviaLega(idLega);
    }

    public List<Integer> listaGiornatePerLega(int idLega) {
        final String sql = "SELECT DISTINCT Numero_Giornata FROM SCONTRO WHERE ID_Lega = ? ORDER BY Numero_Giornata";
        List<Integer> out = new ArrayList<>();
        try (Connection c = dbConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Errore lista giornate: " + e.getMessage());
        }
        return out;
    }

    public boolean aggiornaRisultato(int idScontro, double punti1, double punti2) {
        String risultato = formatRisultato(punti1, punti2);
        final String sql = """
            UPDATE SCONTRO
            SET Risultato = ?, 
                Stato = 'COMPLETATO',
                Data_inizio = COALESCE(Data_inizio, CURRENT_TIMESTAMP)
            WHERE ID_Scontro = ?
        """;
        try (var c = dbConnection.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, risultato);
            ps.setInt(2, idScontro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento risultato: " + e.getMessage());
            return false;
        }
    }


    private static String formatRisultato(double p1, double p2) {
        int g1 = puntiToGol(p1);
        int g2 = puntiToGol(p2);
        return String.format("%d-%d (%.1f–%.1f)", g1, g2, p1, p2);
    }

    // classica soglia 66 = 1 gol, +6 per gol addizionale (modifica se vuoi)
    private static int puntiToGol(double punti) {
        if (punti < 66.0) return 0;
        return 1 + (int)Math.floor((punti - 66.0) / 6.0);
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
                if (rs.next()) {
                    int val = rs.getInt(1);
                    if (!rs.wasNull()) return val;   // c'è almeno una giornata ancora da giocare
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore lettura giornata corrente: " + e.getMessage());
        }
        int max = getMaxGiornate(idLega);
        return (max > 0) ? (max + 1) : 1;
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

    public int getMaxGiornatePublic(int idLega) {
        return getMaxGiornate(idLega);
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

    public boolean aggiornaRisultato(int idScontro, String risultato) {
        final String sql = """
            UPDATE SCONTRO
            SET Risultato = ?,
                Stato = 'COMPLETATO',
                Data_inizio = COALESCE(Data_inizio, CURRENT_TIMESTAMP)
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

    public boolean avanzaGiornata(int idLega) {
        int corrente = getGiornataCorrente(idLega);
        int max = getMaxGiornate(idLega);
        return corrente <= max;
    }

    public boolean esistonoScontriDaGiocare(int idLega) {
        final String sql = "SELECT COUNT(*) FROM SCONTRO WHERE ID_Lega = ? AND Stato <> 'COMPLETATO'";
        try (var c = dbConnection.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            try (var rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    // set di stato per TUTTI gli scontri di una giornata
    public int setStatoGiornata(int idLega, int giornata, fantacalcio.model.ScontroLega.StatoScontro stato) {
        final String sql = "UPDATE SCONTRO SET Stato = ? WHERE ID_Lega = ? AND Numero_Giornata = ?";
        try (var c = dbConnection.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, stato.name());
            ps.setInt(2, idLega);
            ps.setInt(3, giornata);
            return ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    // comodo per chiudere la lega
    public void setStatoLega(int idLega, String stato) {
        final String sql = "UPDATE LEGA SET Stato = ? WHERE ID_Lega = ?";
        try (var c = dbConnection.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, stato);
            ps.setInt(2, idLega);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void simulaTutteLeGiornate(int idLega) {
        if (!"IN_CORSO".equals(getStatoLega(idLega))) {
            System.out.println("La lega non è in corso");
            return;
        }
        int g = getGiornataCorrente(idLega);
        int max = getMaxGiornate(idLega);
        for (; g <= max; g++) {
            System.out.println("Simulata giornata " + g + "/" + max);
        }
        terminaLega(idLega);
    }

    /** true se esiste almeno uno scontro già creato per la lega */
    private boolean esistonoScontriPerLega(int idLega) {
        final String q = "SELECT 1 FROM SCONTRO WHERE ID_Lega = ? LIMIT 1";
        try (Connection c = dbConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, idLega);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            System.err.println("Errore esistonoScontriPerLega: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera un calendario di ESATTAMENTE 38 giornate per la lega.
     * Se N è dispari gestisce BYE; se N è piccolo (es. 2) ripete i turni alternando casa/trasferta
     * finché si raggiunge la giornata 38. Crea anche le FORMAZIONI "scheletro" per ogni giornata.
     */
    public boolean generaCalendario38(int idLega, List<Integer> squadreFantIds) {
        final int NUM_GIORNATE = 38;

        if (squadreFantIds == null || squadreFantIds.size() < 2) {
            System.err.println("[generaCalendario38] servono almeno 2 squadre");
            return false;
        }
        if (esistonoScontriPerLega(idLega)) {
            System.out.println("[generaCalendario38] calendario già presente, salto");
            return true;
        }

        // Assicura le giornate 1..38 (FK su FORMAZIONE.Numero_Giornata e SCONTRO.Numero_Giornata)
        new GiornataDAO().ensureGiornate38();

        // Costruisci round-robin base (algoritmo di Berger) – con BYE se dispari
        List<Integer> teams = new ArrayList<>(squadreFantIds);
        if (teams.size() % 2 != 0) teams.add(null); // BYE
        int n = teams.size();
        int giornateBase = n - 1;           // turni base
        int partitePerGiornata = n / 2;

        List<List<int[]>> roundBase = new ArrayList<>();
        List<Integer> rot = new ArrayList<>(teams);

        for (int g = 0; g < giornateBase; g++) {
            List<int[]> matches = new ArrayList<>();
            for (int i = 0; i < partitePerGiornata; i++) {
                Integer a = rot.get(i);
                Integer b = rot.get(n - 1 - i);
                if (a != null && b != null) matches.add(new int[]{a, b}); // a in casa, b fuori
            }
            roundBase.add(matches);
            // rotazione (mantieni fisso l'indice 0)
            Integer first = rot.get(0);
            Integer last  = rot.remove(rot.size() - 1);
            rot.add(1, last);
            rot.set(0, first);
        }

        // Replicazione dei turni base fino a 38, alternando casa/trasferta
        List<List<int[]>> giornate = new ArrayList<>();
        boolean invertHomeAway = false;
        while (giornate.size() < NUM_GIORNATE) {
            for (var g : roundBase) {
                if (giornate.size() >= NUM_GIORNATE) break;
                List<int[]> copia = new ArrayList<>(g.size());
                for (int[] p : g) {
                    copia.add(invertHomeAway ? new int[]{p[1], p[0]} : new int[]{p[0], p[1]});
                }
                giornate.add(copia);
            }
            invertHomeAway = !invertHomeAway;
        }

        // INSERT negli SCONTRO + ensure FORMAZIONE per (squadra, giornata)
        final String insScontro = """
            INSERT INTO SCONTRO
            (ID_Formazione1, ID_Formazione2, Risultato, Stato, Data_inizio, Numero_Giornata, ID_Lega)
            VALUES (?, ?, '', 'PROGRAMMATO', NULL, ?, ?)
        """;

        try (Connection c = dbConnection.getConnection();
            PreparedStatement psScontro = c.prepareStatement(insScontro)) {

            c.setAutoCommit(false);

            for (int gIdx = 0; gIdx < NUM_GIORNATE; gIdx++) {
                int numGiornata = gIdx + 1;

                for (int[] match : giornate.get(gIdx)) {
                    int idSq1 = match[0];
                    int idSq2 = match[1];

                    int f1 = ensureFormazione(c, idSq1, numGiornata);
                    int f2 = ensureFormazione(c, idSq2, numGiornata);

                    psScontro.setInt(1, f1);
                    psScontro.setInt(2, f2);
                    psScontro.setInt(3, numGiornata);
                    psScontro.setInt(4, idLega);
                    psScontro.addBatch();
                }
            }

            psScontro.executeBatch();
            c.commit();

            System.out.println("[generaCalendario38] creato calendario da 38 giornate per lega " + idLega);
            return true;

        } catch (SQLException e) {
            System.err.println("Errore generaCalendario38: " + e.getMessage());
            return false;
        }
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
