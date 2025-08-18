package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
 * Panel per permettere agli utenti di joinare le leghe
 */
public class JoinLeaguePanel extends JPanel {
    
    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final LegaDAO legaDAO;
    private final SquadraFantacalcioDAO squadraDAO;
    
    // Componenti GUI
    private JTable tabellaLeghePartecipate;
    private DefaultTableModel modelTabella;
    private JTextField txtCodiceAccesso;
    private JButton btnJoinLega, btnRefresh, btnCreaSquadra, btnGestisciSquadra;
    private JButton btnCreaLega;
    // Statistiche
    private JLabel lblNumLeghe, lblSquadreCreate, lblSquadreComplete;
    
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
        createStats();
        setupTableDoubleClick();
    }

    private void setupTableDoubleClick() {
            tabellaLeghePartecipate.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) { // Double click
                        int row = tabellaLeghePartecipate.getSelectedRow();
                        if (row >= 0) {
                            String nomeLega = (String) modelTabella.getValueAt(row, 0);
                            String codiceAccesso = (String) modelTabella.getValueAt(row, 1);
                            
                            // Trova la lega corrispondente
                            Lega lega = leghePartecipate.stream()
                                .filter(l -> l.getNome().equals(nomeLega) && l.getCodiceAccesso().equals(codiceAccesso))
                                .findFirst()
                                .orElse(null);
                            
                            if (lega != null) {
                                parentFrame.openLeagueDetail(lega);
                            }
                        }
                    }
                }
            });
        }
    
    private void createTable() {
        String[] colonne = {"Nome Lega", "Codice Accesso", "Partecipanti", "Ruolo", "Tue Squadre"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaLeghePartecipate = new JTable(modelTabella);
        tabellaLeghePartecipate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaLeghePartecipate.setRowHeight(30);
        
        // Stile tabella
        tabellaLeghePartecipate.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaLeghePartecipate.getTableHeader().setForeground(Color.WHITE);
        tabellaLeghePartecipate.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Larghezza colonne
        tabellaLeghePartecipate.getColumnModel().getColumn(0).setPreferredWidth(200); // Nome
        tabellaLeghePartecipate.getColumnModel().getColumn(1).setPreferredWidth(120); // Codice
        tabellaLeghePartecipate.getColumnModel().getColumn(2).setPreferredWidth(100); // Partecipanti
        tabellaLeghePartecipate.getColumnModel().getColumn(3).setPreferredWidth(100); // Tue Squadre
        tabellaLeghePartecipate.getColumnModel().getColumn(4).setPreferredWidth(120); // Status
    }
    
    private void createForm() {
        txtCodiceAccesso = new JTextField(15);
        txtCodiceAccesso.setToolTipText("Inserisci il codice di accesso della lega");
        txtCodiceAccesso.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        
        // Listener per Enter
        txtCodiceAccesso.addActionListener(e -> joinLega(null));
    }
    
    private void createButtons() {
        btnJoinLega = new JButton("🔗 Unisciti alla Lega");
        btnRefresh = new JButton("🔄 Aggiorna");
        btnCreaLega = new JButton("🏆 Crea Nuova Lega"); // NUOVO
        
        // Stile pulsanti
        styleButton(btnJoinLega, new Color(76, 175, 80));
        styleButton(btnRefresh, new Color(158, 158, 158));
        styleButton(btnCreaLega, new Color(156, 39, 176)); // NUOVO
        
        // Tooltip informativi
        btnJoinLega.setToolTipText("Unisciti a una lega usando il codice di accesso");
        btnCreaLega.setToolTipText("Crea una nuova lega e diventa admin"); // NUOVO
        
        // Event listeners
        btnJoinLega.addActionListener(this::joinLega);
        btnRefresh.addActionListener(e -> refreshData());
        btnCreaLega.addActionListener(this::creaLega); // NUOVO
    }

    private void creaLega(ActionEvent e) {
        String nomeLega = JOptionPane.showInputDialog(this,
            "Inserisci il nome della nuova lega:",
            "Crea Nuova Lega",
            JOptionPane.QUESTION_MESSAGE);
        
        if (nomeLega != null && !nomeLega.trim().isEmpty()) {
            final String nomeLegaFinal = nomeLega.trim();
            
            // Verifica se esiste già una lega con questo nome per questo utente
            if (legaDAO.nomeLegaEsiste(nomeLegaFinal, utenteCorrente.getIdUtente())) {
                JOptionPane.showMessageDialog(this,
                    "Hai già una lega con questo nome!",
                    "Nome Duplicato",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            final Lega nuovaLega = new Lega(nomeLegaFinal, utenteCorrente.getIdUtente());
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return legaDAO.creaLega(nuovaLega);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean successo = get();
                        
                        if (successo) {
                            JOptionPane.showMessageDialog(JoinLeaguePanel.this,
                                "Lega '" + nomeLegaFinal + "' creata con successo!\n" +
                                "Codice di accesso: " + nuovaLega.getCodiceAccesso() + "\n" +
                                "Condividi questo codice con i tuoi amici!",
                                "Lega Creata",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            refreshData();
                            
                            // Chiedi se vuole aprire subito la lega
                            int apri = JOptionPane.showConfirmDialog(JoinLeaguePanel.this,
                                "Vuoi aprire subito la gestione della lega?",
                                "Apri Lega",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                            
                            if (apri == JOptionPane.YES_OPTION) {
                                parentFrame.openLeagueDetail(nuovaLega);
                            }
                            
                        } else {
                            JOptionPane.showMessageDialog(JoinLeaguePanel.this,
                                "Errore durante la creazione della lega.",
                                "Errore",
                                JOptionPane.ERROR_MESSAGE);
                        }
                        
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(JoinLeaguePanel.this,
                            "Errore: " + ex.getMessage(),
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void createStats() {
        lblNumLeghe = new JLabel("0");
        lblSquadreCreate = new JLabel("0");
        lblSquadreComplete = new JLabel("0");
        
        // Stile per le statistiche
        Font statsFont = new Font(Font.SANS_SERIF, Font.BOLD, 24);
        lblNumLeghe.setFont(statsFont);
        lblSquadreCreate.setFont(statsFont);
        lblSquadreComplete.setFont(statsFont);
    }
    
    private void setupLayout() {
        // Pannello superiore - header e form
        JPanel topPanel = createTopPanel();
        
        // Pannello centrale - tabella
        JPanel centerPanel = createCenterPanel();
        
        // Pannello destro - statistiche e azioni
        JPanel rightPanel = createRightPanel();
        
        // Layout finale
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("🏆 Le tue Leghe Fantacalcio");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 150, 243));
        
        JLabel subtitleLabel = new JLabel("Unisciti a una lega per iniziare a giocare");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Form per join lega
        JPanel formPanel = createFormPanel();
        
        topPanel.add(headerPanel, BorderLayout.CENTER);
        topPanel.add(formPanel, BorderLayout.SOUTH);
        
        return topPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Gestione Leghe"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Prima riga - Join lega esistente
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Codice Accesso:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(txtCodiceAccesso, gbc);
        
        gbc.gridx = 2;
        formPanel.add(btnJoinLega, gbc);
        
        gbc.gridx = 3;
        formPanel.add(btnRefresh, gbc);
        
        // Seconda riga - Crea nuova lega
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 4;
        JPanel createPanel = new JPanel(new FlowLayout());
        createPanel.add(btnCreaLega);
        formPanel.add(createPanel, gbc);
        gbc.gridwidth = 1; // Reset
        
        return formPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Leghe a cui Partecipi"));
        
        JScrollPane scrollPane = new JScrollPane(tabellaLeghePartecipate);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        return centerPanel;
    }
    
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Statistiche e Azioni"));
        rightPanel.setPreferredSize(new Dimension(250, 0));
        
        // Statistiche
        JPanel statsPanel = createStatsPanel();
        
        // Azioni rapide
        JPanel actionsPanel = createActionsPanel();
        
        rightPanel.add(statsPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(actionsPanel);
        rightPanel.add(Box.createVerticalGlue());
        
        return rightPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        
        JPanel leghePanel = createStatCard("🏆 Leghe:", lblNumLeghe, new Color(33, 150, 243));
        JPanel squadrePanel = createStatCard("⚽ Squadre:", lblSquadreCreate, new Color(76, 175, 80));
        JPanel completePanel = createStatCard("✅ Complete:", lblSquadreComplete, new Color(255, 152, 0));
        
        statsPanel.add(leghePanel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(squadrePanel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(completePanel);
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String titolo, JLabel valore, Color colore) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(colore, 1));
        panel.setBackground(Color.WHITE);
        
        JLabel titoloLabel = new JLabel(titolo);
        titoloLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titoloLabel.setForeground(colore);
        titoloLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        
        valore.setForeground(colore);
        valore.setHorizontalAlignment(JLabel.CENTER);
        valore.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        
        panel.add(titoloLabel, BorderLayout.NORTH);
        panel.add(valore, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Azioni Rapide"));
        
        JLabel infoLabel = new JLabel("<html><center>Fai doppio click su una lega<br>per gestire la tua squadra</center></html>");
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        
        actionsPanel.add(infoLabel);
        
        return actionsPanel;
    }
    
    public void refreshData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // CAMBIARE: Carica solo le leghe a cui l'utente partecipa realmente
                leghePartecipate = trovaLegheRealiUtente();
                
                // Carica squadre utente
                squadreUtente = squadraDAO.trovaSquadrePerUtente(utenteCorrente.getIdUtente());
                
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Controlla eccezioni
                    updateTable();
                    updateStats();
                } catch (Exception e) {
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
        
        // 1. Trova leghe dove l'utente è admin
        List<Lega> legheAdmin = legaDAO.trovaLeghePerAdmin(utenteCorrente.getIdUtente());
        legheReali.addAll(legheAdmin);
        
        // 2. Trova leghe dove l'utente partecipa con squadre
        List<SquadraFantacalcio> squadre = squadraDAO.trovaSquadrePerUtente(utenteCorrente.getIdUtente());
        
        for (SquadraFantacalcio squadra : squadre) {
            try {
                Optional<Lega> lega = trovaLegaDiSquadra(squadra.getIdSquadraFantacalcio());
                if (lega.isPresent() && !legheReali.contains(lega.get())) {
                    legheReali.add(lega.get());
                }
            } catch (Exception e) {
                System.err.println("Errore ricerca lega per squadra " + squadra.getIdSquadraFantacalcio());
            }
        }
        
        return legheReali;
    }

    private Optional<Lega> trovaLegaDiSquadra(int idSquadra) {
        String sql = """
            SELECT l.* FROM LEGA l 
            JOIN PARTECIPA p ON l.ID_Lega = p.ID_Lega 
            WHERE p.ID_Squadra = ?
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idSquadra);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Lega(
                        rs.getInt("ID_Lega"),
                        rs.getString("Nome"),
                        rs.getString("Codice_accesso"),
                        0 // ID admin non necessario qui
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
            int squadreInLega = contaSquadreInLega(lega.getIdLega());
            
            // Determina ruolo
            boolean isAdmin = legheAdmin.stream().anyMatch(l -> l.getIdLega() == lega.getIdLega());
            String ruolo = isAdmin ? "👑 Admin" : "👤 Partecipante";
            
            Object[] riga = {
                lega.getNome(),
                lega.getCodiceAccesso(),
                partecipanti,
                ruolo,
                squadreInLega
            };
            modelTabella.addRow(riga);
        }
    }
    
    private int contaSquadreInLega(int idLega) {
        if (squadreUtente == null) return 0;
        
        int count = 0;
        for (SquadraFantacalcio squadra : squadreUtente) {
            // Qui dovresti verificare se la squadra partecipa alla lega
            // Per ora assumiamo che tutte le squadre dell'utente partecipino a tutte le leghe
            count++;
        }
        return count;
    }
    
    private void updateStats() {
        if (leghePartecipate == null || squadreUtente == null) return;
        
        int squadreComplete = (int) squadreUtente.stream().filter(SquadraFantacalcio::isCompletata).count();
        
        lblNumLeghe.setText(String.valueOf(leghePartecipate.size()));
        lblSquadreCreate.setText(String.valueOf(squadreUtente.size()));
        lblSquadreComplete.setText(String.valueOf(squadreComplete));
    }
    
    private void joinLega(ActionEvent e) {
        String codiceAccesso = txtCodiceAccesso.getText().trim().toUpperCase();
        
        if (codiceAccesso.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Inserisci il codice di accesso della lega!",
                "Attenzione",
                JOptionPane.WARNING_MESSAGE);
            txtCodiceAccesso.requestFocus();
            return;
        }
        
        SwingWorker<Optional<Lega>, Void> worker = new SwingWorker<Optional<Lega>, Void>() {
            @Override
            protected Optional<Lega> doInBackground() throws Exception {
                return legaDAO.trovaLegaPerCodice(codiceAccesso);
            }
            
            @Override
            protected void done() {
                try {
                    Optional<Lega> legaOpt = get();
                    
                    if (legaOpt.isEmpty()) {
                        JOptionPane.showMessageDialog(JoinLeaguePanel.this,
                            "Codice di accesso non valido!\nVerifica di aver inserito il codice corretto.",
                            "Lega non trovata",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    Lega lega = legaOpt.get();
                    
                    // Verifica se l'utente partecipa già a questa lega
                    boolean giaPartecipa = leghePartecipate.stream()
                        .anyMatch(l -> l.getIdLega() == lega.getIdLega());
                    
                    if (giaPartecipa) {
                        // Se già partecipa, apri direttamente la lega
                        JOptionPane.showMessageDialog(JoinLeaguePanel.this,
                            "Partecipi già alla lega '" + lega.getNome() + "'!\nAprendo la gestione della lega...",
                            "Lega già joined",
                            JOptionPane.INFORMATION_MESSAGE);
                        parentFrame.openLeagueDetail(lega);
                        return;
                    }
                    
                    // Chiedi conferma per entrare
                    int conferma = JOptionPane.showConfirmDialog(JoinLeaguePanel.this,
                        "Vuoi unirti alla lega '" + lega.getNome() + "'?\n\n" +
                        "Verrai reindirizzato alla creazione della tua squadra per questa lega.",
                        "Conferma Join Lega",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (conferma == JOptionPane.YES_OPTION) {
                        txtCodiceAccesso.setText("");
                        refreshData();
                        
                        // Apri direttamente il dettaglio della lega
                        parentFrame.openLeagueDetail(lega);
                    }
                    
                } catch (Exception ex) {
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
    
    // Getters per statistiche
    public int getNumLeghe() {
        return leghePartecipate != null ? leghePartecipate.size() : 0;
    }
    
    public int getSquadreCreate() {
        return squadreUtente != null ? squadreUtente.size() : 0;
    }
    
    public int getSquadreComplete() {
        return squadreUtente != null ? 
            (int) squadreUtente.stream().filter(SquadraFantacalcio::isCompletata).count() : 0;
    }
}