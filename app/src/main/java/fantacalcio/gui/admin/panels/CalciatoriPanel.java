package fantacalcio.gui.admin.panels;

import fantacalcio.dao.CalciatoreDAO;
import fantacalcio.dao.CalciatoreDAO.CalciatoreConSquadra;
import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraSerieA;
import fantacalcio.gui.MainFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class CalciatoriPanel extends JPanel {
        
    private final MainFrame parentFrame;
    private final SquadrePanel squadrePanel;
    private final CalciatoreDAO calciatoreDAO;
    
    // Componenti GUI
    private JTable tabellaCalciatori;
    private DefaultTableModel modelTabella;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Form components  
    private JTextField txtNome, txtCognome, txtCosto, txtFiltro;
    private JComboBox<Calciatore.Ruolo> comboRuolo, comboFiltroRuolo;
    private JComboBox<SquadraSerieA> comboSquadra, comboFiltroSquadra;
    private JButton btnAggiungi, btnElimina, btnPulisci, btnRefresh;
    
    // Stats components
    private JLabel lblTotalCalciatori, lblPortieri, lblDifensori, lblCentrocampisti, lblAttaccanti;
    
    public CalciatoriPanel(MainFrame parentFrame, SquadrePanel squadrePanel) {
        this.parentFrame = parentFrame;
        this.squadrePanel = squadrePanel;
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
        createFilters();
        createStats();
    }
    
    private void createTable() {
        String[] colonne = {"ID", "Nome", "Cognome", "Costo", "Ruolo", "Squadra"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaCalciatori = new JTable(modelTabella);
        tabellaCalciatori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaCalciatori.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                popolaCampiDaTabella();
            }
        });
        
        // Aggiungi sorting
        sorter = new TableRowSorter<>(modelTabella);
        tabellaCalciatori.setRowSorter(sorter);
        
        // Stile tabella
        tabellaCalciatori.setRowHeight(25);
        tabellaCalciatori.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaCalciatori.getTableHeader().setForeground(Color.WHITE);
        tabellaCalciatori.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Larghezza colonne
        tabellaCalciatori.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tabellaCalciatori.getColumnModel().getColumn(1).setPreferredWidth(100); // Nome
        tabellaCalciatori.getColumnModel().getColumn(2).setPreferredWidth(100); // Cognome
        tabellaCalciatori.getColumnModel().getColumn(3).setPreferredWidth(80);  // Ruolo
        tabellaCalciatori.getColumnModel().getColumn(4).setPreferredWidth(150); // Squadra
        tabellaCalciatori.getColumnModel().getColumn(5).setPreferredWidth(80);  // Costo
    }
    
    private void createForm() {
        txtNome = new JTextField(15);
        txtCognome = new JTextField(15);
        // AGGIUNTA: campo per il costo che mancava
        txtCosto = new JTextField(10);
        txtCosto.setToolTipText("Costo in crediti (lascia vuoto per il valore di default)");
        
        comboRuolo = new JComboBox<>(Calciatore.Ruolo.values());
        comboSquadra = new JComboBox<>();
    }
    
    private void createButtons() {
        btnAggiungi = new JButton("Aggiungi Calciatore");
        btnElimina = new JButton("Elimina");
        btnPulisci = new JButton("Pulisci Form");
        btnRefresh = new JButton("Aggiorna");
        
        // Stile pulsanti
        styleButton(btnAggiungi, new Color(76, 175, 80));
        styleButton(btnElimina, new Color(244, 67, 54));
        styleButton(btnPulisci, new Color(158, 158, 158));
        styleButton(btnRefresh, new Color(33, 150, 243));
        
        // Event listeners
        btnAggiungi.addActionListener(this::aggiungiCalciatore);
        btnElimina.addActionListener(this::eliminaCalciatore);
        btnPulisci.addActionListener(e -> pulisciForm());
        btnRefresh.addActionListener(e -> loadData());
    }
    
    private void createFilters() {
        txtFiltro = new JTextField(15);
        comboFiltroRuolo = new JComboBox<>();
        comboFiltroSquadra = new JComboBox<>();
        
        // Aggiungi opzioni "Tutti"
        comboFiltroRuolo.addItem(null); // "Tutti i ruoli"
        for (Calciatore.Ruolo ruolo : Calciatore.Ruolo.values()) {
            comboFiltroRuolo.addItem(ruolo);
        }
        
        comboFiltroSquadra.addItem(null); // "Tutte le squadre"
        
        // Event listeners per filtri
        txtFiltro.addActionListener(e -> applicaFiltri());
        comboFiltroRuolo.addActionListener(e -> applicaFiltri());
        comboFiltroSquadra.addActionListener(e -> applicaFiltri());
        
        // Tooltip
        txtFiltro.setToolTipText("Cerca per nome o cognome");
    }
    
    private void createStats() {
        lblTotalCalciatori = new JLabel("Totale: 0");
        lblPortieri = new JLabel("Portieri: 0");
        lblDifensori = new JLabel("Difensori: 0");
        lblCentrocampisti = new JLabel("Centrocampisti: 0");
        lblAttaccanti = new JLabel("Attaccanti: 0");
    }
    
    private void setupLayout() {
        // Pannello filtri
        JPanel filterPanel = createFilterPanel();
        
        // Pannello tabella
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JLabel("Lista Calciatori:"), BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(tabellaCalciatori);
        scrollPane.setPreferredSize(new Dimension(800, 350));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Pannello form
        JPanel formPanel = createFormPanel();
        
        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnAggiungi);
        buttonPanel.add(btnElimina);
        buttonPanel.add(btnPulisci);
        buttonPanel.add(btnRefresh);
        
        // Pannello statistiche
        JPanel statsPanel = createStatsPanel();
        
        // Pannello inferiore
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JLabel("Inserimento Calciatore:"), BorderLayout.NORTH);
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Layout finale
        add(filterPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(statsPanel, BorderLayout.EAST);
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtri"));
        
        filterPanel.add(new JLabel("Cerca:"));
        filterPanel.add(txtFiltro);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(new JLabel("Ruolo:"));
        filterPanel.add(comboFiltroRuolo);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(new JLabel("Squadra:"));
        filterPanel.add(comboFiltroSquadra);
        
        return filterPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Prima riga
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNome, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Cognome:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtCognome, gbc);
        
        // Seconda riga
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Ruolo:"), gbc);
        gbc.gridx = 1;
        formPanel.add(comboRuolo, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Squadra:"), gbc);
        gbc.gridx = 3;
        formPanel.add(comboSquadra, gbc);
        
        // Terza riga - AGGIUNTA
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Costo:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtCosto, gbc);
        
        return formPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistiche"));
        statsPanel.setPreferredSize(new Dimension(150, 0));
        
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(lblTotalCalciatori);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(new JSeparator());
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(lblPortieri);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(lblDifensori);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(lblCentrocampisti);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(lblAttaccanti);
        statsPanel.add(Box.createVerticalGlue());
        
        return statsPanel;
    }
    
    public void loadData() {
        modelTabella.setRowCount(0);
        
        List<CalciatoreConSquadra> calciatori = calciatoreDAO.trovaTuttiICalciatoriConSquadra();
        
        for (CalciatoreConSquadra calciatoreConSquadra : calciatori) {
            Calciatore calciatore = calciatoreConSquadra.getCalciatore();
            String nomeSquadra = calciatoreConSquadra.getNomeSquadra();
            
            Object[] riga = {
                calciatore.getIdCalciatore(),
                calciatore.getNome(),
                calciatore.getCognome(),
                calciatore.getRuolo().getAbbreviazione(),
                nomeSquadra,
                calciatore.getCosto() + " €"
            };
            modelTabella.addRow(riga);
        }
        
        updateStats();
    }
    
    public void refreshSquadreCombo() {
        // Aggiorna le combo box delle squadre
        refreshComboBox(comboSquadra);
        refreshComboBox(comboFiltroSquadra);
    }
    
    private void refreshComboBox(JComboBox<SquadraSerieA> combo) {
        combo.removeAllItems();
        if (combo == comboFiltroSquadra) {
            combo.addItem(null); // "Tutte le squadre"
        }
        
        List<SquadraSerieA> squadre = squadrePanel.getAllSquadre();
        for (SquadraSerieA squadra : squadre) {
            combo.addItem(squadra);
        }
    }
    
    private void updateStats() {
        Map<Calciatore.Ruolo, Integer> conteggi = calciatoreDAO.contaCalciatoriPerRuolo();
        
        int totale = conteggi.values().stream().mapToInt(Integer::intValue).sum();
        
        lblTotalCalciatori.setText("Totale: " + totale);
        lblPortieri.setText("Portieri: " + conteggi.getOrDefault(Calciatore.Ruolo.PORTIERE, 0));
        lblDifensori.setText("Difensori: " + conteggi.getOrDefault(Calciatore.Ruolo.DIFENSORE, 0));
        lblCentrocampisti.setText("Centrocampisti: " + conteggi.getOrDefault(Calciatore.Ruolo.CENTROCAMPISTA, 0));
        lblAttaccanti.setText("Attaccanti: " + conteggi.getOrDefault(Calciatore.Ruolo.ATTACCANTE, 0));
    }
    
    private void applicaFiltri() {
        RowFilter<DefaultTableModel, Object> filter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Filtro testo (nome o cognome)
                String searchText = txtFiltro.getText().trim().toLowerCase();
                if (!searchText.isEmpty()) {
                    String nome = entry.getStringValue(1).toLowerCase();
                    String cognome = entry.getStringValue(2).toLowerCase();
                    
                    if (!nome.contains(searchText) && !cognome.contains(searchText)) {
                        return false;
                    }
                }
                
                // Filtro ruolo
                Calciatore.Ruolo ruoloSelezionato = (Calciatore.Ruolo) comboFiltroRuolo.getSelectedItem();
                if (ruoloSelezionato != null) {
                    String ruoloTabella = entry.getStringValue(3);
                    if (!ruoloSelezionato.getAbbreviazione().equals(ruoloTabella)) {
                        return false;
                    }
                }
                
                // Filtro squadra
                SquadraSerieA squadraSelezionata = (SquadraSerieA) comboFiltroSquadra.getSelectedItem();
                if (squadraSelezionata != null) {
                    String squadraTabella = entry.getStringValue(4);
                    if (!squadraSelezionata.getNome().equals(squadraTabella)) {
                        return false;
                    }
                }
                
                return true;
            }
        };
        
        sorter.setRowFilter(filter);
    }
    
    private void popolaCampiDaTabella() {
        int rigaSelezionata = tabellaCalciatori.getSelectedRow();
        if (rigaSelezionata >= 0) {
            // Converte l'indice della vista all'indice del modello (per il sorting)
            int modelRow = tabellaCalciatori.convertRowIndexToModel(rigaSelezionata);
            
            txtNome.setText((String) modelTabella.getValueAt(modelRow, 1));
            txtCognome.setText((String) modelTabella.getValueAt(modelRow, 2));
            
            String ruoloAbbr = (String) modelTabella.getValueAt(modelRow, 3);
            for (Calciatore.Ruolo ruolo : Calciatore.Ruolo.values()) {
                if (ruolo.getAbbreviazione().equals(ruoloAbbr)) {
                    comboRuolo.setSelectedItem(ruolo);
                    break;
                }
            }
            
            String nomeSquadra = (String) modelTabella.getValueAt(modelRow, 4);
            for (int i = 0; i < comboSquadra.getItemCount(); i++) {
                SquadraSerieA squadra = comboSquadra.getItemAt(i);
                if (squadra != null && squadra.getNome().equals(nomeSquadra)) {
                    comboSquadra.setSelectedItem(squadra);
                    break;
                }
            }
        }
    }
    
    private void aggiungiCalciatore(ActionEvent e) {
        try {
            String nome = txtNome.getText().trim();
            String cognome = txtCognome.getText().trim();
            Calciatore.Ruolo ruolo = (Calciatore.Ruolo) comboRuolo.getSelectedItem();
            SquadraSerieA squadra = (SquadraSerieA) comboSquadra.getSelectedItem();
            
            // Validazioni
            if (nome.isEmpty() || cognome.isEmpty()) {
                throw new IllegalArgumentException("Nome e cognome sono obbligatori!");
            }
            
            if (squadra == null) {
                throw new IllegalArgumentException("Seleziona una squadra!");
            }
            
            // Crea calciatore con costo di default per ruolo
            Calciatore nuovoCalciatore = new Calciatore(nome, cognome, ruolo);
            
            if (calciatoreDAO.inserisciCalciatore(nuovoCalciatore, squadra.getIdSquadraA())) {
                JOptionPane.showMessageDialog(this, 
                    "Calciatore '" + nuovoCalciatore.getNomeCompleto() + "' aggiunto con successo!\nCosto: " + nuovoCalciatore.getCosto() + " crediti", 
                    "Successo", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
                pulisciForm();
                txtNome.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Errore durante l'inserimento del calciatore.", 
                    "Errore", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Errore: " + ex.getMessage(), 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminaCalciatore(ActionEvent e) {
        int rigaSelezionata = tabellaCalciatori.getSelectedRow();
        if (rigaSelezionata < 0) {
            JOptionPane.showMessageDialog(this, 
                "Seleziona un calciatore dalla tabella per eliminarlo.", 
                "Attenzione", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Converte l'indice della vista all'indice del modello
        int modelRow = tabellaCalciatori.convertRowIndexToModel(rigaSelezionata);
        
        String nomeCompleto = modelTabella.getValueAt(modelRow, 1) + " " + modelTabella.getValueAt(modelRow, 2);
        String squadra = (String) modelTabella.getValueAt(modelRow, 5);
        
        int conferma = JOptionPane.showConfirmDialog(this, 
            "Sei sicuro di voler eliminare il calciatore:\n" + 
            nomeCompleto + " (" + squadra + ")?", 
            "Conferma Eliminazione", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            int idCalciatore = (Integer) modelTabella.getValueAt(modelRow, 0);
            
            if (calciatoreDAO.eliminaCalciatore(idCalciatore)) {
                JOptionPane.showMessageDialog(this, 
                    "Calciatore '" + nomeCompleto + "' eliminato con successo!", 
                    "Successo", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
                pulisciForm();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Errore durante l'eliminazione del calciatore.", 
                    "Errore", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void pulisciForm() {
        txtNome.setText("");
        txtCognome.setText("");
        comboRuolo.setSelectedIndex(0);
        if (comboSquadra.getItemCount() > 0) {
            comboSquadra.setSelectedIndex(0);
        }
        tabellaCalciatori.clearSelection();
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    public int getNumCalciatori() {
        Map<Calciatore.Ruolo, Integer> conteggi = calciatoreDAO.contaCalciatoriPerRuolo();
        return conteggi.values().stream().mapToInt(Integer::intValue).sum();
    }
}