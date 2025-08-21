package fantacalcio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import fantacalcio.gui.admin.panels.CalciatoriPanel;
import fantacalcio.gui.admin.panels.SquadrePanel;
import fantacalcio.gui.admin.panels.UtentiPanel;

/**
 * Finestra principale dell'app
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private UtentiPanel utentiPanel;
    private SquadrePanel squadrePanel;
    private CalciatoriPanel calciatoriPanel;
    
    public MainFrame() {
        initializeGUI();
        setLocationRelativeTo(null);
    }
    
    private void initializeGUI() {
        setTitle("FantaCalcio - Pannello Amministrazione");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1200, 700));
        
        createTopPanel();
        createTabbedPane();
        
        pack();
    }
    
    /**
     * Crea il pannello superiore con il titolo
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(33, 150, 243));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("FantaCalcio - Pannello Amministrazione", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        topPanel.add(titleLabel);
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Crea le tab con i panel dedicati
     */
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        
        // Crea i panel specializzati
        utentiPanel = new UtentiPanel(this);
        squadrePanel = new SquadrePanel(this);
        calciatoriPanel = new CalciatoriPanel(this, squadrePanel);
        
        // Aggiungi le tab
        tabbedPane.addTab("Gestione Utenti", utentiPanel);
        tabbedPane.addTab("Squadre Serie A", squadrePanel);
        tabbedPane.addTab("Calciatori", calciatoriPanel);
        
        // Event listener per aggiornare i dati quando si cambia tab
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch (selectedIndex) {
                case 0 -> // Utenti
                    utentiPanel.loadData();
                case 1 -> // Squadre
                    squadrePanel.loadData();
                case 2 -> {
                    // Calciatori
                    calciatoriPanel.refreshSquadreCombo();
                    calciatoriPanel.loadData();
                }
            }
            updateTitle();
        });
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Carica i dati iniziali
        SwingUtilities.invokeLater(this::updateTitle);
    }
    
    /**
     * Aggiorna il titolo della finestra con statistiche
     */
    public void updateTitle() {
        
        setTitle(String.format("FantaCalcio - Pannello Amministrazione"));
    }
    
    /**
     * Metodo per notificare agli altri panel che le squadre sono cambiate
     */
    public void notifySquadreChanged() {
        calciatoriPanel.refreshSquadreCombo();
        updateTitle();
    }
    
    /**
     * Metodo per ottenere il panel attualmente selezionato
     */
    public String getCurrentTabName() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        return tabbedPane.getTitleAt(selectedIndex);
    }
    
    /**
     * Metodo per passare a una tab specifica
     */
    public void switchToTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabIndex);
        }
    }
}