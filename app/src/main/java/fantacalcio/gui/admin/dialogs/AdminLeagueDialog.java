package fantacalcio.gui.admin.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import fantacalcio.gui.admin.panels.AdminLeaguePanel;
import fantacalcio.gui.admin.panels.CalciatoriPanel;
import fantacalcio.gui.admin.panels.SquadrePanel;
import fantacalcio.model.Utente;

/**
 * Dialog per la gestione admin delle leghe
 */
public class AdminLeagueDialog extends JDialog {
    
    private final Utente adminUtente;
    private JTabbedPane tabbedPane;
    
    // Panels
    private AdminLeaguePanel adminLeaguePanel;
    private SquadrePanel squadrePanel;
    private CalciatoriPanel calciatoriPanel;
    
    public AdminLeagueDialog(Frame parent, Utente adminUtente) {
        super(parent, "Admin Panel - Gestione Leghe", true);
        this.adminUtente = adminUtente;
        
        initializeGUI();
        setLocationRelativeTo(parent);
    }
    
    private void initializeGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1200, 700));
        
        createTabbedPane();
        
        pack();
    }
    
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        
        // Crea i panel
        adminLeaguePanel = new AdminLeaguePanel(null, adminUtente);
        squadrePanel = new SquadrePanel(null);
        calciatoriPanel = new CalciatoriPanel(null, squadrePanel);
        
        // Aggiungi le tab
        tabbedPane.addTab("🏆 Gestione Leghe", adminLeaguePanel);
        tabbedPane.addTab("⚽ Squadre Serie A", squadrePanel);
        tabbedPane.addTab("👤 Calciatori", calciatoriPanel);
        
        // Event listener per aggiornare i dati quando si cambia tab
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch (selectedIndex) {
                case 0 -> // Leghe
                    adminLeaguePanel.loadData();
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
        javax.swing.SwingUtilities.invokeLater(this::updateTitle);
    }
    
    /**
     * Aggiorna il titolo del dialog
     */
    public void updateTitle() {
        int numLeghe = adminLeaguePanel.getNumLeghe();
        int numSquadre = squadrePanel.getNumSquadre();
        int numCalciatori = calciatoriPanel.getNumCalciatori();
        
        setTitle(String.format("Admin Panel - %s (%d leghe, %d squadre, %d calciatori)", 
                              adminUtente.getNickname(), numLeghe, numSquadre, numCalciatori));
    }
    
    /**
     * Metodo per notificare agli altri panel che le squadre sono cambiate
     */
    public void notifySquadreChanged() {
        calciatoriPanel.refreshSquadreCombo();
        updateTitle();
    }
}