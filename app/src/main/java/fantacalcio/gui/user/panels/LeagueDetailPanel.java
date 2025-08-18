package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.ScontroLegaDAO;
import fantacalcio.dao.SquadraFantacalcioDAO;
import fantacalcio.gui.user.UserMainFrame;
import fantacalcio.gui.user.dialog.FormationManagementDialog;
import fantacalcio.model.Lega;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;
import fantacalcio.util.DatabaseConnection;

public class LeagueDetailPanel extends JPanel {
    
    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final Lega lega;
    private final SquadraFantacalcioDAO squadraDAO;
    private final ScontroLegaDAO scontroDAO;
    private Timer countdownTimer;
    private JLabel lblCountdown;
    private JButton btnAvviaLega;
    
    private SquadraFantacalcio squadraUtente; // LA squadra dell'utente per questa lega
    private JPanel contentPanel;
    
    public LeagueDetailPanel(UserMainFrame parentFrame, Utente utente, Lega lega) {
        this.parentFrame = parentFrame;
        this.utenteCorrente = utente;
        this.lega = lega;
        this.squadraDAO = new SquadraFantacalcioDAO();
        this.scontroDAO = new ScontroLegaDAO();

        initializeGUI();
        loadUserTeamForLeague();
    }
    
    private void initializeGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
        
        // Mostra messaggio di caricamento iniziale
        JLabel loadingLabel = new JLabel("Caricamento...", JLabel.CENTER);
        loadingLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
        loadingLabel.setForeground(new Color(100, 100, 100));
        contentPanel.add(loadingLabel, BorderLayout.CENTER);
    }
    
    private void loadUserTeamForLeague() {
        SwingWorker<Optional<SquadraFantacalcio>, Void> worker = new SwingWorker<Optional<SquadraFantacalcio>, Void>() {
            @Override
            protected Optional<SquadraFantacalcio> doInBackground() throws Exception {
                return squadraDAO.trovaSquadraUtentePerLega(utenteCorrente.getIdUtente(), lega.getIdLega());
            }
            
            @Override
            protected void done() {
                try {
                    Optional<SquadraFantacalcio> squadraOpt = get();
                    
                    if (squadraOpt.isPresent()) {
                        squadraUtente = squadraOpt.get();
                        showTeamManagement(); // Mostra gestione squadra esistente
                    } else {
                        showTeamCreation(); // Mostra form creazione squadra
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(LeagueDetailPanel.this,
                        "Errore caricamento squadra: " + e.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void showTeamCreation() {
        contentPanel.removeAll();
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("⚽ Crea la tua squadra per questa lega");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 150, 243));
        
        JLabel infoLabel = new JLabel("Non hai ancora una squadra per la lega \"" + lega.getNome() + "\"");
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        infoLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(infoLabel, BorderLayout.SOUTH);
        
        // PASSA idLega al costruttore
        CreateTeamPanel createPanel = new CreateTeamPanel(parentFrame, utenteCorrente, lega.getIdLega()) {
            @Override
            protected void onTeamCreatedSuccess(SquadraFantacalcio nuovaSquadra) {
                LeagueDetailPanel.this.onTeamCreated(nuovaSquadra);
            }
        };
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(createPanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    
    private void showTeamManagement() {
        contentPanel.removeAll();
        
        // Header con info squadra dell'utente
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("🏆 " + squadraUtente.getNomeSquadra());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 150, 243));
        
        JLabel statsLabel = new JLabel(squadraUtente.getStatistiche());
        statsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        statsLabel.setForeground(squadraUtente.isCompletata() ? new Color(76, 175, 80) : new Color(255, 152, 0));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statsLabel, BorderLayout.SOUTH);
        
        // NUOVO: Panel con tutte le squadre della lega
        JPanel legaSquadrePanel = createLegaSquadrePanel();
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(legaSquadrePanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createLegaControlsPanel() {
        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.setBorder(BorderFactory.createTitledBorder("🏆 Controlli Lega"));
        
        // Info lega
        JPanel infoPanel = new JPanel(new FlowLayout());
        JLabel lblStatoLega = new JLabel("Stato: " + scontroDAO.getStatoLega(lega.getIdLega()));
        lblCountdown = new JLabel("⏰ In attesa...");
        
        infoPanel.add(lblStatoLega);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(lblCountdown);
        
        // Pulsanti admin (solo se è admin)
        JPanel adminPanel = createAdminPanel();
        
        controlsPanel.add(infoPanel, BorderLayout.NORTH);
        if (adminPanel != null) {
            controlsPanel.add(adminPanel, BorderLayout.SOUTH);
        }
        
        return controlsPanel;
    }

    private JPanel createAdminPanel() {
        // Verifica se l'utente è admin di questa lega
        // TODO: Implementare controllo admin
        
        JPanel adminPanel = new JPanel(new FlowLayout());
        adminPanel.setBorder(BorderFactory.createTitledBorder("👑 Controlli Admin"));
        
        btnAvviaLega = new JButton("🚀 Avvia Lega");
        btnAvviaLega.setBackground(new Color(76, 175, 80));
        btnAvviaLega.setForeground(Color.WHITE);
        btnAvviaLega.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnAvviaLega.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnAvviaLega.setFocusPainted(false);
        btnAvviaLega.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnAvviaLega.addActionListener(e -> avviaLega());
        
        adminPanel.add(btnAvviaLega);
        
        return adminPanel;
    }

    private void startCountdownIfNeeded() {
        String statoLega = scontroDAO.getStatoLega(lega.getIdLega());
        
        if ("IN_CORSO".equals(statoLega)) {
            // Avvia timer countdown
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            
            countdownTimer = new Timer(1000, e -> updateCountdown());
            countdownTimer.start();
            
            System.out.println("Timer countdown avviato per lega: " + lega.getNome());
        }
    }

    private void updateCountdown() {
        // TODO: Implementare logica countdown basata su prossima partita
        // Per ora mostra un countdown fisso
        lblCountdown.setText("⏰ Prossima partita: 45 secondi");
    }

    private void avviaLega() {
        // Verifica prerequisiti
        if (!scontroDAO.verificaAvvioLega(lega.getIdLega())) {
            JOptionPane.showMessageDialog(this,
                "La lega non può essere avviata!\n" +
                "Requisiti: 2-12 squadre complete, numero pari.",
                "Impossibile Avviare",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int conferma = JOptionPane.showConfirmDialog(this,
            "Sei sicuro di voler avviare la lega?\n" +
            "Una volta avviata, nessuno potrà più unirsi!",
            "Conferma Avvio Lega",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            // TODO: Genera calendario e avvia lega
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // Genera calendario per tutte le squadre complete
                    // TODO: Implementare generazione calendario
                    return scontroDAO.avviaLega(lega.getIdLega());
                }
                
                @Override
                protected void done() {
                    try {
                        boolean successo = get();
                        if (successo) {
                            JOptionPane.showMessageDialog(LeagueDetailPanel.this,
                                "Lega avviata con successo!\n" +
                                "Prima partita inizia tra 1 minuto!",
                                "Lega Avviata",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            btnAvviaLega.setEnabled(false);
                            startCountdownIfNeeded();
                        } else {
                            JOptionPane.showMessageDialog(LeagueDetailPanel.this,
                                "Errore durante l'avvio della lega.",
                                "Errore",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(LeagueDetailPanel.this,
                            "Errore: " + ex.getMessage(),
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }
    
    // Callback quando squadra viene creata
    public void onTeamCreated(SquadraFantacalcio nuovaSquadra) {
        this.squadraUtente = nuovaSquadra;
        
        // Il collegamento alla lega è già fatto automaticamente dal DAO
        showTeamManagement();
        parentFrame.updateTitle();
        
        JOptionPane.showMessageDialog(this,
            "Squadra '" + nuovaSquadra.getNomeSquadra() + "' creata e collegata alla lega!",
            "Successo",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createLegaSquadrePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Info lega e controlli
        JPanel topPanel = createLegaInfoPanel();
        
        // Tabella squadre della lega
        JPanel squadrePanel = createSquadreLegaTable();
        
        // Pannello formazione
        JPanel formazionePanel = createFormazionePanel();
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(squadrePanel, BorderLayout.CENTER);
        mainPanel.add(formazionePanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }

    private JPanel createLegaInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("📊 Info Lega"));
        
        // Info stato lega
        String statoLega = scontroDAO.getStatoLega(lega.getIdLega());
        int giornataCorrente = scontroDAO.getGiornataCorrente(lega.getIdLega());
        
        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftInfo.add(new JLabel("Stato: " + statoLega));
        leftInfo.add(Box.createHorizontalStrut(20));
        leftInfo.add(new JLabel("Giornata: " + giornataCorrente));
        
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblCountdown = new JLabel("⏰ In attesa...");
        rightInfo.add(lblCountdown);
        
        // Pulsante risultati (se lega avviata)
        if ("IN_CORSO".equals(statoLega) || "TERMINATA".equals(statoLega)) {
            JButton btnRisultati = new JButton("📊 Vedi Risultati");
            styleButton(btnRisultati, new Color(33, 150, 243));
            btnRisultati.addActionListener(e -> mostraRisultati());
            rightInfo.add(btnRisultati);
        }
        
        infoPanel.add(leftInfo, BorderLayout.WEST);
        infoPanel.add(rightInfo, BorderLayout.EAST);
        
        return infoPanel;
    }

    private JPanel createSquadreLegaTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("🏆 Squadre della Lega"));
        
        // Crea tabella per le squadre della lega
        String[] colonne = {"Nome Squadra", "Giocatori", "Budget Speso", "Stato", "Proprietario"};
        DefaultTableModel model = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tabella = new JTable(model);
        tabella.setRowHeight(30);
        tabella.getTableHeader().setBackground(new Color(63, 81, 181));
        tabella.getTableHeader().setForeground(Color.WHITE);
        
        // Carica squadre della lega
        caricaSquadreLega(model);
        
        JScrollPane scrollPane = new JScrollPane(tabella);
        scrollPane.setPreferredSize(new Dimension(800, 200));
        
        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnAggiorna = new JButton("🔄 Aggiorna");
        styleButton(btnAggiorna, new Color(158, 158, 158));
        btnAggiorna.addActionListener(e -> caricaSquadreLega(model));
        buttonPanel.add(btnAggiorna);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return tablePanel;
    }

    private JPanel createFormazionePanel() {
        JPanel formazionePanel = new JPanel(new BorderLayout());
        formazionePanel.setBorder(BorderFactory.createTitledBorder("⚽ Gestione Formazione"));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("La tua formazione per la prossima giornata:"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGestisciFormazione = new JButton("⚙️ Gestisci Formazione");
        styleButton(btnGestisciFormazione, new Color(76, 175, 80));
        btnGestisciFormazione.addActionListener(e -> apriGestioneFormazione());
        buttonPanel.add(btnGestisciFormazione);
        
        formazionePanel.add(infoPanel, BorderLayout.WEST);
        formazionePanel.add(buttonPanel, BorderLayout.EAST);
        
        return formazionePanel;
    }

    private void caricaSquadreLega(DefaultTableModel model) {
        model.setRowCount(0);
        
        // TODO: Implementare query per ottenere tutte le squadre della lega
        SwingWorker<List<SquadraLegaInfo>, Void> worker = new SwingWorker<List<SquadraLegaInfo>, Void>() {
            @Override
            protected List<SquadraLegaInfo> doInBackground() throws Exception {
                return getSquadreDaLega(lega.getIdLega());
            }
            
            @Override
            protected void done() {
                try {
                    List<SquadraLegaInfo> squadre = get();
                    for (SquadraLegaInfo squadra : squadre) {
                        Object[] riga = {
                            squadra.nomeSquadra,
                            squadra.numeroGiocatori + "/25",
                            squadra.budgetSpeso + " €",
                            squadra.completata ? "✅ Completa" : "⚠️ In costruzione",
                            squadra.proprietario
                        };
                        model.addRow(riga);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LeagueDetailPanel.this,
                        "Errore caricamento squadre: " + e.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void apriGestioneFormazione() {
        FormationManagementDialog dialog = new FormationManagementDialog(
            SwingUtilities.getWindowAncestor(this),
            squadraUtente,
            utenteCorrente
        );
        dialog.setVisible(true);
    }

    private void mostraRisultati() {
        RisultatiLegaDialog dialog = new RisultatiLegaDialog(
            SwingUtilities.getWindowAncestor(this),
            lega,
            scontroDAO.getGiornataCorrente(lega.getIdLega())
        );
        dialog.setVisible(true);
    }

    // Classe helper per le info squadre
    private static class SquadraLegaInfo {
        String nomeSquadra;
        int numeroGiocatori;
        int budgetSpeso;
        boolean completata;
        String proprietario;
        
        SquadraLegaInfo(String nome, int giocatori, int budget, boolean completa, String owner) {
            this.nomeSquadra = nome;
            this.numeroGiocatori = giocatori;
            this.budgetSpeso = budget;
            this.completata = completa;
            this.proprietario = owner;
        }
    }

    private List<SquadraLegaInfo> getSquadreDaLega(int idLega) {
        List<SquadraLegaInfo> squadre = new ArrayList<>();
        String sql = """
            SELECT s.Nome, s.Budget_totale - s.Budget_rimanente as Budget_Speso, 
                s.Completata, u.Nickname,
                (SELECT COUNT(*) FROM COMPOSIZIONE c WHERE c.ID_Squadra = s.ID_Squadra) as Num_Giocatori
            FROM SQUADRA_FANTACALCIO s
            JOIN PARTECIPA p ON s.ID_Squadra = p.ID_Squadra
            JOIN CREA cr ON s.ID_Squadra = cr.ID_Squadra
            JOIN UTENTE u ON cr.ID_Utente = u.ID_Utente
            WHERE p.ID_Lega = ?
            ORDER BY s.Completata DESC, s.Nome
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    squadre.add(new SquadraLegaInfo(
                        rs.getString("Nome"),
                        rs.getInt("Num_Giocatori"),
                        rs.getInt("Budget_Speso"),
                        rs.getBoolean("Completata"),
                        rs.getString("Nickname")
                    ));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore caricamento squadre lega: " + e.getMessage());
        }
        
        return squadre;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}