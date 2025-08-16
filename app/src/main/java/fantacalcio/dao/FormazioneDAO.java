package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * Restituisce l'oggetto Formazione (modulo + numero giornata)
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
                    return f;
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore lettura formazione: " + e.getMessage());
        }
        return null;
    }

    /**
     * Restituisce l'id della formazione schierata da una squadra in una giornata
     * (tabella SCHIERAMENTO)
     */
    public Integer getFormazioneSquadraGiornata(int idSquadra, int numeroGiornata) {
        String sql = """
            SELECT ID_Formazione 
            FROM SCHIERAMENTO 
            WHERE ID_Squadra = ? AND Numero_Giornata = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSquadra);
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

    /**
     * Recupera tutti i calciatori TITOLARI di una formazione (Panchina = 'NO')
     */
    public List<Calciatore> trovaTitolari(int idFormazione) {
        return trovaCalciatoriByFormazione(idFormazione, false);
    }

    /**
     * Recupera tutti i calciatori PANCHINARI di una formazione (Panchina = 'SI')
     */
    public List<Calciatore> trovaPanchinari(int idFormazione) {
        return trovaCalciatoriByFormazione(idFormazione, true);
    }

    // ---- Metodo interno che esegue la query ----
    private List<Calciatore> trovaCalciatoriByFormazione(int idFormazione, boolean panchina) {
        List<Calciatore> calciatori = new ArrayList<>();

        String sql = """
            SELECT c.* FROM CALCIATORE c
            JOIN FORMANO f ON c.ID_Calciatore = f.ID_Calciatore
            WHERE f.ID_Formazione = ? AND f.Panchina = ?
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFormazione);
            stmt.setString(2, panchina ? "SI" : "NO");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Calciatore c = new Calciatore(
                            rs.getInt("ID_Calciatore"),
                            rs.getString("Nome"),
                            rs.getString("Cognome"),
                            Calciatore.Ruolo.valueOf(rs.getString("Ruolo")),
                            rs.getInt("Costo")
                    );
                    calciatori.add(c);
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore recupero calciatori della formazione: " + e.getMessage());
        }

        return calciatori;
    }
}
