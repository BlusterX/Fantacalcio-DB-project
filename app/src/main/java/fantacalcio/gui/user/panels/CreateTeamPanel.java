package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import fantacalcio.dao.CalciatoreDAO;
import fantacalcio.dao.CalciatoreDAO.CalciatoreConSquadra;
import fantacalcio.dao.SquadraFantacalcioDAO;
import fantacalcio.gui.user.UserMainFrame;
import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;

/**
 * Panel per la creazione di nuove squadre fantacalcio
 */
public class CreateTeamPanel extends JPanel {
    
    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final SquadraFantacalcioDAO squadraDAO;
    private final CalciatoreDAO calciatoreDAO;
    
    // Componenti GUI principali
    private JTextField txtNomeSquadra;
    private JButton btnCreaSquadra, btnAnnulla;
    
    // Tabella calciatori disponibili
    private JTable tabellaCalciatori;
    private DefaultTableModel modelTabella;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Filtri
    private JTextField txtFiltroNome;
    private JComboBox<Calciatore.Ruolo> comboFiltroRuolo;
    private JComboBox<String> comboFiltroSquadra;
    private JSlider sliderCosto;
    
    // Panel squadra in costruzione
    private JPanel squadraPanel;
    private JLabel lblBudgetRimanente, lblGiocatoriTotali;
    private JLabel lblPortieri, lblDifensori, lblCentrocampisti, lblAttaccanti;
    
    // Squadra corrente in costruzione
    private SquadraFantacalcio squadraInCostruzione;
    
    // Cache dei calciatori per evitare ricaricamenti
    private List<CalciatoreConSquadra> calciatori;
    
    public CreateTeamPanel(UserMainFrame parentFrame, Utente utente) {
        this.parentFrame = parentFrame;
        this.utenteCorrente = utente;
        this.squadraDAO = new SquadraFantacalcioDAO();
        this.calciatoreDAO = new CalciatoreDAO();
        
        initializeComponents();
        setupLayout();
        refreshData();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        createSquadraCreationPanel();
        createCalciatoriTable();
        createFiltersPanel();
        createSquadraInCostruzionePanel();
        
        // Inizializza squadra vuota
        resetSquadra();
    }
    
    private void createSquadraCreationPanel() {
        txtNomeSquadra = new JTextField(25);
        txtNomeSquadra.setToolTipText("Inserisci il nome della tua squadra");
        
        btnCreaSquadra = new JButton("🏆 Crea Squadra");
        btnAnnulla = new JButton("❌ Annulla");
        
        // Stile pulsanti
        btnCreaSquadra.setBackground(new Color(76, 175, 80));
        btnCreaSquadra.setForeground(Color.WHITE);
        btnCreaSquadra.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnCreaSquadra.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnCreaSquadra.setFocusPainted(false);
        btnCreaSquadra.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnAnnulla.setBackground(new Color(158, 158, 158));
        btnAnnulla.setForeground(Color.WHITE);
        btnAnnulla.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnAnnulla.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnAnnulla.setFocusPainted(false);
        btnAnnulla.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Event listeners
        btnCreaSquadra.addActionListener(this::creaSquadra);
        btnAnnulla.addActionListener(this::annullaCreazione);
    }
    
    private void createCalciatoriTable() {
        String[] colonne = {"Nome", "Cognome", "Ruolo", "Squadra", "Costo", "Azione"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Solo la colonna "Azione" è editabile
            }
        };
        
        tabellaCalciatori = new JTable(modelTabella);
        tabellaCalciatori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaCalciatori.setRowHeight(30);
        
        // Aggiungi sorting
        sorter = new TableRowSorter<>(modelTabella);
        tabellaCalciatori.setRowSorter(sorter);
        
        // Stile tabella
        tabellaCalciatori.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaCalciatori.getTableHeader().setForeground(Color.WHITE);
        tabellaCalciatori.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Renderer personalizzato per la colonna azione
        tabellaCalciatori.getColumn("Azione").setCellRenderer(new ButtonRenderer());
        tabellaCalciatori.getColumn("Azione").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Larghezza colonne
        tabellaCalciatori.getColumnModel().getColumn(0).setPreferredWidth(100); // Nome
        tabellaCalciatori.getColumnModel().getColumn(1).setPreferredWidth(100); // Cognome
        tabellaCalciatori.getColumnModel().getColumn(2).setPreferredWidth(60);  // Ruolo
        tabellaCalciatori.getColumnModel().getColumn(3).setPreferredWidth(120); // Squadra
        tabellaCalciatori.getColumnModel().getColumn(4).setPreferredWidth(70);  // Costo
        tabellaCalciatori.getColumnModel().getColumn(5).setPreferredWidth(100); // Azione
    }
    
    private void createFiltersPanel() {
        txtFiltroNome = new JTextField(15);
        txtFiltroNome.setToolTipText("Cerca per nome o cognome");
        
        comboFiltroRuolo = new JComboBox<>();
        comboFiltroRuolo.addItem(null); // "Tutti i ruoli"
        for (Calciatore.Ruolo ruolo : Calciatore.Ruolo.values()) {
            comboFiltroRuolo.addItem(ruolo);
        }
        
        comboFiltroSquadra = new JComboBox<>();
        comboFiltroSquadra.addItem("Tutte le squadre");
        
        // Slider per il costo
        sliderCosto = new JSlider(0, 100, 100);
        sliderCosto.setMajorTickSpacing(25);
        sliderCosto.setMinorTickSpacing(5);
        sliderCosto.setPaintTicks(true);
        sliderCosto.setPaintLabels(true);
        sliderCosto.setToolTipText("Filtra per costo massimo");
        
        // Event listeners per filtri
        txtFiltroNome.addActionListener(e -> applicaFiltri());
        comboFiltroRuolo.addActionListener(e -> applicaFiltri());
        comboFiltroSquadra.addActionListener(e -> applicaFiltri());
        sliderCosto.addChangeListener(e -> applicaFiltri());
    }
    
    private void createSquadraInCostruzionePanel() {
        lblBudgetRimanente = new JLabel("1000 €");
        lblGiocatoriTotali = new JLabel("0/25");
        lblPortieri = new JLabel("0/3");
        lblDifensori = new JLabel("0/8");
        lblCentrocampisti = new JLabel("0/8");
        lblAttaccanti = new JLabel("0/6");
        
        // Stile per le statistiche
        Font statsFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        lblBudgetRimanente.setFont(statsFont);
        lblGiocatoriTotali.setFont(statsFont);
    }
    
    private void setupLayout() {
        // Pannello superiore - creazione squadra
        JPanel topPanel = createTopPanel();
        
        // Pannello centrale - split tra tabella calciatori e squadra in costruzione
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createCalciatoriPanel());
        splitPane.setRightComponent(createSquadraStatusPanel());
        splitPane.setDividerLocation(800);
        splitPane.setResizeWeight(0.7);
        
        // Layout finale
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Titolo e istruzioni
        JPanel titlePanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("⚽ Crea la tua Squadra Fantacalcio");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 150, 243));
        
        JLabel instructionLabel = new JLabel("Scegli 25 giocatori (3P + 8D + 8C + 6A) con un budget di 1000 crediti");
        instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        instructionLabel.setForeground(new Color(100, 100, 100));
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(instructionLabel, BorderLayout.SOUTH);
        
        // Form per nome squadra
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formPanel.add(new JLabel("Nome Squadra:"));
        formPanel.add(txtNomeSquadra);
        formPanel.add(Box.createHorizontalStrut(20));
        formPanel.add(btnCreaSquadra);
        formPanel.add(btnAnnulla);
        
        topPanel.add(titlePanel, BorderLayout.CENTER);
        topPanel.add(formPanel, BorderLayout.SOUTH);
        
        return topPanel;
    }
    
    private JPanel createCalciatoriPanel() {
        JPanel calciatoriPanel = new JPanel(new BorderLayout());
        calciatoriPanel.setBorder(BorderFactory.createTitledBorder("Calciatori Disponibili"));
        
        // Pannello filtri
        JPanel filtersPanel = createFiltersLayout();
        
        // Tabella
        JScrollPane scrollPane = new JScrollPane(tabellaCalciatori);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        
        calciatoriPanel.add(filtersPanel, BorderLayout.NORTH);
        calciatoriPanel.add(scrollPane, BorderLayout.CENTER);
        
        return calciatoriPanel;
    }
    
    private JPanel createFiltersLayout() {
        JPanel filtersPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Prima riga
        gbc.gridx = 0; gbc.gridy = 0;
        filtersPanel.add(new JLabel("Cerca:"), gbc);
        gbc.gridx = 1;
        filtersPanel.add(txtFiltroNome, gbc);
        gbc.gridx = 2;
        filtersPanel.add(new JLabel("Ruolo:"), gbc);
        gbc.gridx = 3;
        filtersPanel.add(comboFiltroRuolo, gbc);
        
        // Seconda riga
        gbc.gridx = 0; gbc.gridy = 1;
        filtersPanel.add(new JLabel("Squadra:"), gbc);
        gbc.gridx = 1;
        filtersPanel.add(comboFiltroSquadra, gbc);
        gbc.gridx = 2;
        filtersPanel.add(new JLabel("Costo max:"), gbc);
        gbc.gridx = 3;
        filtersPanel.add(sliderCosto, gbc);
        
        return filtersPanel;
    }
    
    private JPanel createSquadraStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("La Tua Squadra"));
        statusPanel.setPreferredSize(new Dimension(350, 0));
        
        // Statistiche generali
        JPanel generalStatsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        generalStatsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        generalStatsPanel.add(createStatLabel("💰 Budget Rimanente:", lblBudgetRimanente, new Color(76, 175, 80)));
        generalStatsPanel.add(createStatLabel("👥 Giocatori:", lblGiocatoriTotali, new Color(33, 150, 243)));
        
        // Statistiche per ruolo
        JPanel roleStatsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        roleStatsPanel.setBorder(BorderFactory.createTitledBorder("Composizione"));
        
        roleStatsPanel.add(createStatLabel("🥅 Portieri:", lblPortieri, new Color(255, 152, 0)));
        roleStatsPanel.add(createStatLabel("🛡️ Difensori:", lblDifensori, new Color(156, 39, 176)));
        roleStatsPanel.add(createStatLabel("⚽ Centrocampisti:", lblCentrocampisti, new Color(76, 175, 80)));
        roleStatsPanel.add(createStatLabel("🎯 Attaccanti:", lblAttaccanti, new Color(244, 67, 54)));
        
        // Lista giocatori selezionati
        squadraPanel = new JPanel();
        squadraPanel.setLayout(new BoxLayout(squadraPanel, BoxLayout.Y_AXIS));
        JScrollPane squadraScrollPane = new JScrollPane(squadraPanel);
        squadraScrollPane.setBorder(BorderFactory.createTitledBorder("Giocatori Selezionati"));
        squadraScrollPane.setPreferredSize(new Dimension(300, 200));
        
        statusPanel.add(generalStatsPanel, BorderLayout.NORTH);
        statusPanel.add(roleStatsPanel, BorderLayout.CENTER);
        statusPanel.add(squadraScrollPane, BorderLayout.SOUTH);
        
        return statusPanel;
    }
    
    private JPanel createStatLabel(String titolo, JLabel valore, Color colore) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titoloLabel = new JLabel(titolo);
        titoloLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titoloLabel.setForeground(colore);
        
        valore.setForeground(colore);
        
        panel.add(titoloLabel, BorderLayout.WEST);
        panel.add(valore, BorderLayout.EAST);
        
        return panel;
    }
    
    public void refreshData() {
        SwingWorker<List<CalciatoreConSquadra>, Void> worker = new SwingWorker<List<CalciatoreConSquadra>, Void>() {
            @Override
            protected List<CalciatoreConSquadra> doInBackground() throws Exception {
                return calciatoreDAO.trovaTuttiICalciatoriConSquadra();
            }
            
            @Override
            protected void done() {
                try {
                    calciatori = get(); // Salva nella cache
                    updateTable(calciatori);
                    updateSquadreCombo(calciatori);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CreateTeamPanel.this,
                        "Errore nel caricamento dei calciatori: " + e.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateTable(List<CalciatoreConSquadra> calciatori) {
        modelTabella.setRowCount(0);
        
        for (CalciatoreConSquadra calciatoreConSquadra : calciatori) {
            Calciatore calciatore = calciatoreConSquadra.getCalciatore();
            String nomeSquadra = calciatoreConSquadra.getNomeSquadra();
            
            Object[] riga = {
                calciatore.getNome(),
                calciatore.getCognome(),
                calciatore.getRuolo().getAbbreviazione(),
                nomeSquadra,
                calciatore.getCosto() + " €",
                "Aggiungi"
            };
            modelTabella.addRow(riga);
        }
    }
    
    private void updateSquadreCombo(List<CalciatoreConSquadra> calciatori) {
        comboFiltroSquadra.removeAllItems();
        comboFiltroSquadra.addItem("Tutte le squadre");
        
        calciatori.stream()
                  .map(CalciatoreConSquadra::getNomeSquadra)
                  .distinct()
                  .sorted()
                  .forEach(comboFiltroSquadra::addItem);
    }
    
    private void applicaFiltri() {
        RowFilter<DefaultTableModel, Object> filter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Filtro per nome
                String searchText = txtFiltroNome.getText().trim().toLowerCase();
                if (!searchText.isEmpty()) {
                    String nome = entry.getStringValue(0).toLowerCase();
                    String cognome = entry.getStringValue(1).toLowerCase();
                    if (!nome.contains(searchText) && !cognome.contains(searchText)) {
                        return false;
                    }
                }
                
                // Filtro per ruolo
                Calciatore.Ruolo ruoloSelezionato = (Calciatore.Ruolo) comboFiltroRuolo.getSelectedItem();
                if (ruoloSelezionato != null) {
                    String ruoloTabella = entry.getStringValue(2);
                    if (!ruoloSelezionato.getAbbreviazione().equals(ruoloTabella)) {
                        return false;
                    }
                }
                
                // Filtro per squadra
                String squadraSelezionata = (String) comboFiltroSquadra.getSelectedItem();
                if (squadraSelezionata != null && !squadraSelezionata.equals("Tutte le squadre")) {
                    String squadraTabella = entry.getStringValue(3);
                    if (!squadraSelezionata.equals(squadraTabella)) {
                        return false;
                    }
                }
                
                // Filtro per costo
                String costoStr = entry.getStringValue(4).replace(" €", "");
                try {
                    int costo = Integer.parseInt(costoStr);
                    if (costo > sliderCosto.getValue()) {
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    // Ignora errori di parsing
                }
                
                return true;
            }
        };
        
        sorter.setRowFilter(filter);
    }
    
    private void creaSquadra(ActionEvent e) {
        String nomeSquadra = txtNomeSquadra.getText().trim();
        
        if (nomeSquadra.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Inserisci il nome della squadra!",
                "Attenzione",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!squadraInCostruzione.isCompletata()) {
            JOptionPane.showMessageDialog(this,
                "La squadra non è ancora completa!\nDevi selezionare esattamente 25 giocatori.",
                "Squadra Incompleta",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Verifica nome duplicato
        if (squadraDAO.nomeSquadraEsiste(nomeSquadra, utenteCorrente.getIdUtente())) {
            JOptionPane.showMessageDialog(this,
                "Hai già una squadra con questo nome!",
                "Nome Duplicato",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Crea la squadra
        squadraInCostruzione.setNomeSquadra(nomeSquadra);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Crea la squadra
                if (!squadraDAO.creaSquadra(squadraInCostruzione)) {
                    return false;
                }
                
                // Aggiungi tutti i calciatori
                for (Calciatore calciatore : squadraInCostruzione.getCalciatori()) {
                    if (!squadraDAO.aggiungiCalciatoreAllaSquadra(
                            squadraInCostruzione.getIdSquadraFantacalcio(),
                            calciatore.getIdCalciatore())) {
                        return false;
                    }
                }
                
                return true;
            }
            
            @Override
            protected void done() {
                try {
                    boolean successo = get();
                    
                    if (successo) {
                        JOptionPane.showMessageDialog(CreateTeamPanel.this,
                            "Squadra '" + nomeSquadra + "' creata con successo!",
                            "Successo",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reset e notifica parent
                        resetSquadra();
                        txtNomeSquadra.setText("");
                        parentFrame.notifySquadraCreata();
                        
                    } else {
                        JOptionPane.showMessageDialog(CreateTeamPanel.this,
                            "Errore durante la creazione della squadra.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CreateTeamPanel.this,
                        "Errore: " + ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void annullaCreazione(ActionEvent e) {
        int conferma = JOptionPane.showConfirmDialog(this,
            "Sei sicuro di voler annullare?\nPerderai tutti i giocatori selezionati.",
            "Conferma Annullamento",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            resetSquadra();
            txtNomeSquadra.setText("");
        }
    }
    
    private void resetSquadra() {
        squadraInCostruzione = new SquadraFantacalcio("", utenteCorrente.getIdUtente());
        updateSquadraStatus();
    }
    
    private void aggiungiCalciatore(int row) {
        // Converti l'indice della vista all'indice del modello
        int modelRow = tabellaCalciatori.convertRowIndexToModel(row);
        
        String nome = (String) modelTabella.getValueAt(modelRow, 0);
        String cognome = (String) modelTabella.getValueAt(modelRow, 1);
        String ruoloStr = (String) modelTabella.getValueAt(modelRow, 2);
        String costoStr = (String) modelTabella.getValueAt(modelRow, 4);
        
        // Trova il calciatore completo dalla cache
        Calciatore calciatore = null;
        if (calciatori != null) {
            // Trova il calciatore corrispondente nella cache
            for (CalciatoreConSquadra calciatoreConSquadra : calciatori) {
                Calciatore c = calciatoreConSquadra.getCalciatore();
                if (c.getNome().equals(nome) && c.getCognome().equals(cognome) && 
                    c.getRuolo().getAbbreviazione().equals(ruoloStr)) {
                    calciatore = c;
                    break;
                }
            }
        }
        
        if (calciatore == null) {
            JOptionPane.showMessageDialog(this, "Errore: Calciatore non trovato!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verifica se può essere aggiunto
        if (!squadraInCostruzione.puoAcquistare(calciatore)) {
            String messaggio = "Non puoi aggiungere questo calciatore:\n";
            if (calciatore.getCosto() > squadraInCostruzione.getBudgetRimanente()) {
                messaggio += "• Budget insufficiente";
            } else {
                messaggio += "• Hai già il numero massimo di giocatori per questo ruolo";
            }
            
            JOptionPane.showMessageDialog(this, messaggio, "Impossibile Aggiungere", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Aggiungi alla squadra
        if (squadraInCostruzione.aggiungiCalciatore(calciatore)) {
            updateSquadraStatus();
            updateSquadraPanel();
        }
    }
    
    private void updateSquadraStatus() {
        lblBudgetRimanente.setText(squadraInCostruzione.getBudgetRimanente() + " €");
        lblGiocatoriTotali.setText(squadraInCostruzione.getNumeroTotaleGiocatori() + "/25");
        lblPortieri.setText(squadraInCostruzione.contaGiocatoriPerRuolo(Calciatore.Ruolo.PORTIERE) + "/3");
        lblDifensori.setText(squadraInCostruzione.contaGiocatoriPerRuolo(Calciatore.Ruolo.DIFENSORE) + "/8");
        lblCentrocampisti.setText(squadraInCostruzione.contaGiocatoriPerRuolo(Calciatore.Ruolo.CENTROCAMPISTA) + "/8");
        lblAttaccanti.setText(squadraInCostruzione.contaGiocatoriPerRuolo(Calciatore.Ruolo.ATTACCANTE) + "/6");
        
        // Colora in base al completamento
        if (squadraInCostruzione.isCompletata()) {
            lblGiocatoriTotali.setForeground(new Color(76, 175, 80));
            btnCreaSquadra.setEnabled(true);
        } else {
            lblGiocatoriTotali.setForeground(new Color(33, 150, 243));
            btnCreaSquadra.setEnabled(false);
        }
    }
    
    private void updateSquadraPanel() {
        squadraPanel.removeAll();
        
        for (Calciatore calciatore : squadraInCostruzione.getCalciatori()) {
            JPanel giocatorePanel = new JPanel(new BorderLayout());
            giocatorePanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            giocatorePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            
            String testo = calciatore.getRuolo().getAbbreviazione() + " " + 
                          calciatore.getNomeCompleto() + " (" + calciatore.getCosto() + "€)";
            JLabel lblGiocatore = new JLabel(testo);
            lblGiocatore.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            
            JButton btnRimuovi = new JButton("✖");
            btnRimuovi.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 8));
            btnRimuovi.setPreferredSize(new Dimension(20, 20));
            btnRimuovi.setBackground(new Color(244, 67, 54));
            btnRimuovi.setForeground(Color.WHITE);
            btnRimuovi.setBorder(null);
            btnRimuovi.setFocusPainted(false);
            btnRimuovi.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRimuovi.addActionListener(e -> rimuoviCalciatore(calciatore.getIdCalciatore()));
            
            giocatorePanel.add(lblGiocatore, BorderLayout.CENTER);
            giocatorePanel.add(btnRimuovi, BorderLayout.EAST);
            
            squadraPanel.add(giocatorePanel);
        }
        
        squadraPanel.revalidate();
        squadraPanel.repaint();
    }
    
    private void rimuoviCalciatore(int idCalciatore) {
        if (squadraInCostruzione.rimuoviCalciatore(idCalciatore)) {
            updateSquadraStatus();
            updateSquadraPanel();
        }
    }
    
    // Renderer e Editor per i pulsanti nella tabella
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Aggiungi" : value.toString());
            setBackground(new Color(76, 175, 80));
            setForeground(Color.WHITE);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            return this;
        }
    }
    
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "Aggiungi" : value.toString();
            button.setText(label);
            button.setBackground(new Color(76, 175, 80));
            button.setForeground(Color.WHITE);
            button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            isPushed = true;
            currentRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                aggiungiCalciatore(currentRow);
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}