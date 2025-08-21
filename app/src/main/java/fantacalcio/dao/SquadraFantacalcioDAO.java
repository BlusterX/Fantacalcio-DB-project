package fantacalcio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.util.DatabaseConnection;

public class SquadraFantacalcioDAO {

    private final DatabaseConnection dbConnection;
    private final CalciatoreDAO calciatoreDAO;

    public SquadraFantacalcioDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.calciatoreDAO = new CalciatoreDAO();
    }

    /**
     * Crea una nuova squadra per (utente, lega).
     * Flow: verifica unicità -> INSERT CLASSIFICA -> INSERT SQUADRA_FANTACALCIO (con ID_Classifica)
     */
    public boolean creaSquadraPerLega(SquadraFantacalcio squadra, int idUtente, int idLega) {

        if (trovaSquadraUtentePerLega(idUtente, idLega).isPresent()) {
            System.err.println("L'utente ha già una squadra in questa lega!");
            return false;
        }

        final String sqlInsClassifica = "INSERT INTO CLASSIFICA (Punti, Vittorie, Pareggi, Sconfitte, Punteggio_totale, Punteggio_subito) VALUES (0,0,0,0,0,0)";
        final String sqlInsSquadra = """
            INSERT INTO SQUADRA_FANTACALCIO
                (ID_Classifica, Nome, Budget_totale, Budget_rimanente, Completata, ID_Utente, ID_Lega)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1) Crea classifica
                Integer idClassifica = null;
                try (PreparedStatement ps = conn.prepareStatement(sqlInsClassifica, Statement.RETURN_GENERATED_KEYS)) {
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) idClassifica = gk.getInt(1);
                    }
                }
                if (idClassifica == null) throw new SQLException("Impossibile generare ID_Classifica");

                // 2) Inserisci squadra (lasciando Data_creazione/ultima_modifica ai default del DB)
                try (PreparedStatement ps = conn.prepareStatement(sqlInsSquadra, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idClassifica);
                    ps.setString(2, squadra.getNomeSquadra());
                    ps.setInt(3, squadra.getBudgetTotale());
                    ps.setInt(4, squadra.getBudgetRimanente());
                    ps.setBoolean(5, squadra.isCompletata());
                    ps.setInt(6, idUtente);
                    ps.setInt(7, idLega);

                    int rows = ps.executeUpdate();
                    if (rows == 0) throw new SQLException("Insert SQUADRA_FANTACALCIO fallito");

                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            squadra.setIdSquadraFantacalcio(gk.getInt(1));
                            squadra.setIdClassifica(idClassifica);
                            squadra.setIdUtente(idUtente);
                            squadra.setIdLega(idLega);
                        }
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore creazione squadra: " + e.getMessage());
            return false;
        }
    }

    /**
     * Collega/riassegna la squadra a una lega aggiornando il campo ID_Lega.
     */
    public boolean collegaSquadraALega(int idSquadraFant, int idLega) {
        final String sql = "UPDATE SQUADRA_FANTACALCIO SET ID_Lega = ? WHERE ID_Squadra_Fantacalcio = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLega);
            ps.setInt(2, idSquadraFant);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Squadra " + idSquadraFant + " collegata alla lega " + idLega);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore collegamento squadra-lega: " + e.getMessage());
        }
        return false;
    }

    /**
     * Trova una squadra per ID_Squadra_Fantacalcio
     */
    public Optional<SquadraFantacalcio> trovaSquadraPerId(int idSquadraFant) {
        final String sql = "SELECT * FROM SQUADRA_FANTACALCIO WHERE ID_Squadra_Fantacalcio = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSquadraFant);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SquadraFantacalcio s = creaSquadraDaResultSet(rs);
                    caricaCalciatori(s);
                    return Optional.of(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca squadra per ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Trova tutte le squadre di un utente
     */
    public List<SquadraFantacalcio> trovaSquadrePerUtente(int idUtente) {
        List<SquadraFantacalcio> out = new ArrayList<>();
        final String sql = """
            SELECT *
            FROM SQUADRA_FANTACALCIO
            WHERE ID_Utente = ?
            ORDER BY Data_creazione DESC
        """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SquadraFantacalcio s = creaSquadraDaResultSet(rs);
                    caricaCalciatori(s);
                    out.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca squadre per utente: " + e.getMessage());
        }
        return out;
    }

    /**
     * Aggiorna una squadra esistente (nome, budget_rimanente, completata).
     * Data_ultima_modifica è gestita dal DB con ON UPDATE.
     */
    public boolean aggiornaSquadra(SquadraFantacalcio squadra) {
        final String sql = """
            UPDATE SQUADRA_FANTACALCIO
            SET Nome = ?, Budget_rimanente = ?, Completata = ?
            WHERE ID_Squadra_Fantacalcio = ?
        """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, squadra.getNomeSquadra());
            ps.setInt(2, squadra.getBudgetRimanente());
            ps.setBoolean(3, squadra.isCompletata());
            ps.setInt(4, squadra.getIdSquadraFantacalcio());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Squadra aggiornata: " + squadra.getNomeSquadra());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento squadra: " + e.getMessage());
        }
        return false;
    }

    /**
     * Elimina una squadra (FKs su COMPOSIZIONE/FORMZIONE/SCONTRO potrebbero richiedere ON DELETE o cleanup preventivo)
     */
    public boolean eliminaSquadra(int idSquadraFant) {
        final String deleteComposizione = "DELETE FROM COMPOSIZIONE WHERE ID_Squadra_Fantacalcio = ?";
        final String deleteFormazione   = "DELETE FROM FORMAZIONE WHERE ID_Squadra_Fantacalcio = ?";
        final String deleteSquadra      = "DELETE FROM SQUADRA_FANTACALCIO WHERE ID_Squadra_Fantacalcio = ?";

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1) Cancella composizioni
                try (PreparedStatement ps = conn.prepareStatement(deleteComposizione)) {
                    ps.setInt(1, idSquadraFant);
                    ps.executeUpdate();
                }
                // 2) Cancella formazioni
                try (PreparedStatement ps = conn.prepareStatement(deleteFormazione)) {
                    ps.setInt(1, idSquadraFant);
                    ps.executeUpdate();
                }
                // NB: gli SCONTRI collegati a quelle formazioni rimangono, 
                // ma se vuoi davvero eliminarli dovresti fare un join su ID_Formazione1/2 e cancellarli prima.
                // 3) Cancella la squadra
                int rows;
                try (PreparedStatement ps = conn.prepareStatement(deleteSquadra)) {
                    ps.setInt(1, idSquadraFant);
                    rows = ps.executeUpdate();
                }

                conn.commit();
                if (rows > 0) {
                    System.out.println("Squadra eliminata (ID: " + idSquadraFant + ")");
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore eliminazione squadra: " + e.getMessage());
        }
        return false;
    }


    /**
     * Aggiunge un calciatore alla squadra (COMPOSIZIONE su ID_Squadra_Fantacalcio)
     */
    public boolean aggiungiCalciatoreAllaSquadra(int idSquadraFant, int idCalciatore) {
        final String sql = "INSERT INTO COMPOSIZIONE (ID_Calciatore, ID_Squadra_Fantacalcio) VALUES (?, ?)";
        System.out.println("DEBUG: Aggiunta calciatore - Squadra ID: " + idSquadraFant + ", Calciatore ID: " + idCalciatore);
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCalciatore);
            ps.setInt(2, idSquadraFant);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Calciatore aggiunto (sq: " + idSquadraFant + ", cal: " + idCalciatore + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore aggiunta calciatore alla squadra: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                System.err.println("   Calciatore già presente nella squadra!");
            }
        }
        return false;
    }

    /**
     * Rimuove un calciatore dalla squadra
     */
    public boolean rimuoviCalciatoreDallaSquadra(int idSquadraFant, int idCalciatore) {
        final String sql = "DELETE FROM COMPOSIZIONE WHERE ID_Squadra_Fantacalcio = ? AND ID_Calciatore = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSquadraFant);
            ps.setInt(2, idCalciatore);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Calciatore rimosso (sq: " + idSquadraFant + ", cal: " + idCalciatore + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore rimozione calciatore dalla squadra: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica se un nome squadra è già usato da un utente
     */
    public boolean nomeSquadraEsiste(String nomeSquadra, int idUtente) {
        final String sql = "SELECT COUNT(*) FROM SQUADRA_FANTACALCIO WHERE Nome = ? AND ID_Utente = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeSquadra);
            ps.setInt(2, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Errore verifica nome squadra: " + e.getMessage());
        }
        return false;
    }

    /**
     * Conta il numero di squadre create da un utente
     */
    public int contaSquadreUtente(int idUtente) {
        final String sql = "SELECT COUNT(*) FROM SQUADRA_FANTACALCIO WHERE ID_Utente = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Errore conteggio squadre utente: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Carica i calciatori della squadra
     */
    private void caricaCalciatori(SquadraFantacalcio squadra) {
        if (squadra.getIdSquadraFantacalcio() <= 0) {
            squadra.setCalciatori(new ArrayList<>());
            return;
        }
        final String sql = """
            SELECT c.*
            FROM CALCIATORE c
            JOIN COMPOSIZIONE cs ON c.ID_Calciatore = cs.ID_Calciatore
            WHERE cs.ID_Squadra_Fantacalcio = ?
            ORDER BY c.Ruolo, c.Cognome, c.Nome
        """;
        List<Calciatore> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, squadra.getIdSquadraFantacalcio());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ruoloDb = rs.getString("Ruolo");
                    Calciatore.Ruolo ruolo;
                    if ("P".equals(ruoloDb))      ruolo = Calciatore.Ruolo.PORTIERE;
                    else if ("D".equals(ruoloDb)) ruolo = Calciatore.Ruolo.DIFENSORE;
                    else if ("C".equals(ruoloDb)) ruolo = Calciatore.Ruolo.CENTROCAMPISTA;
                    else                          ruolo = Calciatore.Ruolo.ATTACCANTE;

                    Calciatore calciatore = new Calciatore(
                        rs.getInt("ID_Calciatore"),
                        rs.getString("Nome"),
                        rs.getString("Cognome"),
                        ruolo,                       // mappato da 'P','D','C','A'
                        rs.getInt("Costo"),
                        rs.getBoolean("Infortunato"),
                        rs.getBoolean("Squalificato"),
                        rs.getInt("ID_Squadra")
                    );
                    list.add(calciatore);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore caricamento calciatori squadra: " + e.getMessage());
            e.printStackTrace();
        }
        squadra.setCalciatori(list);
    }

    /**
     * Trova la squadra dell'utente in una data lega
     */
    public Optional<SquadraFantacalcio> trovaSquadraUtentePerLega(int idUtente, int idLega) {
        final String sql = """
            SELECT *
            FROM SQUADRA_FANTACALCIO
            WHERE ID_Utente = ? AND ID_Lega = ?
        """;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ps.setInt(2, idLega);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SquadraFantacalcio s = creaSquadraDaResultSet(rs);
                    caricaCalciatori(s);
                    return Optional.of(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca squadra utente per lega: " + e.getMessage());
        }
        return Optional.empty();
    }

    private SquadraFantacalcio creaSquadraDaResultSet(ResultSet rs) throws SQLException {
        // NB: le colonne Data_* sono gestite dal DB; le leggiamo se servono
        Timestamp tc = rs.getTimestamp("Data_creazione");
        Timestamp tu = rs.getTimestamp("Data_ultima_modifica");
        LocalDateTime dc = (tc != null) ? tc.toLocalDateTime() : LocalDateTime.now();
        LocalDateTime du = (tu != null) ? tu.toLocalDateTime() : LocalDateTime.now();

        return new SquadraFantacalcio(
            rs.getInt("ID_Squadra_Fantacalcio"),
            rs.getString("Nome"),
            rs.getInt("Budget_totale"),
            rs.getInt("Budget_rimanente"),
            dc,
            du,
            rs.getBoolean("Completata"),
            rs.getInt("ID_Classifica"),
            rs.getInt("ID_Utente"),
            rs.getInt("ID_Lega")
        );
    }
}
