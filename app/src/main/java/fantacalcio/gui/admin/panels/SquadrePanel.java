package fantacalcio.gui.admin.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;

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

import fantacalcio.dao.CalciatoreDAO;
import fantacalcio.dao.SquadraSerieADAO;
import fantacalcio.gui.MainFrame;
import fantacalcio.model.SquadraSerieA;
import fantacalcio.util.DataPopulator;

public class SquadrePanel extends JPanel {
    
    private final MainFrame parentFrame;
    private final SquadraSerieADAO squadraDAO;
    private final CalciatoreDAO calciatoreDAO;
    
    // Componenti GUI
    private JTable tabellaSquadre;
    private DefaultTableModel modelTabella;
    private JTextField txtNomeSquadra;
    private JButton btnAggiungi, btnElimina, btnRefresh;
    
    public SquadrePanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.squadraDAO = new SquadraSerieADAO();
        this.calciatoreDAO = new CalciatoreDAO();
        
        initializeComponents();
        setupLayout();
        loadData();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        createTable();
        createForm();
        createButtons();
    }
    
    private void createTable() {
        String[] colonne = {"Nome Squadra", "Numero Calciatori"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaSquadre = new JTable(modelTabella);
        tabellaSquadre.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaSquadre.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                popolaCampiDaTabella();
            }
        });
        
        // Stile tabella
        tabellaSquadre.setRowHeight(25);
        tabellaSquadre.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaSquadre.getTableHeader().setForeground(Color.WHITE);
        tabellaSquadre.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    }
    
    private void createForm() {
        txtNomeSquadra = new JTextField(25);
        txtNomeSquadra.setToolTipText("Inserisci il nome della squadra");
    }
    
    private void createButtons() {
        btnAggiungi = new JButton("Aggiungi Squadra");
        btnElimina = new JButton("Elimina");
        btnRefresh = new JButton("Aggiorna");
        
        // Stile pulsanti
        styleButton(btnAggiungi, new Color(76, 175, 80));   // Verde
        styleButton(btnElimina, new Color(244, 67, 54));    // Rosso
        styleButton(btnRefresh, new Color(158, 158, 158));  // Grigio
        
        // Tooltip informativi
        btnElimina.setToolTipText("Elimina la squadra selezionata e tutti i suoi calciatori");
        
        // Event listeners
        btnAggiungi.addActionListener(this::aggiungiSquadra);
        btnElimina.addActionListener(this::eliminaSquadra);
        btnRefresh.addActionListener(e -> loadData());
    }
    
    private void setupLayout() {
        // Pannello tabella
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Squadre Serie A:"), BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(tabellaSquadre);
        scrollPane.setPreferredSize(new Dimension(800, 350));
        topPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Pannello form
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formPanel.add(new JLabel("Nome Squadra:"));
        formPanel.add(txtNomeSquadra);
        
        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnAggiungi);
        buttonPanel.add(btnElimina);
        buttonPanel.add(btnRefresh);
        
        // Seconda riga di pulsanti per setup completo
        JPanel setupPanel = new JPanel(new FlowLayout());
        JButton btnSetupCompleto = new JButton("Setup Completo Serie A");
        styleButton(btnSetupCompleto, new Color(156, 39, 176));
        btnSetupCompleto.setToolTipText("Inserisce squadre + giocatori famosi automaticamente");
        btnSetupCompleto.addActionListener(this::setupCompletoSerieA);
        setupPanel.add(btnSetupCompleto);
        
        JPanel allButtonsPanel = new JPanel();
        allButtonsPanel.setLayout(new BoxLayout(allButtonsPanel, BoxLayout.Y_AXIS));
        allButtonsPanel.add(buttonPanel);
        allButtonsPanel.add(setupPanel);
        
        // Pannello statistiche
        JPanel statsPanel = createStatsPanel();
        
        // Pannello inferiore
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JLabel("Gestione Squadre:"), BorderLayout.NORTH);
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(allButtonsPanel, BorderLayout.SOUTH);
        
        // Layout finale
        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(statsPanel, BorderLayout.EAST);
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistiche"));
        statsPanel.setPreferredSize(new Dimension(200, 0));
        
        JLabel lblNumSquadre = new JLabel("Squadre: 0");
        JLabel lblTotCalciatori = new JLabel("Tot. Calciatori: 0");
        JLabel lblMediaCalciatori = new JLabel("Media: 0.0");
        
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(lblNumSquadre);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(lblTotCalciatori);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(lblMediaCalciatori);
        statsPanel.add(Box.createVerticalGlue());
        
        return statsPanel;
    }
    
    public void loadData() {
        modelTabella.setRowCount(0);
        
        List<SquadraSerieA> squadre = squadraDAO.trovaTutteLeSquadre();
        int totalCalciatori = 0;
        
        for (SquadraSerieA squadra : squadre) {
            int numCalciatori = calciatoreDAO.trovaCalciatoriPerSquadra(squadra.getIdSquadraA()).size();
            totalCalciatori += numCalciatori;
            
            Object[] riga = {
                squadra.getIdSquadraA(),
                squadra.getNome(),
                numCalciatori
            };
            modelTabella.addRow(riga);
        }
        
        // Aggiorna statistiche
        updateStats(squadre.size(), totalCalciatori);
    }
    
    private void updateStats(int numSquadre, int totalCalciatori) {
        Component statsPanel = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.EAST);
        if (statsPanel instanceof JPanel jPanel) {
            Component[] components = jPanel.getComponents();
            if (components.length >= 6) {
                ((JLabel) components[1]).setText("Squadre: " + numSquadre);
                ((JLabel) components[3]).setText("Tot. Calciatori: " + totalCalciatori);
                
                double media = numSquadre > 0 ? (double) totalCalciatori / numSquadre : 0.0;
                ((JLabel) components[5]).setText(String.format("Media: %.1f", media));
            }
        }
    }
    
    private void popolaCampiDaTabella() {
        int rigaSelezionata = tabellaSquadre.getSelectedRow();
        if (rigaSelezionata >= 0) {
            String nomeSquadra = (String) modelTabella.getValueAt(rigaSelezionata, 1);
            txtNomeSquadra.setText(nomeSquadra);
        }
    }
    
    private void aggiungiSquadra(ActionEvent e) {
        String nomeSquadra = txtNomeSquadra.getText().trim();
        
        if (nomeSquadra.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Inserisci il nome della squadra!", 
                "Attenzione", 
                JOptionPane.WARNING_MESSAGE);
            txtNomeSquadra.requestFocus();
            return;
        }
        
        // Verifica se esiste già
        if (squadraDAO.trovaSquadraPerNome(nomeSquadra).isPresent()) {
            JOptionPane.showMessageDialog(this, 
                "Una squadra con questo nome esiste già!", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        SquadraSerieA nuovaSquadra = new SquadraSerieA(nomeSquadra);
        
        if (squadraDAO.inserisciSquadra(nuovaSquadra)) {
            JOptionPane.showMessageDialog(this, 
                "Squadra '" + nomeSquadra + "' aggiunta con successo!", 
                "Successo", 
                JOptionPane.INFORMATION_MESSAGE);
            loadData();
            txtNomeSquadra.setText("");
            txtNomeSquadra.requestFocus();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Errore durante l'inserimento della squadra.", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminaSquadra(ActionEvent e) {
        int rigaSelezionata = tabellaSquadre.getSelectedRow();
        if (rigaSelezionata < 0) {
            JOptionPane.showMessageDialog(this, 
                "Seleziona una squadra dalla tabella per eliminarla.", 
                "Attenzione", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nomeSquadra = (String) modelTabella.getValueAt(rigaSelezionata, 1);
        int numCalciatori = (Integer) modelTabella.getValueAt(rigaSelezionata, 2);
        
        String message = "Sei sicuro di voler eliminare la squadra '" + nomeSquadra + "'?";
        if (numCalciatori > 0) {
            message += "\nQuesta azione eliminerà anche tutti i " + numCalciatori + " calciatori associati!";
        }
        
        int conferma = JOptionPane.showConfirmDialog(this, 
            message, 
            "Conferma Eliminazione", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            int idSquadra = (Integer) modelTabella.getValueAt(rigaSelezionata, 0);
            
            if (squadraDAO.eliminaSquadra(idSquadra)) {
                JOptionPane.showMessageDialog(this, 
                    "Squadra '" + nomeSquadra + "' eliminata con successo!", 
                    "Successo", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
                txtNomeSquadra.setText("");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Errore durante l'eliminazione della squadra.", 
                    "Errore", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Setup completo: squadre + giocatori famosi
     */
    private void setupCompletoSerieA(ActionEvent e) {
        // Controlla se ci sono già dati
        int numSquadre = squadraDAO.contaSquadre();
        int numCalciatori = calciatoreDAO.contaCalciatoriPerRuolo().values()
                                        .stream().mapToInt(Integer::intValue).sum();
        
        String message = "Setup Completo Serie A 2024/25\n\n";
        message += "Questo inserirà automaticamente:\n";
        message += " 20 squadre di Serie A\n";
        message += " Molti giocatori di quella annata\n";
        message += " Costi automatici per ogni giocatore\n\n";
        
        if (numSquadre > 0 || numCalciatori > 0) {
            message += "ATTENZIONE: Ci sono già " + numSquadre + " squadre e " + numCalciatori + " calciatori.\n";
            message += "I duplicati verranno ignorati.\n\n";
        }
        
        message += "Procedere con il setup completo?";
        
        int conferma = JOptionPane.showConfirmDialog(this, 
            message, 
            "Setup Completo Serie A", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            // Usa SwingWorker per processo lungo
            SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish("Setup completo in corso...");
                    
                    DataPopulator populator = new DataPopulator();
                    populator.popolaCompletamente();
                    
                    return null;
                }
                
                @Override
                protected void process(List<String> chunks) {
                    // Qui potresti mostrare progress
                    for (String msg : chunks) {
                        System.out.println(msg);
                    }
                }
                
                @Override
                protected void done() {
                    loadData();
                    
                    // Controlla se parentFrame non è null prima di usarlo
                    if (parentFrame != null) {
                        Component[] components = parentFrame.getContentPane().getComponents();
                        for (Component c : components) {
                            if (c instanceof fantacalcio.gui.admin.panels.CalciatoriPanel calciatoriPanel) {
                                calciatoriPanel.loadData();
                            }
                        }
                        parentFrame.notifySquadreChanged();
                    }
                    
                    // Mostra risultati finali
                    int finalSquadre = squadraDAO.contaSquadre();
                    int finalCalciatori = calciatoreDAO.contaCalciatoriPerRuolo().values()
                                                    .stream().mapToInt(Integer::intValue).sum();
                    
                    String risultato = "Setup completato con successo!\n\n";
                    risultato += "Database popolato con:\n";
                    risultato += "• " + finalSquadre + " squadre di Serie A\n";
                    risultato += "• " + finalCalciatori + " calciatori\n\n";
                    risultato += "Ora puoi iniziare a creare squadre fantacalcio!";
                    
                    JOptionPane.showMessageDialog(SquadrePanel.this, 
                        risultato, 
                        "Setup Completato", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            };
            
            worker.execute();
        }
    }
    
    // METODO STYLEBUTTON CORRETTO - questo era mancante!
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    public List<SquadraSerieA> getAllSquadre() {
        return squadraDAO.trovaTutteLeSquadre();
    }
    
    public int getNumSquadre() {
        return squadraDAO.contaSquadre();
    }
}