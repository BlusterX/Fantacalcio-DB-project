package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import fantacalcio.dao.SquadraFantacalcioDAO;
import fantacalcio.gui.user.UserMainFrame;
import fantacalcio.model.Lega;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;

public class LeagueDetailPanel extends JPanel {
    
    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final Lega lega;
    private final SquadraFantacalcioDAO squadraDAO;
    
    private SquadraFantacalcio squadraUtente; // LA squadra dell'utente per questa lega
    private JPanel contentPanel;
    
    public LeagueDetailPanel(UserMainFrame parentFrame, Utente utente, Lega lega) {
        this.parentFrame = parentFrame;
        this.utenteCorrente = utente;
        this.lega = lega;
        this.squadraDAO = new SquadraFantacalcioDAO();
        
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
        
        // Header con info squadra
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
        
        // Panel per gestire la squadra esistente - usa quello esistente senza override
        ManageTeamPanel managePanel = new ManageTeamPanel(parentFrame, utenteCorrente);
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(managePanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
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
}