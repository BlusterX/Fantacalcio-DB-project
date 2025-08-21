package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.Lega;
import fantacalcio.util.DatabaseConnection;

/**
 * Data Access Object per la gestione delle leghe
 */
public class LegaDAO {

    private final DatabaseConnection dbConnection;

    public LegaDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Crea una nuova lega
     */
    public boolean creaLega(Lega lega) {
        String sql = """
            INSERT INTO LEGA (Nome, Codice_accesso, Stato, ID_Campionato, ID_Utente)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        // forza unicità applicativa del codice
        String codice = generaCodiceUnico(conn, lega.getCodiceAccesso());
        lega.setCodiceAccesso(codice);

        stmt.setString(1, lega.getNome());
        stmt.setString(2, codice);
        stmt.setString(3, lega.getStato()); // deve essere 'CREATA'/'IN_CORSO'/'TERMINATA'
        if (lega.getIdCampionato() != null) stmt.setInt(4, lega.getIdCampionato());
        else stmt.setNull(4, java.sql.Types.INTEGER);
        stmt.setInt(5, lega.getIdAdmin());

            int righeInserite = stmt.executeUpdate();
            if (righeInserite > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        lega.setIdLega(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Lega creata: " + lega.getNome() + " (" + lega.getCodiceAccesso() + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore creazione lega: " + e.getMessage());
        }
        return false;
    }

    /**
     * Trova una lega per codice di accesso
     */
    public Optional<Lega> trovaLegaPerCodice(String codiceAccesso) {
        String sql = "SELECT * FROM LEGA WHERE UPPER(Codice_accesso) = ? ORDER BY ID_Lega DESC LIMIT 1";
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codiceAccesso.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(creaLegaDaResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca lega per codice: " + e.getMessage());
        }
        return Optional.empty();
    }


    /**
     * Trova tutte le leghe create da un admin
     */
    public List<Lega> trovaLeghePerAdmin(int idAdmin) {
        List<Lega> leghe = new ArrayList<>();
        String sql = "SELECT * FROM LEGA WHERE ID_Utente = ? ORDER BY Nome";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAdmin);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leghe.add(creaLegaDaResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore ricerca leghe per admin: " + e.getMessage());
        }
        return leghe;
    }

    public int contaPartecipanti(int idLega) {
        String sql = "SELECT COUNT(DISTINCT ID_Utente) AS c FROM SQUADRA_FANTACALCIO WHERE ID_Lega = ?";
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, idLega);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt("c");
            }
        } catch (SQLException e) {
            System.err.println("Errore conta partecipanti: " + e.getMessage());
        }
        return 0;
    }


    /**
     * Trova tutte le leghe (partecipazioni non ancora gestite)
     */
    public List<Lega> trovaLeghePerUtente(int idUtente) {
        List<Lega> leghe = new ArrayList<>();
        String sql = """
            SELECT DISTINCT l.*
            FROM LEGA l
            LEFT JOIN SQUADRA_FANTACALCIO sf ON sf.ID_Lega = l.ID_Lega
            WHERE l.ID_Utente = ? OR sf.ID_Utente = ?
            ORDER BY l.Nome
            """;
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtente);
            stmt.setInt(2, idUtente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) leghe.add(creaLegaDaResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca leghe per utente: " + e.getMessage());
        }
        return leghe;
    }


    /**
     * Elimina una lega (solo se è l'admin che l'ha creata)
     */
    public boolean eliminaLega(int idLega, int idAdmin) {
        String sql = "DELETE FROM LEGA WHERE ID_Lega = ? AND ID_Utente = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idLega);
            stmt.setInt(2, idAdmin);

            int righeEliminate = stmt.executeUpdate();
            if (righeEliminate > 0) {
                System.out.println("Lega eliminata (ID: " + idLega + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore eliminazione lega: " + e.getMessage());
        }
        return false;
    }

    /**
     * Aggiorna una lega
     */
    public boolean aggiornaLega(Lega lega) {
        String sql = """
            UPDATE LEGA
            SET Nome = ?, Stato = ?, ID_Campionato = ?
            WHERE ID_Lega = ? AND ID_Utente = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lega.getNome());
            stmt.setString(2, lega.getStato());
            if (lega.getIdCampionato() != null) {
                stmt.setInt(3, lega.getIdCampionato());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setInt(4, lega.getIdLega());
            stmt.setInt(5, lega.getIdAdmin());

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Lega aggiornata: " + lega.getNome());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore aggiornamento lega: " + e.getMessage());
        }
        return false;
    }

    /**
     * Crea un oggetto Lega a partire dal ResultSet
     */
    private Lega creaLegaDaResultSet(ResultSet rs) throws SQLException {
        return new Lega(
                rs.getInt("ID_Lega"),
                rs.getString("Nome"),
                rs.getString("Codice_accesso"),
                rs.getInt("ID_Utente"),
                rs.getString("Stato"),
                rs.getObject("ID_Campionato") != null ? rs.getInt("ID_Campionato") : null
        );
    }

    private boolean codiceEsiste(Connection conn, String code) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(
                "SELECT 1 FROM LEGA WHERE Codice_accesso = ? LIMIT 1")) {
            st.setString(1, code);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String generaCodiceUnico(Connection conn, String seed) throws SQLException {
        String code = (seed == null || seed.isBlank()) ? null : seed;
        int tentativi = 0;
        while (code == null || codiceEsiste(conn, code)) {
            // 6 char alfanumerico maiuscolo (compatibile con VARCHAR(10))
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder b = new StringBuilder(6);
            java.util.Random r = new java.util.Random();
            for (int i = 0; i < 6; i++) b.append(chars.charAt(r.nextInt(chars.length())));
            code = b.toString();
            if (++tentativi > 50) throw new SQLException("Impossibile generare codice lega univoco");
        }
        return code;
    }
}
