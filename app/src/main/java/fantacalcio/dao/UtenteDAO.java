package fantacalcio.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fantacalcio.model.Utente;
import fantacalcio.util.DatabaseConnection;

public class UtenteDAO {

    private final DatabaseConnection dbConnection;

    public UtenteDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Inserisce un nuovo utente nel database
     */
    public boolean inserisciUtente(Utente utente) {
        String sql = """
            INSERT INTO UTENTE (Nome, Cognome, Nickname, Email, Numero, Data_di_nascita, Password)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getCognome());
            stmt.setString(3, utente.getNickname());
            stmt.setString(4, utente.getEmail());
            stmt.setString(5, utente.getNumero());
            stmt.setDate(6, Date.valueOf(utente.getDataDiNascita()));
            stmt.setString(7, utente.getPassword());

            int righeInserite = stmt.executeUpdate();

            if (righeInserite > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) utente.setIdUtente(generatedKeys.getInt(1));
                }
                System.out.println("Utente inserito: " + utente.getNickname());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore inserimento utente: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                if (e.getMessage().toLowerCase().contains("nickname")) {
                    System.err.println("   Nickname già esistente!");
                } else if (e.getMessage().toLowerCase().contains("email")) {
                    System.err.println("   Email già esistente!");
                }
            }
        }
        return false;
    }

    /**
     * Trova un utente per ID
     */
    public Optional<Utente> trovaUtentePerId(int idUtente) {
        String sql = "SELECT * FROM UTENTE WHERE ID_Utente = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtente);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaUtenteDaResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore ricerca utente per ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Trova un utente per nickname
     */
    public Optional<Utente> trovaUtentePerNickname(String nickname) {
        String sql = "SELECT * FROM UTENTE WHERE Nickname = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nickname);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaUtenteDaResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore ricerca utente per nickname: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Trova un utente per email
     */
    public Optional<Utente> trovaUtentePerEmail(String email) {
        String sql = "SELECT * FROM UTENTE WHERE Email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(creaUtenteDaResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore ricerca utente per email: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Recupera tutti gli utenti
     */
    public List<Utente> trovaTuttiGliUtenti() {
        List<Utente> utenti = new ArrayList<>();
        String sql = "SELECT * FROM UTENTE ORDER BY Nickname";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                utenti.add(creaUtenteDaResultSet(rs));
            }

            System.out.println("Trovati " + utenti.size() + " utenti");

        } catch (SQLException e) {
            System.err.println("Errore recupero tutti gli utenti: " + e.getMessage());
        }
        return utenti;
    }

    /**
     * Aggiorna un utente esistente
     */
    public boolean aggiornaUtente(Utente utente) {
        String sql = """
            UPDATE UTENTE
            SET Nome = ?, Cognome = ?, Nickname = ?, Email = ?,
                Numero = ?, Data_di_nascita = ?, Password = ?
            WHERE ID_Utente = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getCognome());
            stmt.setString(3, utente.getNickname());
            stmt.setString(4, utente.getEmail());
            stmt.setString(5, utente.getNumero());
            stmt.setDate(6, Date.valueOf(utente.getDataDiNascita()));
            stmt.setString(7, utente.getPassword());
            stmt.setInt(8, utente.getIdUtente());

            int righeAggiornate = stmt.executeUpdate();

            if (righeAggiornate > 0) {
                System.out.println("Utente aggiornato: " + utente.getNickname());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore aggiornamento utente: " + e.getMessage());
        }
        return false;
    }

    /**
     * Elimina un utente e tutte le sue risorse collegate (squadre + dipendenze)
     *
     * Ordine:
     *  - per ogni squadra dell'utente:
     *      COMPOSIZIONE -> FORMANO -> SCONTRO -> FORMAZIONE -> SQUADRA_FANTACALCIO
     *  - poi UTENTE
     */
    public boolean eliminaUtente(int idUtente) {
        final String selSquadreUtente = "SELECT ID_Squadra_Fantacalcio FROM SQUADRA_FANTACALCIO WHERE ID_Utente = ?";

        final String delComposizione = "DELETE FROM COMPOSIZIONE WHERE ID_Squadra_Fantacalcio = ?";
        final String delFormanoByFormazioni = """
            DELETE FROM FORMANO
            WHERE ID_Formazione IN (SELECT ID_Formazione FROM FORMAZIONE WHERE ID_Squadra_Fantacalcio = ?)
        """;
        final String delScontroByFormazioni = """
            DELETE FROM SCONTRO
            WHERE ID_Formazione1 IN (SELECT ID_Formazione FROM FORMAZIONE WHERE ID_Squadra_Fantacalcio = ?)
               OR ID_Formazione2 IN (SELECT ID_Formazione FROM FORMAZIONE WHERE ID_Squadra_Fantacalcio = ?)
        """;
        final String delFormazioni = "DELETE FROM FORMAZIONE WHERE ID_Squadra_Fantacalcio = ?";
        final String delSquadraFant = "DELETE FROM SQUADRA_FANTACALCIO WHERE ID_Squadra_Fantacalcio = ?";
        final String delUtente = "DELETE FROM UTENTE WHERE ID_Utente = ?";

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Trova tutte le squadre dell'utente
                List<Integer> squadreIds = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(selSquadreUtente)) {
                    ps.setInt(1, idUtente);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            squadreIds.add(rs.getInt(1));
                        }
                    }
                }

                // Per ciascuna squadra: cancella in cascata "manuale"
                for (int idSquadraFant : squadreIds) {
                    try (PreparedStatement p1 = conn.prepareStatement(delComposizione)) {
                        p1.setInt(1, idSquadraFant);
                        p1.executeUpdate();
                    }
                    try (PreparedStatement p2 = conn.prepareStatement(delFormanoByFormazioni)) {
                        p2.setInt(1, idSquadraFant);
                        p2.executeUpdate();
                    }
                    try (PreparedStatement p3 = conn.prepareStatement(delScontroByFormazioni)) {
                        p3.setInt(1, idSquadraFant);
                        p3.setInt(2, idSquadraFant);
                        p3.executeUpdate();
                    }
                    try (PreparedStatement p4 = conn.prepareStatement(delFormazioni)) {
                        p4.setInt(1, idSquadraFant);
                        p4.executeUpdate();
                    }
                    try (PreparedStatement p5 = conn.prepareStatement(delSquadraFant)) {
                        p5.setInt(1, idSquadraFant);
                        p5.executeUpdate();
                    }
                }

                // Infine elimina l'utente
                int rows;
                try (PreparedStatement p6 = conn.prepareStatement(delUtente)) {
                    p6.setInt(1, idUtente);
                    rows = p6.executeUpdate();
                }

                conn.commit();
                if (rows > 0) {
                    System.out.println("Utente eliminato (ID: " + idUtente + ")");
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore eliminazione utente: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica login utente (plaintext — valuta hashing in produzione)
     */
    public Optional<Utente> verificaLogin(String nickname, String password) {
        String sql = "SELECT * FROM UTENTE WHERE Nickname = ? AND Password = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nickname);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Login successful per: " + nickname);
                    return Optional.of(creaUtenteDaResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore verifica login: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Conta il numero totale di utenti
     */
    public int contaUtenti() {
        String sql = "SELECT COUNT(*) FROM UTENTE";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Errore conteggio utenti: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Helper per creare un oggetto Utente da un ResultSet
     */
    private Utente creaUtenteDaResultSet(ResultSet rs) throws SQLException {
        return new Utente(
            rs.getInt("ID_Utente"),
            rs.getString("Nome"),
            rs.getString("Cognome"),
            rs.getString("Nickname"),
            rs.getString("Email"),
            rs.getString("Numero"),
            rs.getDate("Data_di_nascita").toLocalDate(),
            rs.getString("Password")
        );
    }
}
