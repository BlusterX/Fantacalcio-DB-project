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
            INSERT INTO Punteggio_Giocatore (id_calciatore, id_bonus_malus, numero_giornata, quantita)
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

    public double getValoreBonus(int idBonusMalus) {
    String sql = "SELECT Punteggio FROM BONUS_MALUS WHERE ID_Bonus_Malus = ?";
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, idBonusMalus);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getDouble("Punteggio");
        }
    } catch (SQLException e) {
        
    }
        return 0;
    }

    /**
     * Restituisce tutti gli eventi (bonus/malus) di un calciatore in una giornata
     */
    public List<PunteggioGiocatore> trovaEventiCalciatore(int idCalciatore, int numeroGiornata) {
        List<PunteggioGiocatore> eventi = new ArrayList<>();
        String sql = """
            SELECT * FROM Punteggio_Giocatore
            WHERE id_calciatore = ? AND numero_giornata = ?
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
     * Metodo helper per creare oggetto PunteggioGiocatore da ResultSet
     */
    private PunteggioGiocatore creaEventoDaResultSet(ResultSet rs) throws SQLException {
        PunteggioGiocatore p = new PunteggioGiocatore();
        p.setId(rs.getInt("id_punteggio"));
        p.setIdCalciatore(rs.getInt("id_calciatore"));
        p.setIdBonusMalus(rs.getInt("id_bonus_malus"));
        p.setNumeroGiornata(rs.getInt("numero_giornata"));
        p.setQuantita(rs.getInt("quantita"));
        return p;
    }
}
