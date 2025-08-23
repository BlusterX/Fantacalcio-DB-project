package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import fantacalcio.model.VotoGiornata;
import fantacalcio.util.DatabaseConnection;

public class VotoGiornataDAO {

    private final DatabaseConnection dbConnection;

    public VotoGiornataDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * INSERT semplice in VOTO_GIORNATA.
     * La PK (Numero_Giornata, ID_Calciatore) impedisce duplicati.
     */
    public boolean inserisciVoto(VotoGiornata voto) {
        final String sql = """
            INSERT INTO VOTO_GIORNATA (ID_Calciatore, Numero_Giornata, Voto_base)
            VALUES (?, ?, ?)
        """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voto.getIdCalciatore());
            stmt.setInt(2, voto.getNumeroGiornata());
            stmt.setDouble(3, voto.getVotoBase());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Voto salvato -> Calciatore " + voto.getIdCalciatore()
                        + ", giornata " + voto.getNumeroGiornata());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore inserimento voto giornata: " + e.getMessage());
        }
        return false;
    }

    /**
     * Upsert: inserisce o aggiorna Voto_base se esiste già quella PK.
     */
    public boolean upsertVoto(VotoGiornata voto) {
        final String sql = """
            INSERT INTO VOTO_GIORNATA (ID_Calciatore, Numero_Giornata, Voto_base)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE Voto_base = VALUES(Voto_base)
        """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voto.getIdCalciatore());
            stmt.setInt(2, voto.getNumeroGiornata());
            stmt.setDouble(3, voto.getVotoBase());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Errore upsert voto giornata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera il voto base del calciatore in una determinata giornata
     */
    public VotoGiornata trovaVoto(int idCalciatore, int numeroGiornata) {
        final String sql = """
            SELECT Numero_Giornata, ID_Calciatore, Voto_base
            FROM VOTO_GIORNATA
            WHERE ID_Calciatore = ? AND Numero_Giornata = ?
        """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCalciatore);
            stmt.setInt(2, numeroGiornata);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creaVotoDaResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca voto giornata: " + e.getMessage());
        }
        return null;
    }

    /**
     * Aggiorna il voto base (dato PK composta)
     */
    public boolean aggiornaVotoBase(int idCalciatore, int numeroGiornata, double nuovoVotoBase) {
        final String sql = """
            UPDATE VOTO_GIORNATA
            SET Voto_base = ?
            WHERE ID_Calciatore = ? AND Numero_Giornata = ?
        """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nuovoVotoBase);
            stmt.setInt(2, idCalciatore);
            stmt.setInt(3, numeroGiornata);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento voto giornata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancella il voto (dato PK composta)
     */
    public boolean eliminaVoto(int idCalciatore, int numeroGiornata) {
        final String sql = """
            DELETE FROM VOTO_GIORNATA
            WHERE ID_Calciatore = ? AND Numero_Giornata = ?
        """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCalciatore);
            stmt.setInt(2, numeroGiornata);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore eliminazione voto giornata: " + e.getMessage());
            return false;
        }
    }

    public int cancellaVoto(int idCalciatore, int numeroGiornata) {
        final String sql = "DELETE FROM VOTO_GIORNATA WHERE ID_Calciatore = ? AND Numero_Giornata = ?";
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCalciatore);
            stmt.setInt(2, numeroGiornata);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore cancellaVoto: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Mapping ResultSet -> VotoGiornata
     * NB: il DB salva solo il Voto_base; gli altri campi del model sono calcolati/gestiti app.
     */
    private VotoGiornata creaVotoDaResultSet(ResultSet rs) throws SQLException {
        VotoGiornata v = new VotoGiornata();
        v.setIdCalciatore(rs.getInt("ID_Calciatore"));
        v.setNumeroGiornata(rs.getInt("Numero_Giornata"));
        v.setVotoBase(rs.getDouble("Voto_base"));
        // gli altri campi (minuti, gol, ecc.) restano ai default del model
        return v;
    }
}
