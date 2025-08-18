package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.SquadraFantacalcioDAO;
import fantacalcio.gui.user.UserMainFrame;
import fantacalcio.gui.user.dialog.FormationManagementDialog;
import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;

/**
 * Panel per la gestione delle squadre fantacalcio esistenti
 */
public class ManageTeamPanel extends JPanel {
    
    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final SquadraFantacalcioDAO squadraDAO;
    
    // Componenti GUI
    private JTable tabellaSquadre;
    private DefaultTableModel modelTabellaSquadre;
    private JTable tabellaGiocatori;
    private DefaultTableModel modelTabellaGiocatori;
    
    private JButton btnModifica, btnElimina, btnRefresh;
    private JLabel lblSquadraSelezionata, lblStatistiche;
    
    // Squadra attualmente selezionata
    private SquadraFantacalcio squadraSelezionata;
    
    public ManageTeamPanel(UserMainFrame parentFrame, Utente utente) {
        this.parentFrame = parentFrame;
        this.utenteCorrente = utente;
        this.squadraDAO = new SquadraFantacalcioDAO();
        
        initializeComponents();
        setupLayout();
        refreshData();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        createSquadreTable();
        createGiocatoriTable();
        createButtons();
        createLabels();
    }
    
    private void createSquadreTable() {
        String[] colonneSquadre = {"Nome Squadra", "Giocatori", "Budget Speso", "Stato", "Ultima Modifica"};
        modelTabellaSquadre = new DefaultTableModel(colonneSquadre, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaSquadre = new JTable(modelTabellaSquadre);
        tabellaSquadre.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaSquadre.setRowHeight(30);
        
        // Event listener per selezione squadra
        tabellaSquadre.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                caricaSquadraSelezionata();
            }
        });
        
        // Stile tabella
        tabellaSquadre.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaSquadre.getTableHeader().setForeground(Color.WHITE);
        tabellaSquadre.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    }
    
    private void createGiocatoriTable() {
        String[] colonneGiocatori = {"Ruolo", "Nome", "Cognome", "Squadra Serie A", "Costo"};
        modelTabellaGiocatori = new DefaultTableModel(colonneGiocatori, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaGiocatori = new JTable(modelTabellaGiocatori);
        tabellaGiocatori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaGiocatori.setRowHeight(25);
        
        // Stile tabella
        tabellaGiocatori.getTableHeader().setBackground(new Color(76, 175, 80));
        tabellaGiocatori.getTableHeader().setForeground(Color.WHITE);
        tabellaGiocatori.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Larghezza colonne
        tabellaGiocatori.getColumnModel().getColumn(0).setPreferredWidth(60);  // Ruolo
        tabellaGiocatori.getColumnModel().getColumn(1).setPreferredWidth(100); // Nome
        tabellaGiocatori.getColumnModel().getColumn(2).setPreferredWidth(100); // Cognome
        tabellaGiocatori.getColumnModel().getColumn(3).setPreferredWidth(150); // Squadra
        tabellaGiocatori.getColumnModel().getColumn(4).setPreferredWidth(70);  // Costo

        tabellaGiocatori.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && squadraSelezionata != null) {
                    aprireGestioneFormazione();
                }
            }
        });
    }
    
    private void aprireGestioneFormazione() {
        if (squadraSelezionata == null) {
            JOptionPane.showMessageDialog(this,
                "Seleziona prima una squadra.",
                "Nessuna squadra selezionata",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window parent = SwingUtilities.getWindowAncestor(this);
        FormationManagementDialog dialog =
                new FormationManagementDialog(parent, squadraSelezionata, utenteCorrente);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void createButtons() {
        btnModifica = new JButton("📝 Modifica Squadra");
        btnElimina = new JButton("🗑️ Elimina Squadra");
        btnRefresh = new JButton("🔄 Aggiorna");
        
        // Stile pulsanti
        btnModifica.setBackground(new Color(33, 150, 243));
        btnModifica.setForeground(Color.WHITE);
        btnModifica.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnModifica.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnModifica.setFocusPainted(false);
        btnModifica.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnModifica.setEnabled(false);
        
        btnElimina.setBackground(new Color(244, 67, 54));
        btnElimina.setForeground(Color.WHITE);
        btnElimina.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnElimina.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnElimina.setFocusPainted(false);
        btnElimina.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnElimina.setEnabled(false);
        
        btnRefresh.setBackground(new Color(158, 158, 158));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Event listeners
        btnModifica.addActionListener(this::modificaSquadra);
        btnElimina.addActionListener(this::eliminaSquadra);
        btnRefresh.addActionListener(e -> refreshData());
    }
    
    private void createLabels() {
        lblSquadraSelezionata = new JLabel("Seleziona una squadra per visualizzare i dettagli");
        lblSquadraSelezionata.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        lblSquadraSelezionata.setForeground(new Color(100, 100, 100));
        lblSquadraSelezionata.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblStatistiche = new JLabel(" ");
        lblStatistiche.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblStatistiche.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private void setupLayout() {
        // Pannello superiore - lista squadre
        JPanel topPanel = createSquadrePanel();
        
        // Pannello inferiore - dettagli squadra selezionata
        JPanel bottomPanel = createDettagliPanel();
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.4);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createSquadrePanel() {
        JPanel squadrePanel = new JPanel(new BorderLayout());
        squadrePanel.setBorder(BorderFactory.createTitledBorder("📋 Le tue squadre"));
        
        // Tabella squadre
        JScrollPane scrollSquadre = new JScrollPane(tabellaSquadre);
        scrollSquadre.setPreferredSize(new Dimension(800, 200));
        
        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnModifica);
        buttonPanel.add(btnElimina);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnRefresh);
        
        squadrePanel.add(scrollSquadre, BorderLayout.CENTER);
        squadrePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return squadrePanel;
    }
    
    private JPanel createDettagliPanel() {
        JPanel dettagliPanel = new JPanel(new BorderLayout());
        dettagliPanel.setBorder(BorderFactory.createTitledBorder("⚽ Dettagli squadra"));
        
        // Header con info squadra
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        headerPanel.add(lblSquadraSelezionata, BorderLayout.CENTER);
        headerPanel.add(lblStatistiche, BorderLayout.SOUTH);
        
        // Tabella giocatori
        JScrollPane scrollGiocatori = new JScrollPane(tabellaGiocatori);
        scrollGiocatori.setPreferredSize(new Dimension(800, 300));
        
        dettagliPanel.add(headerPanel, BorderLayout.NORTH);
        dettagliPanel.add(scrollGiocatori, BorderLayout.CENTER);
        
        return dettagliPanel;
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
                    updateSquadreTable(squadre);
                    
                    // Reset selezione
                    squadraSelezionata = null;
                    updateDettagli();
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ManageTeamPanel.this,
                        "Errore nel caricamento delle squadre: " + e.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateSquadreTable(List<SquadraFantacalcio> squadre) {
        modelTabellaSquadre.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (SquadraFantacalcio squadra : squadre) {
            String stato = squadra.isCompletata() ? "✅ Completa" : "⚠️ In costruzione";
            String ultimaModifica = squadra.getDataUltimaModifica().format(formatter);
            
            Object[] riga = {
                squadra.getNomeSquadra(),
                squadra.getNumeroTotaleGiocatori() + "/25",
                squadra.getBudgetSpeso() + " €",
                stato,
                ultimaModifica
            };
            modelTabellaSquadre.addRow(riga);
        }
    }
    
    private void caricaSquadraSelezionata() {
        int rigaSelezionata = tabellaSquadre.getSelectedRow();
        if (rigaSelezionata < 0) {
            squadraSelezionata = null;
            updateDettagli();
            return;
        }
        
        String nomeSquadra = (String) modelTabellaSquadre.getValueAt(rigaSelezionata, 0);
        
        SwingWorker<SquadraFantacalcio, Void> worker = new SwingWorker<SquadraFantacalcio, Void>() {
            @Override
            protected SquadraFantacalcio doInBackground() throws Exception {
                // Trova la squadra per nome (potremmo migliorare con ID)
                List<SquadraFantacalcio> squadre = squadraDAO.trovaSquadrePerUtente(utenteCorrente.getIdUtente());
                return squadre.stream()
                             .filter(s -> s.getNomeSquadra().equals(nomeSquadra))
                             .findFirst()
                             .orElse(null);
            }
            
            @Override
            protected void done() {
                try {
                    squadraSelezionata = get();
                    updateDettagli();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ManageTeamPanel.this,
                        "Errore nel caricamento della squadra: " + e.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateDettagli() {
        if (squadraSelezionata == null) {
            lblSquadraSelezionata.setText("Seleziona una squadra per visualizzare i dettagli");
            lblStatistiche.setText(" ");
            modelTabellaGiocatori.setRowCount(0);
            btnModifica.setEnabled(false);
            btnElimina.setEnabled(false);
            return;
        }
        
        // Aggiorna header
        lblSquadraSelezionata.setText("🏆 " + squadraSelezionata.getNomeSquadra());
        lblStatistiche.setText(squadraSelezionata.getStatistiche());
        
        // Aggiorna tabella giocatori
        updateGiocatoriTable();
        
        // Abilita pulsanti
        btnModifica.setEnabled(true);
        btnElimina.setEnabled(true);
        
        // Colora statistiche in base al completamento
        if (squadraSelezionata.isCompletata()) {
            lblStatistiche.setForeground(new Color(76, 175, 80));
        } else {
            lblStatistiche.setForeground(new Color(255, 152, 0));
        }
    }
    
    private void updateGiocatoriTable() {
        modelTabellaGiocatori.setRowCount(0);
        
        List<Calciatore> giocatori = squadraSelezionata.getCalciatori();
        
        // Ordina per ruolo
        giocatori.sort((c1, c2) -> {
            int ruoloCompare = c1.getRuolo().compareTo(c2.getRuolo());
            if (ruoloCompare != 0) return ruoloCompare;
            return c1.getCognome().compareTo(c2.getCognome());
        });
        
        for (Calciatore calciatore : giocatori) {
            Object[] riga = {
                calciatore.getRuolo().getAbbreviazione(),
                calciatore.getNome(),
                calciatore.getCognome(),
                "Serie A", // Placeholder - potresti caricare la squadra reale
                calciatore.getCosto() + " €"
            };
            modelTabellaGiocatori.addRow(riga);
        }
    }
    
    private void modificaSquadra(ActionEvent e) {
        if (squadraSelezionata == null) return;
        
        // Per ora, solo possibilità di cambiare nome
        String nuovoNome = JOptionPane.showInputDialog(this,
            "Inserisci il nuovo nome per la squadra:",
            "Modifica Nome Squadra",
            JOptionPane.QUESTION_MESSAGE);
        
        if (nuovoNome != null && !nuovoNome.trim().isEmpty()) {
            nuovoNome = nuovoNome.trim();
            
            // Verifica nome duplicato
            if (!nuovoNome.equals(squadraSelezionata.getNomeSquadra()) && 
                squadraDAO.nomeSquadraEsiste(nuovoNome, utenteCorrente.getIdUtente())) {
                JOptionPane.showMessageDialog(this,
                    "Hai già una squadra con questo nome!",
                    "Nome Duplicato",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            squadraSelezionata.setNomeSquadra(nuovoNome);
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return squadraDAO.aggiornaSquadra(squadraSelezionata);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean successo = get();
                        
                        if (successo) {
                            JOptionPane.showMessageDialog(ManageTeamPanel.this,
                                "Nome squadra aggiornato con successo!",
                                "Successo",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            refreshData();
                            parentFrame.updateTitle();
                        } else {
                            JOptionPane.showMessageDialog(ManageTeamPanel.this,
                                "Errore durante l'aggiornamento del nome.",
                                "Errore",
                                JOptionPane.ERROR_MESSAGE);
                        }
                        
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ManageTeamPanel.this,
                            "Errore: " + ex.getMessage(),
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void eliminaSquadra(ActionEvent e) {
        if (squadraSelezionata == null) return;
        
        String message = "Sei sicuro di voler eliminare la squadra '" + 
                        squadraSelezionata.getNomeSquadra() + "'?\n\n" +
                        "Questa azione non può essere annullata!";
        
        int conferma = JOptionPane.showConfirmDialog(this,
            message,
            "Conferma Eliminazione",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return squadraDAO.eliminaSquadra(squadraSelezionata.getIdSquadraFantacalcio());
                }
                
                @Override
                protected void done() {
                    try {
                        boolean successo = get();
                        
                        if (successo) {
                            JOptionPane.showMessageDialog(ManageTeamPanel.this,
                                "Squadra eliminata con successo!",
                                "Successo",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            refreshData();
                            parentFrame.notifySquadraEliminata();
                            
                        } else {
                            JOptionPane.showMessageDialog(ManageTeamPanel.this,
                                "Errore durante l'eliminazione della squadra.",
                                "Errore",
                                JOptionPane.ERROR_MESSAGE);
                        }
                        
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ManageTeamPanel.this,
                            "Errore: " + ex.getMessage(),
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
        }
    }
}