package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fantacalcio.model.PunteggioGiocatore;
import fantacalcio.util.DatabaseConnection;

public class PunteggioGiocatoreDAO {

    private final DatabaseConnection dbConnection;

    public PunteggioGiocatoreDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inserisce un evento (bonus/malus) per un calciatore in una giornata
     */
    public boolean inserisciEvento(int idCalciatore, int idBonusMalus, int numeroGiornata, int quantita) {
        String sql = """
            INSERT INTO PUNTEGGIO (ID_Calciatore, ID_Bonus_Malus, Numero_Giornata, Quantità)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCalciatore);
            stmt.setInt(2, idBonusMalus);
            stmt.setInt(3, numeroGiornata);
            stmt.setInt(4, quantita);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Evento registrato -> calciatore " + idCalciatore +
                                   ", bonus/malus " + idBonusMalus +
                                   ", giornata " + numeroGiornata +
                                   ", quantità " + quantita);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore inserimento evento punteggio: " + e.getMessage());
        }
        return false;
    }

    /**
     * Restituisce tutti gli eventi (bonus/malus) di un calciatore in una giornata
     */
    public List<PunteggioGiocatore> trovaEventiCalciatore(int idCalciatore, int numeroGiornata) {
        List<PunteggioGiocatore> eventi = new ArrayList<>();
        String sql = """
            SELECT * FROM PUNTEGGIO
            WHERE ID_Calciatore = ? AND Numero_Giornata = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCalciatore);
            stmt.setInt(2, numeroGiornata);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventi.add(creaEventoDaResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore recupero eventi calciatore: " + e.getMessage());
        }

        return eventi;
    }

    /**
     * Restituisce il valore numerico del bonus/malus
     */
    public double getValoreBonus(int idBonusMalus) {
        String sql = "SELECT Punteggio FROM BONUS_MALUS WHERE ID_Bonus_Malus = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idBonusMalus);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Punteggio");
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore lettura valore bonus/malus: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Metodo helper per creare oggetto PunteggioGiocatore da ResultSet
     */
    private PunteggioGiocatore creaEventoDaResultSet(ResultSet rs) throws SQLException {
        return new PunteggioGiocatore(
                rs.getInt("ID_Calciatore"),
                rs.getInt("ID_Bonus_Malus"),
                rs.getInt("Numero_Giornata"),
                rs.getInt("Quantità")
        );
    }
}
