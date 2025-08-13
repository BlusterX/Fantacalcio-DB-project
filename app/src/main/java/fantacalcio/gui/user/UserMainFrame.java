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
import fantacalcio.gui.user.panels.ManageTeamPanel;
import fantacalcio.gui.user.panels.UserDashboardPanel;
import fantacalcio.model.Utente;

/**
 * Finestra principale per l'interfaccia utente del fantacalcio
 */
public class UserMainFrame extends JFrame {
    
    private final Utente utenteCorrente;
    
    // Componenti GUI
    private JTabbedPane tabbedPane;
    private UserDashboardPanel dashboardPanel;
    private CreateTeamPanel createTeamPanel;
    private ManageTeamPanel manageTeamPanel;
    
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
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Apri a schermo intero
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
        
        // Menu Aiuto
        JMenu menuAiuto = new JMenu("Aiuto");
        
        JMenuItem itemRegole = new JMenuItem("Regole del gioco");
        itemRegole.addActionListener(this::mostraRegole);
        
        JMenuItem itemInfo = new JMenuItem("Informazioni");
        itemInfo.addActionListener(this::mostraInfo);
        
        menuAiuto.add(itemRegole);
        menuAiuto.add(itemInfo);
        
        menuBar.add(menuAccount);
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
        tabbedPane = new JTabbedPane();
        
        // Crea i panel specializzati
        dashboardPanel = new UserDashboardPanel(this, utenteCorrente);
        createTeamPanel = new CreateTeamPanel(this, utenteCorrente);
        manageTeamPanel = new ManageTeamPanel(this, utenteCorrente);
        
        // Aggiungi le tab con icone
        tabbedPane.addTab("🏠 Dashboard", dashboardPanel);
        tabbedPane.addTab("⚽ Crea Squadra", createTeamPanel);
        tabbedPane.addTab("📝 Gestisci Squadre", manageTeamPanel);
        
        // Stile delle tab
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        // Event listener per aggiornare i dati quando si cambia tab
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch (selectedIndex) {
                case 0 -> // Dashboard
                    dashboardPanel.refreshData();
                case 1 -> // Crea Squadra
                    createTeamPanel.refreshData();
                case 2 -> // Gestisci Squadre
                    manageTeamPanel.refreshData();
            }
            updateTitle();
        });
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Carica i dati iniziali
        SwingUtilities.invokeLater(() -> {
            dashboardPanel.refreshData();
            updateTitle();
        });
    }
    
    /**
     * Aggiorna il titolo della finestra
     */
    public void updateTitle() {
        int numSquadre = dashboardPanel.getNumeroSquadreUtente();
        int squadreComplete = dashboardPanel.getNumeroSquadreComplete();
        
        setTitle(String.format("FantaCalcio - %s (%d squadre, %d complete)", 
                              utenteCorrente.getNickname(), numSquadre, squadreComplete));
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
    
    private void mostraRegole(ActionEvent e) {
        String regole = """
            📋 REGOLE DEL FANTACALCIO
            
            🏆 OBIETTIVO:
            Crea la tua squadra ideale scegliendo i migliori calciatori della Serie A!
            
            👥 COMPOSIZIONE SQUADRA:
            • 3 Portieri
            • 8 Difensori  
            • 8 Centrocampisti
            • 6 Attaccanti
            • Totale: 25 giocatori
            
            💰 BUDGET:
            • Budget iniziale: 500 crediti
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
            
            Buon divertimento! ⚽
            """;
        
        JOptionPane.showMessageDialog(this, info, "Informazioni", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Metodi per comunicazione tra panel
     */
    public void notifySquadraCreata() {
        dashboardPanel.refreshData();
        manageTeamPanel.refreshData();
        updateTitle();
        
        // Passa alla tab di gestione squadre
        tabbedPane.setSelectedIndex(2);
    }
    
    public void notifySquadraModificata() {
        dashboardPanel.refreshData();
        manageTeamPanel.refreshData();
        updateTitle();
    }
    
    public void notifySquadraEliminata() {
        dashboardPanel.refreshData();
        updateTitle();
    }
    
    /**
     * Getters per i panel
     */
    public Utente getUtenteCorrente() {
        return utenteCorrente;
    }
    
    public void switchToCreateTeam() {
        tabbedPane.setSelectedIndex(1);
    }
    
    public void switchToManageTeams() {
        tabbedPane.setSelectedIndex(2);
    }
}