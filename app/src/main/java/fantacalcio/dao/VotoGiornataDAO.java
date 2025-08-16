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
     * Inserisce un voto nella tabella Voto_Giornata
     */
    public boolean inserisciVoto(VotoGiornata voto) {
        String sql = """
            INSERT INTO Voto_Giornata (id_calciatore, numero_giornata, voto_base, minuti_giocati)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voto.getIdCalciatore());
            stmt.setInt(2, voto.getNumeroGiornata());
            stmt.setDouble(3, voto.getVotoBase());
            stmt.setInt(4, voto.getMinutiGiocati());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Voto salvato -> Calciatore " 
                    + voto.getIdCalciatore() + ", giornata " + voto.getNumeroGiornata());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore inserimento voto giornata: " + e.getMessage());
        }
        return false;
    }

    /**
     * Recupera il voto base del calciatore in una determinata giornata
     */
    public VotoGiornata trovaVoto(int idCalciatore, int numeroGiornata) {
        String sql = "SELECT * FROM Voto_Giornata WHERE id_calciatore = ? AND numero_giornata = ?";

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
     * Metodo helper per creare un VotoGiornata da ResultSet
     */
    private VotoGiornata creaVotoDaResultSet(ResultSet rs) throws SQLException {
        VotoGiornata v = new VotoGiornata();
        v.setIdVoto(rs.getInt("id_voto"));
        v.setIdCalciatore(rs.getInt("id_calciatore"));
        v.setNumeroGiornata(rs.getInt("numero_giornata"));
        v.setVotoBase(rs.getDouble("voto_base"));
        v.setMinutiGiocati(rs.getInt("minuti_giocati"));
        return v;
    }
}
