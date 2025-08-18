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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import fantacalcio.gui.user.dialog.UserProfileDialog;
import fantacalcio.gui.user.panels.CreateTeamPanel;
import fantacalcio.gui.user.panels.JoinLeaguePanel;
import fantacalcio.gui.user.panels.LeagueDetailPanel;
import fantacalcio.gui.user.panels.ManageTeamPanel;
import fantacalcio.gui.user.panels.UserDashboardPanel;
import fantacalcio.model.Lega;
import fantacalcio.model.Utente;

/**
 * Finestra principale per l'interfaccia utente del fantacalcio
 */
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
        setMinimumSize(new Dimension(1400, 800));
        
        createMenuBar();
        createTopPanel();
        createTabbedPane();
        
        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    /**
     * Crea la barra dei menu
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
        
        // Menu Admin - SEMPRE VISIBILE per tutti gli utenti
        JMenu menuAdmin = new JMenu("Admin leghe");
        
        JMenuItem itemAdminPanel = new JMenuItem("Gestione delle leghe create");
        itemAdminPanel.addActionListener(this::apriAdminPanel);
        
        menuAdmin.add(itemAdminPanel);
        
        // Menu Aiuto
        JMenu menuAiuto = new JMenu("Aiuto");
        
        JMenuItem itemRegole = new JMenuItem("Regole del gioco");
        itemRegole.addActionListener(this::mostraRegole);
        
        JMenuItem itemInfo = new JMenuItem("Informazioni");
        itemInfo.addActionListener(this::mostraInfo);
        
        menuAiuto.add(itemRegole);
        menuAiuto.add(itemInfo);
        
        menuBar.add(menuAccount);
        menuBar.add(menuAdmin); // Sempre visibile
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuAiuto);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Crea il pannello superiore con informazioni utente
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(33, 150, 243));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Informazioni utente a sinistra
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setBackground(new Color(33, 150, 243));
        
        JLabel welcomeLabel = new JLabel("🏆 Benvenuto, " + utenteCorrente.getNickname() + "!");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        welcomeLabel.setForeground(Color.WHITE);
        
        JLabel userDetailsLabel = new JLabel(utenteCorrente.getNomeCompleto() + " • " + utenteCorrente.getEmail());
        userDetailsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userDetailsLabel.setForeground(new Color(200, 230, 255));
        
        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(Box.createHorizontalStrut(20));
        userInfoPanel.add(userDetailsLabel);
        
        // Pulsante logout a destra
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(244, 67, 54));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(this::logout);
        
        topPanel.add(userInfoPanel, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Crea le tab con i panel dedicati
     */
    private void createTabbedPane() {
        // Crea il pannello principale che conterrà o le tab o il dettaglio lega
        mainContentPanel = new JPanel(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        
        // Crea SOLO i panel necessari
        dashboardPanel = new UserDashboardPanel(this, utenteCorrente);
        joinLeaguePanel = new JoinLeaguePanel(this, utenteCorrente);
        // RIMUOVERE: createTeamPanel e manageTeamPanel
        
        // Aggiungi SOLO le tab necessarie
        tabbedPane.addTab("🏠 Dashboard", dashboardPanel);
        tabbedPane.addTab("🏆 Le mie Leghe", joinLeaguePanel);
        // RIMUOVERE: le tab di creazione e gestione squadre
        
        // Stile delle tab
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        // Event listener semplificato
        tabbedPane.addChangeListener(e -> {
            if (!inLeagueDetail) {
                int selectedIndex = tabbedPane.getSelectedIndex();
                switch (selectedIndex) {
                    case 0 -> dashboardPanel.refreshData();
                    case 1 -> joinLeaguePanel.refreshData();
                }
                updateTitle();
            }
        });
        
        // Inizialmente mostra le tab
        mainContentPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.CENTER);
        
        // Carica i dati iniziali
        SwingUtilities.invokeLater(() -> {
            dashboardPanel.refreshData();
            updateTitle();
        });
    }

    public void openLeagueDetail(Lega lega) {
        inLeagueDetail = true;
        
        // Rimuovi il contenuto attuale
        mainContentPanel.removeAll();
        
        // Crea il panel per il dettaglio della lega
        LeagueDetailPanel leagueDetailPanel = new LeagueDetailPanel(this, utenteCorrente, lega);
        
        // Crea un panel wrapper con pulsante "Torna alle leghe"
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        
        // Header con pulsante back
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton btnBack = new JButton("← Torna alle Leghe");
        btnBack.setBackground(new Color(158, 158, 158));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnBack.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> backToMainView());
        
        JLabel leagueTitle = new JLabel("🏆 " + lega.getNome() + " (" + lega.getCodiceAccesso() + ")");
        leagueTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        leagueTitle.setForeground(new Color(33, 150, 243));
        
        headerPanel.add(btnBack, BorderLayout.WEST);
        headerPanel.add(leagueTitle, BorderLayout.CENTER);
        
        wrapperPanel.add(headerPanel, BorderLayout.NORTH);
        wrapperPanel.add(leagueDetailPanel, BorderLayout.CENTER);
        
        // Mostra il nuovo contenuto
        mainContentPanel.add(wrapperPanel, BorderLayout.CENTER);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        
        // Aggiorna il titolo
        setTitle("FantaCalcio - " + utenteCorrente.getNickname() + " - " + lega.getNome());
    }

    // NUOVO METODO: Torna alla vista principale con le tab
    public void backToMainView() {
        inLeagueDetail = false;
        
        // Rimuovi il contenuto attuale
        mainContentPanel.removeAll();
        
        // Ripristina le tab
        mainContentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Vai alla tab delle leghe
        tabbedPane.setSelectedIndex(1); // Tab "Le mie Leghe"
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        
        // Refresh dati e titolo
        joinLeaguePanel.refreshData();
        updateTitle();
    }
    
    /**
     * Aggiorna il titolo della finestra
     */
    public void updateTitle() {
        if (inLeagueDetail) {
            // Il titolo viene gestito in openLeagueDetail()
            return;
        }
        
        int numSquadre = dashboardPanel.getNumeroSquadreUtente();
        int squadreComplete = dashboardPanel.getNumeroSquadreComplete();
        int numLeghe = joinLeaguePanel.getNumLeghe();
        
        setTitle(String.format("FantaCalcio - %s (%d leghe, %d squadre, %d complete)", 
                            utenteCorrente.getNickname(), numLeghe, numSquadre, squadreComplete));
    }
    
    /**
     * Event handlers per menu
     */
    private void mostraProfilo(ActionEvent e) {
        UserProfileDialog dialog = new UserProfileDialog(this, utenteCorrente);
        dialog.setVisible(true);
    }
    
    private void logout(ActionEvent e) {
        int conferma = JOptionPane.showConfirmDialog(this,
            "Sei sicuro di voler uscire?",
            "Conferma Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            // Torna alla schermata di login
            fantacalcio.gui.user.UserLoginFrame loginFrame = new fantacalcio.gui.user.UserLoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }
    
    private void apriAdminPanel(ActionEvent e) {
        try {
            // Crea il dialog admin con gestione leghe
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
    
    private void mostraRegole(ActionEvent e) {
        String regole = """
            📋 REGOLE DEL FANTACALCIO
            
            🏆 OBIETTIVO:
            Crea la tua squadra ideale scegliendo i migliori calciatori della Serie A!
            
            🎯 COME INIZIARE:
            1. Unisciti a una lega usando il codice di accesso fornito dall'admin
            2. Crea la tua squadra fantacalcio
            3. Componi la formazione per ogni giornata
            4. Sfida gli altri partecipanti!
            
            👥 COMPOSIZIONE SQUADRA:
            • 3 Portieri
            • 8 Difensori  
            • 8 Centrocampisti
            • 6 Attaccanti
            • Totale: 25 giocatori
            
            💰 BUDGET:
            • Budget iniziale: 1000 crediti
            • Ogni giocatore ha un costo fisso
            • Devi rimanere entro il budget
            
            ⚽ REGOLE:
            • Non puoi acquistare lo stesso giocatore due volte
            • Devi rispettare i limiti per ruolo
            • La squadra è completa solo quando hai tutti i 25 giocatori
            
            🎯 STRATEGIA:
            • Bilancia giocatori costosi e economici
            • Considera le prestazioni reali dei calciatori
            • Una volta completata, la squadra è pronta per competere!
            """;
        
        JTextArea textArea = new JTextArea(regole);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Regole del Gioco", JOptionPane.INFORMATION_MESSAGE);
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
    
    /**
     * Getters per i panel
     */
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