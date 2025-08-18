package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.SquadraFantacalcioDAO;
import fantacalcio.gui.user.UserMainFrame;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;

/**
 * Panel dashboard per l'utente - panoramica delle squadre e statistiche
 */
public class UserDashboardPanel extends JPanel {
    
    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final SquadraFantacalcioDAO squadraDAO;
    
    // Componenti GUI
    private JTable tabellaSquadre;
    private DefaultTableModel modelTabella;
    private JLabel lblNumSquadre, lblSquadreComplete, lblBudgetTotale, lblUltimaAttivita;
    private JButton btnGestisciLeghe;    
    // Statistiche
    private int numeroSquadreUtente = 0;
    private int numeroSquadreComplete = 0;
    
    public UserDashboardPanel(UserMainFrame parentFrame, Utente utente) {
        this.parentFrame = parentFrame;
        this.utenteCorrente = utente;
        this.squadraDAO = new SquadraFantacalcioDAO();
        
        initializeComponents();
        setupLayout();
        refreshData();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createStatsPanel();
        createTablePanel();
        createQuickActionsPanel();
    }
    
    private void createStatsPanel() {
        lblNumSquadre = new JLabel("0");
        lblSquadreComplete = new JLabel("0");
        lblBudgetTotale = new JLabel("0");
        lblUltimaAttivita = new JLabel("Mai");
        
        // Stile per le statistiche
        Font statsFont = new Font(Font.SANS_SERIF, Font.BOLD, 24);
        lblNumSquadre.setFont(statsFont);
        lblSquadreComplete.setFont(statsFont);
        lblBudgetTotale.setFont(statsFont);
    }
    
    private void createTablePanel() {
        String[] colonne = {"Nome Squadra", "Giocatori", "Budget Speso", "Budget Rimanente", "Stato", "Ultima Modifica"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaSquadre = new JTable(modelTabella);
        tabellaSquadre.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaSquadre.setRowHeight(30);
        
        // Stile tabella
        tabellaSquadre.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaSquadre.getTableHeader().setForeground(Color.WHITE);
        tabellaSquadre.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Larghezza colonne
        tabellaSquadre.getColumnModel().getColumn(0).setPreferredWidth(200); // Nome
        tabellaSquadre.getColumnModel().getColumn(1).setPreferredWidth(80);  // Giocatori
        tabellaSquadre.getColumnModel().getColumn(2).setPreferredWidth(100); // Budget Speso
        tabellaSquadre.getColumnModel().getColumn(3).setPreferredWidth(120); // Budget Rimanente
        tabellaSquadre.getColumnModel().getColumn(4).setPreferredWidth(100); // Stato
        tabellaSquadre.getColumnModel().getColumn(5).setPreferredWidth(150); // Ultima Modifica
    }
    
    private void createQuickActionsPanel() {

        btnGestisciLeghe = new JButton("🏆 Le mie Leghe");
        styleButton(btnGestisciLeghe, new Color(33, 150, 243));
        btnGestisciLeghe.addActionListener(e -> parentFrame.switchToLeagues());

    }
    
    private void setupLayout() {
        
        // Pannello tabella al centro
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("📋 Le tue squadre"));
        
        JScrollPane scrollPane = new JScrollPane(tabellaSquadre);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Pannello azioni in basso - SOLO con il pulsante leghe
        JPanel actionsPanel = new JPanel(new FlowLayout());
        actionsPanel.add(btnGestisciLeghe);
        
        // Layout finale
        add(tablePanel, BorderLayout.CENTER);
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    public void refreshData() {
        SwingWorker<List<SquadraFantacalcio>, Void> worker = new SwingWorker<List<SquadraFantacalcio>, Void>() {
            @Override
            protected List<SquadraFantacalcio> doInBackground() throws Exception {
                return squadraDAO.trovaSquadrePerUtente(utenteCorrente.getIdUtente());
            }
            
            @Override
            protected void done() {
                try {
                    List<SquadraFantacalcio> squadre = get();
                    updateTable(squadre);
                    updateStats(squadre);
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(UserDashboardPanel.this,
                        "Errore nel caricamento dei dati: " + e.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateTable(List<SquadraFantacalcio> squadre) {
        modelTabella.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (SquadraFantacalcio squadra : squadre) {
            String stato = squadra.isCompletata() ? "✅ Completa" : "⚠️ In costruzione";
            String ultimaModifica = squadra.getDataUltimaModifica().format(formatter);
            
            Object[] riga = {
                squadra.getNomeSquadra(),
                squadra.getNumeroTotaleGiocatori() + "/" + SquadraFantacalcio.TOTALE_GIOCATORI,
                squadra.getBudgetSpeso() + " €",
                squadra.getBudgetRimanente() + " €",
                stato,
                ultimaModifica
            };
            modelTabella.addRow(riga);
        }
    }
    
    private void updateStats(List<SquadraFantacalcio> squadre) {
        numeroSquadreUtente = squadre.size();
        numeroSquadreComplete = (int) squadre.stream().filter(SquadraFantacalcio::isCompletata).count();
        
        int budgetTotaleSpeso = squadre.stream()
                                     .mapToInt(SquadraFantacalcio::getBudgetSpeso)
                                     .sum();
        
        String ultimaAttivita = "Mai";
        if (!squadre.isEmpty()) {
            ultimaAttivita = squadre.stream()
                                   .map(SquadraFantacalcio::getDataUltimaModifica)
                                   .max(java.time.LocalDateTime::compareTo)
                                   .map(dt -> dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                   .orElse("Mai");
        }
        
        // Aggiorna le label
        lblNumSquadre.setText(String.valueOf(numeroSquadreUtente));
        lblSquadreComplete.setText(String.valueOf(numeroSquadreComplete));
        lblBudgetTotale.setText(budgetTotaleSpeso + " €");
        lblUltimaAttivita.setText(ultimaAttivita);
        
        // Aggiorna dimensione font per ultima attività se necessario
        if (ultimaAttivita.length() > 10) {
            lblUltimaAttivita.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        } else {
            lblUltimaAttivita.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        }
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    // Getters per statistiche
    public int getNumeroSquadreUtente() {
        return numeroSquadreUtente;
    }
    
    public int getNumeroSquadreComplete() {
        return numeroSquadreComplete;
    }
}