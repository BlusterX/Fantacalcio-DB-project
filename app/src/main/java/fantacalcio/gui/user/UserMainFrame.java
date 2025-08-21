package fantacalcio.gui.user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import fantacalcio.gui.user.dialog.UserProfileDialog;
import fantacalcio.gui.user.panels.CreateTeamPanel;
import fantacalcio.gui.user.panels.JoinLeaguePanel;
import fantacalcio.gui.user.panels.LeagueDetailPanel;
import fantacalcio.gui.user.panels.ManageTeamPanel;
import fantacalcio.gui.user.panels.UserDashboardPanel;
import fantacalcio.model.Lega;
import fantacalcio.model.Utente;

public class UserMainFrame extends JFrame {

    private final Utente utenteCorrente;

    // Componenti GUI
    private JTabbedPane tabbedPane;
    private UserDashboardPanel dashboardPanel;
    private JoinLeaguePanel joinLeaguePanel;
    private CreateTeamPanel createTeamPanel;
    private ManageTeamPanel manageTeamPanel;
    private JPanel mainContentPanel; // Per gestire il contenuto principale
    private boolean inLeagueDetail = false;
    // Menu
    private JMenuBar menuBar;
    public UserMainFrame(Utente utente) {
        this.utenteCorrente = utente;
        initializeGUI();
        setLocationRelativeTo(null);
    }
    private void initializeGUI() {
        setTitle("FantaCalcio - " + utenteCorrente.getNickname());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1200, 720));
        createMenuBar();
        createTopPanel();
        createTabbedPane();
        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    /**
     * Barra menu
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        // Menu Account
        JMenu menuAccount = new JMenu("Account");
        JMenuItem itemProfilo = new JMenuItem("Il mio profilo");
        itemProfilo.addActionListener(this::mostraProfilo);
        JMenuItem itemLogout = new JMenuItem("Logout");
        itemLogout.addActionListener(this::logout);
        menuAccount.add(itemProfilo);
        menuAccount.addSeparator();
        menuAccount.add(itemLogout);
        // Menu Admin
        JMenu menuAdmin = new JMenu("Admin leghe");
        JMenuItem itemAdminPanel = new JMenuItem("Gestione leghe create");
        itemAdminPanel.addActionListener(this::apriAdminPanel);
        menuAdmin.add(itemAdminPanel);
        // Menu Aiuto
        JMenu menuAiuto = new JMenu("Aiuto");
        JMenuItem itemInfo = new JMenuItem("Informazioni");
        itemInfo.addActionListener(this::mostraInfo);
        menuAiuto.add(itemInfo);
        menuBar.add(menuAccount);
        menuBar.add(menuAdmin);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuAiuto);
        setJMenuBar(menuBar);
    }
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        // Info utente a sinistra
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userInfoPanel.setOpaque(false);
        JLabel welcomeLabel = new JLabel("Benvenuto, " + utenteCorrente.getNickname());
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(33, 37, 41));
        JLabel userDetailsLabel = new JLabel(" | " + utenteCorrente.getNomeCompleto() + " • " + utenteCorrente.getEmail());
        userDetailsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        userDetailsLabel.setForeground(new Color(90, 98, 105));
        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(userDetailsLabel);
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 219)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnLogout.setBackground(Color.WHITE);
        btnLogout.addActionListener(this::logout);
        topPanel.add(userInfoPanel, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }
    private void createTabbedPane() {
        mainContentPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        // Solo i pannelli necessari
        dashboardPanel = new UserDashboardPanel(this, utenteCorrente);
        joinLeaguePanel = new JoinLeaguePanel(this, utenteCorrente);
        // RIMOSSE: createTeamPanel e manageTeamPanel
        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Le mie Leghe", joinLeaguePanel);
        // Stile sobrio
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        tabbedPane.addChangeListener(e -> {
            if (!inLeagueDetail) {
                int idx = tabbedPane.getSelectedIndex();
                switch (idx) {
                    case 0 -> dashboardPanel.refreshData();
                    case 1 -> joinLeaguePanel.refreshData();
                }
                updateTitle();
            }
        });
        mainContentPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.CENTER);
        // Dati iniziali
        SwingUtilities.invokeLater(() -> {
            dashboardPanel.refreshData();
            updateTitle();
        });
    }

    public void openLeagueDetail(Lega lega) {
        inLeagueDetail = true;
        mainContentPanel.removeAll();
        LeagueDetailPanel leagueDetailPanel = new LeagueDetailPanel(this, utenteCorrente, lega);
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        headerPanel.setBackground(new Color(245, 247, 250));
        JButton btnBack = new JButton("← Indietro");
        btnBack.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 219)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnBack.setBackground(Color.WHITE);
        btnBack.addActionListener(e -> backToMainView());

        JLabel leagueTitle = new JLabel(lega.getNome() + " (" + lega.getCodiceAccesso() + ")");
        leagueTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        leagueTitle.setForeground(new Color(33, 37, 41));
        headerPanel.add(btnBack, BorderLayout.WEST);
        headerPanel.add(leagueTitle, BorderLayout.CENTER);
        wrapperPanel.add(headerPanel, BorderLayout.NORTH);
        wrapperPanel.add(leagueDetailPanel, BorderLayout.CENTER);
        mainContentPanel.add(wrapperPanel, BorderLayout.CENTER);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        setTitle("FantaCalcio - " + utenteCorrente.getNickname() + " - " + lega.getNome());
    }

    public void backToMainView() {
        inLeagueDetail = false;
        mainContentPanel.removeAll();
        mainContentPanel.add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.setSelectedIndex(1);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        joinLeaguePanel.refreshData();
        updateTitle();
    }
    public void updateTitle() {
        if (inLeagueDetail) return;
        setTitle(String.format("FantaCalcio - %s",
                utenteCorrente.getNickname()));
    }
    // Event handlers menu
    private void mostraProfilo(ActionEvent e) {
        UserProfileDialog dialog = new UserProfileDialog(this, utenteCorrente);
        dialog.setVisible(true);
    }
    private void logout(ActionEvent e) {
        int conferma = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler uscire?",
                "Conferma logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (conferma == JOptionPane.YES_OPTION) {
            fantacalcio.gui.user.UserLoginFrame loginFrame = new fantacalcio.gui.user.UserLoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }
    private void apriAdminPanel(ActionEvent e) {
        try {
            fantacalcio.gui.admin.dialogs.AdminLeagueDialog adminDialog =
                    new fantacalcio.gui.admin.dialogs.AdminLeagueDialog(this, utenteCorrente);
            adminDialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Errore apertura admin panel: " + ex.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void mostraInfo(ActionEvent e) {
        String info = """
            🏆 FANTACALCIO
            Versione 1.0
            
            Sviluppato con Java Swing
            Database: MySQL
            
            Un progetto per creare e gestire
            le tue squadre fantacalcio ideali!
            
            🎮 CARATTERISTICHE:
            • Sistema di leghe con codici di accesso
            • Gestione squadre e formazioni
            • Interfaccia utente intuitiva
            • Gestione admin per le leghe
            
            Buon divertimento! ⚽
            """;
        JOptionPane.showMessageDialog(this, info, "Informazioni", JOptionPane.INFORMATION_MESSAGE);
    }
    public void notifySquadraEliminata() {
        dashboardPanel.refreshData();
        joinLeaguePanel.refreshData();
        updateTitle();
    }
    public void notifyLegaJoined() {
        joinLeaguePanel.refreshData();
        dashboardPanel.refreshData();
        updateTitle();
    }
    // Getters
    public Utente getUtenteCorrente() {
        return utenteCorrente;
    }

    public void switchToLeagues() {
        if (inLeagueDetail) {
            backToMainView();
        } else {
            tabbedPane.setSelectedIndex(1);
        }
    }
}