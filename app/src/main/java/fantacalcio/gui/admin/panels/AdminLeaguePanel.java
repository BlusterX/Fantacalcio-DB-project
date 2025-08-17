package fantacalcio.gui.admin.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.gui.MainFrame;
import fantacalcio.model.Lega;
import fantacalcio.model.Utente;
import fantacalcio.util.DataPopulator;

/**
 * Panel per la gestione delle leghe da parte dell'admin
 */
public class AdminLeaguePanel extends JPanel {
    
    private final MainFrame parentFrame;
    private final Utente adminUtente;
    private final LegaDAO legaDAO;
    
    // Componenti GUI
    private JTable tabellaLeghe;
    private DefaultTableModel modelTabella;
    private JTextField txtNomeLega;
    private JButton btnCreaLega, btnEliminaLega, btnRefresh, btnPopola;
    private JComboBox<Lega> comboLeghePopola;
    
    // Lega attualmente selezionata
    private Lega legaSelezionata;
    
    // Statistiche
    private JLabel lblNumLeghe, lblPartecipantiTotali;
    
    public AdminLeaguePanel(MainFrame parentFrame, Utente adminUtente) {
        this.parentFrame = parentFrame;
        this.adminUtente = adminUtente;
        this.legaDAO = new LegaDAO();
        
        initializeComponents();
        setupLayout();
        loadData();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        createTable();
        createForm();
        createButtons();
        createStats();
    }
    
    private void createTable() {
        String[] colonne = {"ID", "Nome Lega", "Codice Accesso", "Partecipanti"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaLeghe = new JTable(modelTabella);
        tabellaLeghe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaLeghe.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selezionaLega();
            }
        });
        
        // Stile tabella
        tabellaLeghe.setRowHeight(30);
        tabellaLeghe.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaLeghe.getTableHeader().setForeground(Color.WHITE);
        tabellaLeghe.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Larghezza colonne
        tabellaLeghe.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tabellaLeghe.getColumnModel().getColumn(1).setPreferredWidth(200); // Nome
        tabellaLeghe.getColumnModel().getColumn(2).setPreferredWidth(120); // Codice
        tabellaLeghe.getColumnModel().getColumn(3).setPreferredWidth(100); // Partecipanti
    }
    
    private void createForm() {
        txtNomeLega = new JTextField(25);
        txtNomeLega.setToolTipText("Inserisci il nome della lega");
        
        comboLeghePopola = new JComboBox<>();
        comboLeghePopola.setToolTipText("Seleziona la lega da popolare");
    }
    
    private void createButtons() {
        btnCreaLega = new JButton("🏆 Crea Lega");
        btnEliminaLega = new JButton("🗑️ Elimina Lega");
        btnRefresh = new JButton("🔄 Aggiorna");
        btnPopola = new JButton("⚽ Popola Lega");
        
        // Stile pulsanti
        styleButton(btnCreaLega, new Color(76, 175, 80));
        styleButton(btnEliminaLega, new Color(244, 67, 54));
        styleButton(btnRefresh, new Color(158, 158, 158));
        styleButton(btnPopola, new Color(156, 39, 176));
        
        // Tooltip informativi
        btnCreaLega.setToolTipText("Crea una nuova lega con codice di accesso automatico");
        btnEliminaLega.setToolTipText("Elimina la lega selezionata");
        btnPopola.setToolTipText("Popola la lega selezionata con squadre e calciatori");
        
        // Event listeners
        btnCreaLega.addActionListener(this::creaLega);
        btnEliminaLega.addActionListener(this::eliminaLega);
        btnRefresh.addActionListener(e -> loadData());
        btnPopola.addActionListener(this::popolaLega);
        
        // Inizialmente disabilita pulsanti che richiedono selezione
        btnEliminaLega.setEnabled(false);
    }
    
    private void createStats() {
        lblNumLeghe = new JLabel("0");
        lblPartecipantiTotali = new JLabel("0");
        
        // Stile per le statistiche
        Font statsFont = new Font(Font.SANS_SERIF, Font.BOLD, 24);
        lblNumLeghe.setFont(statsFont);
        lblPartecipantiTotali.setFont(statsFont);
    }
    
    private void setupLayout() {
        // Pannello superiore - header e form
        JPanel topPanel = createTopPanel();
        
        // Pannello centrale - tabella
        JPanel centerPanel = createCenterPanel();
        
        // Pannello destro - statistiche e azioni
        JPanel rightPanel = createRightPanel();
        
        // Layout finale
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("🏆 Gestione Leghe Admin");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 150, 243));
        
        JLabel subtitleLabel = new JLabel("Crea e gestisci le tue leghe fantacalcio");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Form per creazione lega
        JPanel formPanel = createFormPanel();
        
        topPanel.add(headerPanel, BorderLayout.CENTER);
        topPanel.add(formPanel, BorderLayout.SOUTH);
        
        return topPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Crea Nuova Lega"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nome Lega:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(txtNomeLega, gbc);
        
        gbc.gridx = 2;
        formPanel.add(btnCreaLega, gbc);
        
        gbc.gridx = 3;
        formPanel.add(btnRefresh, gbc);
        
        return formPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Le tue Leghe"));
        
        JScrollPane scrollPane = new JScrollPane(tabellaLeghe);
        scrollPane.setPreferredSize(new Dimension(700, 350));
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Pannello pulsanti sotto la tabella
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnEliminaLega);
        
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return centerPanel;
    }
    
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Statistiche e Azioni"));
        rightPanel.setPreferredSize(new Dimension(250, 0));
        
        // Statistiche
        JPanel statsPanel = createStatsPanel();
        
        // Separator
        JSeparator separator = new JSeparator();
        
        // Pannello popolamento
        JPanel populatePanel = createPopulatePanel();
        
        rightPanel.add(statsPanel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(separator);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(populatePanel);
        rightPanel.add(Box.createVerticalGlue());
        
        return rightPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        
        JPanel leghePanel = createStatCard("📊 Tue Leghe:", lblNumLeghe, new Color(33, 150, 243));
        JPanel partecipantiPanel = createStatCard("👥 Partecipanti Tot.:", lblPartecipantiTotali, new Color(76, 175, 80));
        
        statsPanel.add(leghePanel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(partecipantiPanel);
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String titolo, JLabel valore, Color colore) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(colore, 1));
        panel.setBackground(Color.WHITE);
        
        JLabel titoloLabel = new JLabel(titolo);
        titoloLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titoloLabel.setForeground(colore);
        titoloLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        
        valore.setForeground(colore);
        valore.setHorizontalAlignment(JLabel.CENTER);
        valore.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        
        panel.add(titoloLabel, BorderLayout.NORTH);
        panel.add(valore, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPopulatePanel() {
        JPanel populatePanel = new JPanel();
        populatePanel.setLayout(new BoxLayout(populatePanel, BoxLayout.Y_AXIS));
        populatePanel.setBorder(BorderFactory.createTitledBorder("Popola Lega"));
        
        JLabel lblInfo = new JLabel("Seleziona lega:");
        lblInfo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        populatePanel.add(lblInfo);
        populatePanel.add(Box.createVerticalStrut(5));
        populatePanel.add(comboLeghePopola);
        populatePanel.add(Box.createVerticalStrut(10));
        populatePanel.add(btnPopola);
        
        JLabel lblInfoPopola = new JLabel("<html><small>Popola la lega con squadre Serie A e calciatori famosi</small></html>");
        lblInfoPopola.setForeground(new Color(100, 100, 100));
        populatePanel.add(Box.createVerticalStrut(5));
        populatePanel.add(lblInfoPopola);
        
        return populatePanel;
    }
    
    public void loadData() {
        SwingWorker<List<Lega>, Void> worker = new SwingWorker<List<Lega>, Void>() {
            @Override
            protected List<Lega> doInBackground() throws Exception {
                return legaDAO.trovaLeghePerAdmin(adminUtente.getIdUtente());
            }
            
            @Override
            protected void done() {
                try {
                    List<Lega> leghe = get();
                    updateTable(leghe);
                    updateCombo(leghe);
                    updateStats(leghe);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                        "Errore nel caricamento delle leghe: " + e.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateTable(List<Lega> leghe) {
        modelTabella.setRowCount(0);
        
        for (Lega lega : leghe) {
            int partecipanti = legaDAO.contaPartecipanti(lega.getIdLega());
            
            Object[] riga = {
                lega.getIdLega(),
                lega.getNome(),
                lega.getCodiceAccesso(),
                partecipanti
            };
            modelTabella.addRow(riga);
        }
    }
    
    private void updateCombo(List<Lega> leghe) {
        comboLeghePopola.removeAllItems();
        for (Lega lega : leghe) {
            comboLeghePopola.addItem(lega);
        }
    }
    
    private void updateStats(List<Lega> leghe) {
        int totalPartecipanti = 0;
        for (Lega lega : leghe) {
            totalPartecipanti += legaDAO.contaPartecipanti(lega.getIdLega());
        }
        
        lblNumLeghe.setText(String.valueOf(leghe.size()));
        lblPartecipantiTotali.setText(String.valueOf(totalPartecipanti));
    }
    
    private void selezionaLega() {
        int rigaSelezionata = tabellaLeghe.getSelectedRow();
        if (rigaSelezionata >= 0) {
            int idLega = (Integer) modelTabella.getValueAt(rigaSelezionata, 0);
            
            // Trova la lega nella lista
            legaSelezionata = null;
            for (int i = 0; i < comboLeghePopola.getItemCount(); i++) {
                Lega lega = comboLeghePopola.getItemAt(i);
                if (lega.getIdLega() == idLega) {
                    legaSelezionata = lega;
                    comboLeghePopola.setSelectedItem(lega);
                    break;
                }
            }
            
            btnEliminaLega.setEnabled(true);
        } else {
            legaSelezionata = null;
            btnEliminaLega.setEnabled(false);
        }
    }
    
    private void creaLega(ActionEvent e) {
        String nomeLega = txtNomeLega.getText().trim();
        
        if (nomeLega.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Inserisci il nome della lega!",
                "Attenzione",
                JOptionPane.WARNING_MESSAGE);
            txtNomeLega.requestFocus();
            return;
        }
        
        // Verifica se esiste già
        if (legaDAO.nomeLegaEsiste(nomeLega, adminUtente.getIdUtente())) {
            JOptionPane.showMessageDialog(this,
                "Hai già una lega con questo nome!",
                "Nome Duplicato",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Lega nuovaLega = new Lega(nomeLega, adminUtente.getIdUtente());
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return legaDAO.creaLega(nuovaLega);
            }
            
            @Override
            protected void done() {
                try {
                    boolean successo = get();
                    
                    if (successo) {
                        JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                            "Lega '" + nomeLega + "' creata con successo!\n" +
                            "Codice di accesso: " + nuovaLega.getCodiceAccesso(),
                            "Successo",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        txtNomeLega.setText("");
                        loadData();
                        
                    } else {
                        JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                            "Errore durante la creazione della lega.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                        "Errore: " + ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void eliminaLega(ActionEvent e) {
        if (legaSelezionata == null) {
            JOptionPane.showMessageDialog(this,
                "Seleziona una lega dalla tabella per eliminarla.",
                "Attenzione",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nomeSquadra = legaSelezionata.getNome();
        int partecipanti = legaDAO.contaPartecipanti(legaSelezionata.getIdLega());
        
        String message = "Sei sicuro di voler eliminare la lega '" + nomeSquadra + "'?";
        if (partecipanti > 0) {
            message += "\nQuesta azione eliminerà anche tutti i " + partecipanti + " partecipanti!";
        }
        
        int conferma = JOptionPane.showConfirmDialog(this,
            message,
            "Conferma Eliminazione",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return legaDAO.eliminaLega(legaSelezionata.getIdLega(), adminUtente.getIdUtente());
                }
                
                @Override
                protected void done() {
                    try {
                        boolean successo = get();
                        
                        if (successo) {
                            JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                                "Lega '" + nomeSquadra + "' eliminata con successo!",
                                "Successo",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            loadData();
                            
                        } else {
                            JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                                "Errore durante l'eliminazione della lega.",
                                "Errore",
                                JOptionPane.ERROR_MESSAGE);
                        }
                        
                    } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                        JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                            "Errore: " + ex.getMessage(),
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void popolaLega(ActionEvent e) {
        Lega legaSelezionata = (Lega) comboLeghePopola.getSelectedItem();
        
        if (legaSelezionata == null) {
            JOptionPane.showMessageDialog(this,
                "Seleziona una lega da popolare!",
                "Attenzione",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String message = "Popolare la lega '" + legaSelezionata.getNome() + "'?\n\n";
        message += "Questo inserirà automaticamente:\n";
        message += "• 20 squadre di Serie A\n";
        message += "• ~300 calciatori con ruoli e costi\n";
        message += "• Fasce giocatori automatiche\n\n";
        message += "Procedere?";
        
        int conferma = JOptionPane.showConfirmDialog(this,
            message,
            "Conferma Popolamento Lega",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish("Popolamento lega in corso...");
                    
                    DataPopulator populator = new DataPopulator();
                    populator.popolaCompletamente();
                    
                    return null;
                }
                
                @Override
                protected void process(List<String> chunks) {
                    for (String msg : chunks) {
                        System.out.println(msg);
                    }
                }
                
                @Override
                protected void done() {
                    try {
                        get(); // Controlla eccezioni
                        
                        JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                            "Lega '" + legaSelezionata.getNome() + "' popolata con successo!\n" +
                            "Ora gli utenti possono creare le loro squadre fantacalcio.",
                            "Popolamento Completato",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Notifica il parent frame se necessario
                        if (parentFrame != null) {
                            parentFrame.notifySquadreChanged();
                        }
                        
                    } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                        JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                            "Errore durante il popolamento: " + ex.getMessage(),
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    // Getters per statistiche
    public int getNumLeghe() {
        return Integer.parseInt(lblNumLeghe.getText());
    }
    
    public int getPartecipantiTotali() {
        return Integer.parseInt(lblPartecipantiTotali.getText());
    }
}