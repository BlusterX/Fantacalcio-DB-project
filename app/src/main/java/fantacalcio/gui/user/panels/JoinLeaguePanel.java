package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.LegaDAO;
import fantacalcio.dao.SquadraFantacalcioDAO;
import fantacalcio.gui.user.UserMainFrame;
import fantacalcio.model.Lega;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;
import fantacalcio.util.DatabaseConnection;

/**
 * Panel per permettere agli utenti di joinare le leghe (versione minimale)
 */
public final class JoinLeaguePanel extends JPanel {

    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final LegaDAO legaDAO;
    private final SquadraFantacalcioDAO squadraDAO;

    // GUI
    private JTable tabellaLeghePartecipate;
    private DefaultTableModel modelTabella;
    private JTextField txtCodiceAccesso;
    private JButton btnJoinLega, btnRefresh;

    // Cache dati
    private List<Lega> leghePartecipate;
    private List<SquadraFantacalcio> squadreUtente;

    public JoinLeaguePanel(UserMainFrame parentFrame, Utente utente) {
        this.parentFrame = parentFrame;
        this.utenteCorrente = utente;
        this.legaDAO = new LegaDAO();
        this.squadraDAO = new SquadraFantacalcioDAO();

        initializeComponents();
        setupLayout();
        refreshData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createTable();
        createForm();
        createButtons();
        setupTableDoubleClick();
    }

    private void setupTableDoubleClick() {
        tabellaLeghePartecipate.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabellaLeghePartecipate.getSelectedRow();
                    if (row >= 0) {
                        if (leghePartecipate == null) return;
                        String nomeLega = (String) modelTabella.getValueAt(row, 0);
                        String codiceAccesso = (String) modelTabella.getValueAt(row, 1);
                        Lega lega = leghePartecipate.stream()
                                .filter(l -> l.getNome().equals(nomeLega) && l.getCodiceAccesso().equals(codiceAccesso))
                                .findFirst()
                                .orElse(null);
                                if (lega != null) {
                                    boolean haGiaUnaSquadraQui = (contaMieSquadreInLega(lega.getIdLega()) > 0);
                                    if (haGiaUnaSquadraQui) {
                                        parentFrame.openLeagueDetail(lega);
                                    } else {
                                        parentFrame.openCreateTeam(lega);
                                    }
                                }                    
                            }
                }
            }
        });
    }

    private void createTable() {
        String[] colonne = {"Nome Lega", "Codice Accesso", "Partecipanti", "Ruolo", "Tue Squadre"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabellaLeghePartecipate = new JTable(modelTabella);
        tabellaLeghePartecipate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaLeghePartecipate.setRowHeight(30);

        // look&feel-friendly
        tabellaLeghePartecipate.getTableHeader().setReorderingAllowed(false);
        tabellaLeghePartecipate.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        // larghezze
        tabellaLeghePartecipate.getColumnModel().getColumn(0).setPreferredWidth(220);
        tabellaLeghePartecipate.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabellaLeghePartecipate.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabellaLeghePartecipate.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabellaLeghePartecipate.getColumnModel().getColumn(4).setPreferredWidth(110);
    }

    private void createForm() {
        txtCodiceAccesso = new JTextField(15);
        txtCodiceAccesso.setToolTipText("Inserisci il codice di accesso della lega");
        txtCodiceAccesso.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        txtCodiceAccesso.addActionListener(this::joinLega); // invio ↩︎
    }

    private void createButtons() {
        btnJoinLega = new JButton("Unisciti");
        btnRefresh  = new JButton("Aggiorna");

        styleButton(btnJoinLega, new Color(76, 175, 80));
        styleButton(btnRefresh,  new Color(158, 158, 158));

        btnJoinLega.addActionListener(this::joinLega);
        btnRefresh.addActionListener(e -> refreshData());
    }

    private void setupLayout() {
        // Header + form
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel titleLabel = new JLabel("Le tue Leghe");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 150, 243));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Unisciti a una Lega"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Codice:"), gbc);
        gbc.gridx = 1; formPanel.add(txtCodiceAccesso, gbc);
        gbc.gridx = 2; formPanel.add(btnJoinLega, gbc);
        gbc.gridx = 3; formPanel.add(btnRefresh, gbc);

        topPanel.add(formPanel, BorderLayout.SOUTH);

        // Tabella al centro
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Leghe a cui partecipi"));

        JScrollPane scrollPane = new JScrollPane(tabellaLeghePartecipate);
        scrollPane.setPreferredSize(new Dimension(820, 320));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Layout finale (nessun pannello destro)
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                leghePartecipate = trovaLegheRealiUtente();
                squadreUtente = squadraDAO.trovaSquadrePerUtente(utenteCorrente.getIdUtente());
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    updateTable();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(JoinLeaguePanel.this,
                            "Errore nel caricamento dei dati: " + e.getMessage(),
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private List<Lega> trovaLegheRealiUtente() {
        List<Lega> legheReali = new ArrayList<>();

        // leghe dove l'utente è admin
        List<Lega> legheAdmin = legaDAO.trovaLeghePerAdmin(utenteCorrente.getIdUtente());
        legheReali.addAll(legheAdmin);

        // leghe dove l'utente partecipa con le sue squadre
        List<SquadraFantacalcio> squadre = squadraDAO.trovaSquadrePerUtente(utenteCorrente.getIdUtente());
        for (SquadraFantacalcio squadra : squadre) {
            try {
                Optional<Lega> lega = trovaLegaDiSquadra(squadra.getIdSquadraFantacalcio());
                lega.ifPresent(l -> { if (!legheReali.contains(l)) legheReali.add(l); });
            } catch (Exception ignore) {}
        }
        return legheReali;
    }

    private Optional<Lega> trovaLegaDiSquadra(int idSquadraFantacalcio) {
        String sql = """
            SELECT l.*
            FROM LEGA l
            JOIN SQUADRA_FANTACALCIO sf ON sf.ID_Lega = l.ID_Lega
            WHERE sf.ID_Squadra_Fantacalcio = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSquadraFantacalcio);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // usa il costruttore completo conforme al tuo model Lega
                    return Optional.of(new Lega(
                            rs.getInt("ID_Lega"),
                            rs.getString("Nome"),
                            rs.getString("Codice_accesso"),
                            rs.getInt("ID_Utente"),
                            rs.getString("Stato"),
                            (Integer) rs.getObject("ID_Campionato")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca lega di squadra: " + e.getMessage());
        }
        return Optional.empty();
    }

    private void updateTable() {
        modelTabella.setRowCount(0);
        if (leghePartecipate == null) return;

        List<Lega> legheAdmin = legaDAO.trovaLeghePerAdmin(utenteCorrente.getIdUtente());

        for (Lega lega : leghePartecipate) {
            int partecipanti = legaDAO.contaPartecipanti(lega.getIdLega());
            int mieSquadreInQuestaLega = contaMieSquadreInLega(lega.getIdLega());

            boolean isAdmin = legheAdmin.stream().anyMatch(l -> l.getIdLega() == lega.getIdLega());
            String ruolo = isAdmin ? "👑 Admin" : "👤 Partecipante";

            Object[] riga = {
                    lega.getNome(),
                    lega.getCodiceAccesso(),
                    partecipanti,
                    ruolo,
                    mieSquadreInQuestaLega
            };
            modelTabella.addRow(riga);
        }
    }

    /** Conta SOLO le squadre dell'utente che appartengono a quella lega (usando ID_Lega nella SQUADRA_FANTACALCIO) */
    private int contaMieSquadreInLega(int idLega) {
        if (squadreUtente == null) return 0;
        int count = 0;
        for (SquadraFantacalcio s : squadreUtente) {
            if (s.getIdLega() == idLega) count++;
        }
        return count;
    }

    private void joinLega(ActionEvent e) {
        String codiceAccesso = txtCodiceAccesso.getText().trim().toUpperCase();
        if (codiceAccesso.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci il codice di accesso della lega!",
                    "Attenzione", JOptionPane.WARNING_MESSAGE);
            txtCodiceAccesso.requestFocus();
            return;
        }

        SwingWorker<Optional<Lega>, Void> worker = new SwingWorker<>() {
            @Override protected Optional<Lega> doInBackground() throws Exception {
                return legaDAO.trovaLegaPerCodice(codiceAccesso);
            }
            @Override protected void done() {
                try {
                    Optional<Lega> legaOpt = get();
                    if (legaOpt.isEmpty()) {
                        JOptionPane.showMessageDialog(JoinLeaguePanel.this,
                                "Codice di accesso non valido.",
                                "Lega non trovata",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Lega lega = legaOpt.get();

                    // Se partecipa già -> apri e basta
                    boolean giaPartecipa = leghePartecipate.stream().anyMatch(l -> l.getIdLega() == lega.getIdLega());
                    if (giaPartecipa) {
                        parentFrame.openLeagueDetail(lega);
                        return;
                    }

                    String stato = (lega.getStato() != null) ? lega.getStato().toUpperCase() : "";
                    if (!"CREATA".equals(stato)) {
                        String msg = "Non puoi unirti tramite codice a una lega nello stato '" + stato + "'.";
                        if ("IN_CORSO".equals(stato)) {
                            msg += "\nChiedi all'amministratore di aggiungerti, oppure attendi una nuova lega.";
                        } else if ("TERMINATA".equals(stato)) {
                            msg += "\nLa lega è conclusa.";
                        }
                        JOptionPane.showMessageDialog(JoinLeaguePanel.this, msg, "Join non consentito", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int conferma = JOptionPane.showConfirmDialog(JoinLeaguePanel.this,
                            "Vuoi unirti alla lega '" + lega.getNome() + "'?\n\n" +
                            "Verrai reindirizzato alla gestione della lega.",
                            "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                    if (conferma == JOptionPane.YES_OPTION) {
                        txtCodiceAccesso.setText("");
                        refreshData();

                        boolean haGiaUnaSquadraQui = (contaMieSquadreInLega(lega.getIdLega()) > 0);
                        if (haGiaUnaSquadraQui) {
                            parentFrame.openLeagueDetail(lega);
                        } else {
                            parentFrame.openCreateTeam(lega);
                        }
                    }

                } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(JoinLeaguePanel.this,
                            "Errore durante l'operazione: " + ex.getMessage(),
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Getter usato dal frame per aggiornare il titolo
    public int getNumLeghe() {
        return leghePartecipate != null ? leghePartecipate.size() : 0;
    }
}
