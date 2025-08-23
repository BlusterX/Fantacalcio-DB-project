package fantacalcio.gui.user.dialog;

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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import fantacalcio.dao.FormazioneDAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;

/**
 * Dialog per gestire la formazione di una squadra
 */
public class FormationManagementDialog extends JDialog {
    
    private final SquadraFantacalcio squadra;
    private final Utente utente;
    
    // Componenti GUI
    private JComboBox<String> comboModulo;
    private JList<Calciatore> listDisponibili, listTitolari, listPanchina;
    private DefaultListModel<Calciatore> modelDisponibili, modelTitolari, modelPanchina;
    private JLabel lblGiornataCorrente, lblCountdown, lblStatoLega;
    private JButton btnSalvaFormazione, btnResetFormazione, btnChiudi;
    
    private final Map<Calciatore.Ruolo, List<Calciatore>> formazione;
    private final String[] moduli = {"3-4-3", "3-5-2", "4-3-3", "4-4-2", "4-5-1", "5-3-2"};
    private final int GIORNATA_CORRENTE = 1;
    
    public FormationManagementDialog(Window parent, SquadraFantacalcio squadra, Utente utente) {
        super(parent, "Gestione Formazione - " + squadra.getNomeSquadra(), ModalityType.APPLICATION_MODAL);
        this.squadra = squadra;
        this.utente = utente;
        this.formazione = new HashMap<>();
        
        initializeGUI();
        loadFormazione();
        setLocationRelativeTo(parent);
    }
    
    private void initializeGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1000, 700));
        
        createComponents();
        setupLayout();
        styleComponents();
        setupEventListeners();
        
        pack();
    }
    
    private void createComponents() {
        // Header info
        lblGiornataCorrente = new JLabel("Giornata " + GIORNATA_CORRENTE);
        lblCountdown = new JLabel("⏰ Prossima partita: 15:00");
        lblStatoLega = new JLabel("🟢 Lega Attiva");
        
        // Controlli formazione
        comboModulo = new JComboBox<>(moduli);
        comboModulo.setSelectedItem("4-3-3"); // Default
        
        // Liste giocatori
        modelDisponibili = new DefaultListModel<>();
        modelTitolari = new DefaultListModel<>();
        modelPanchina = new DefaultListModel<>();
        
        listDisponibili = new JList<>(modelDisponibili);
        listTitolari = new JList<>(modelTitolari);
        listPanchina = new JList<>(modelPanchina);
        
        listDisponibili.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTitolari.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPanchina.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Pulsanti
        btnSalvaFormazione = new JButton("💾 Salva Formazione");
        btnResetFormazione = new JButton("🔄 Reset");
        btnChiudi = new JButton("❌ Chiudi");
    }
    
    private void setupLayout() {
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Main content
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setLeftComponent(createFormationPanel());
        mainSplit.setRightComponent(createPlayerListsPanel());
        mainSplit.setDividerLocation(400);
        mainSplit.setResizeWeight(0.4);
        
        // Bottom panel
        JPanel bottomPanel = createBottomPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 150, 243));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Info lato sinistro
        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftInfo.setBackground(new Color(33, 150, 243));
        
        JLabel titleLabel = new JLabel("⚽ " + squadra.getNomeSquadra());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        leftInfo.add(titleLabel);
        leftInfo.add(Box.createHorizontalStrut(20));
        leftInfo.add(lblGiornataCorrente);
        
        // Info lato destro
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightInfo.setBackground(new Color(33, 150, 243));
        rightInfo.add(lblStatoLega);
        rightInfo.add(Box.createHorizontalStrut(15));
        rightInfo.add(lblCountdown);
        
        headerPanel.add(leftInfo, BorderLayout.WEST);
        headerPanel.add(rightInfo, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createFormationPanel() {
        JPanel formationPanel = new JPanel(new BorderLayout());
        formationPanel.setBorder(BorderFactory.createTitledBorder("⚽ Campo da Gioco"));
        
        // Controlli modulo
        JPanel moduloPanel = new JPanel(new FlowLayout());
        moduloPanel.add(new JLabel("Modulo:"));
        moduloPanel.add(comboModulo);
        
        // Campo visuale (da implementare)
        JPanel campoPanel = createCampoVisualePanel();
        
        formationPanel.add(moduloPanel, BorderLayout.NORTH);
        formationPanel.add(campoPanel, BorderLayout.CENTER);
        
        return formationPanel;
    }
    
    private JPanel createCampoVisualePanel() {
        JPanel campoPanel = new JPanel();
        campoPanel.setBackground(new Color(34, 139, 34)); // Verde campo
        campoPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        campoPanel.setPreferredSize(new Dimension(350, 500));
        
        JLabel lblCampo = new JLabel("Campo da Gioco", SwingConstants.CENTER);
        lblCampo.setForeground(Color.WHITE);
        lblCampo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        campoPanel.add(lblCampo);
        
        return campoPanel;
    }
    
    private JPanel createPlayerListsPanel() {
        JPanel listsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        
        // Lista disponibili
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 0.4;
        JPanel disponibiliPanel = createPlayerListPanel("👥 Giocatori Disponibili", listDisponibili);
        listsPanel.add(disponibiliPanel, gbc);
        
        // Pulsanti di controllo
        gbc.gridy = 1; gbc.weighty = 0.1;
        JPanel controlPanel = createControlButtonsPanel();
        listsPanel.add(controlPanel, gbc);
        
        // Lista titolari
        gbc.gridy = 2; gbc.weighty = 0.3;
        JPanel titolariPanel = createPlayerListPanel("⭐ Titolari (11)", listTitolari);
        listsPanel.add(titolariPanel, gbc);
        
        // Lista panchina
        gbc.gridy = 3; gbc.weighty = 0.2;
        JPanel panchinaPanel = createPlayerListPanel("🪑 Panchina (7)", listPanchina);
        listsPanel.add(panchinaPanel, gbc);
        
        return listsPanel;
    }
    
    private JPanel createPlayerListPanel(String title, JList<Calciatore> list) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(250, 150));
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createControlButtonsPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        JButton btnAggiungiTitolare = new JButton("→ Titolare");
        JButton btnRimuovi = new JButton("← Rimuovi");
        
        styleSmallButton(btnAggiungiTitolare, new Color(76, 175, 80));
        styleSmallButton(btnRimuovi, new Color(244, 67, 54));
        
        controlPanel.add(btnAggiungiTitolare);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(btnRimuovi);
        
        // Event listeners
        btnAggiungiTitolare.addActionListener(e -> aggiungiATitolari());
        btnRimuovi.addActionListener(e -> rimuoviGiocatore());
        
        return controlPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        bottomPanel.add(btnSalvaFormazione);
        bottomPanel.add(btnResetFormazione);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(btnChiudi);
        
        return bottomPanel;
    }
    
    private void styleComponents() {
        // Header labels
        Font headerFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        lblGiornataCorrente.setFont(headerFont);
        lblGiornataCorrente.setForeground(Color.WHITE);
        lblCountdown.setFont(headerFont);
        lblCountdown.setForeground(Color.YELLOW);
        lblStatoLega.setFont(headerFont);
        lblStatoLega.setForeground(Color.WHITE);
        
        // Buttons
        styleButton(btnSalvaFormazione, new Color(76, 175, 80));
        styleButton(btnResetFormazione, new Color(158, 158, 158));
        styleButton(btnChiudi, new Color(244, 67, 54));
        
        // Custom cell renderer per le liste
        CalciatoreListCellRenderer renderer = new CalciatoreListCellRenderer();
        listDisponibili.setCellRenderer(renderer);
        listTitolari.setCellRenderer(renderer);
        listPanchina.setCellRenderer(renderer);
    }
    
    private void setupEventListeners() {
        comboModulo.addActionListener(e -> aggiornaModulo());
        btnSalvaFormazione.addActionListener(this::salvaFormazione);
        btnResetFormazione.addActionListener(this::resetFormazione);
        btnChiudi.addActionListener(e -> dispose());
    }
    
    private void loadFormazione() {
        // Carica tutti i giocatori della squadra nella lista disponibili
        for (Calciatore calciatore : squadra.getCalciatori()) {
            modelDisponibili.addElement(calciatore);
        }
        
        aggiornaContatori();
    }
    
    private void aggiungiATitolari() {
        Calciatore selected = listDisponibili.getSelectedValue();
        if (selected == null) return;
        
        if (modelTitolari.getSize() >= 11) {
            JOptionPane.showMessageDialog(this, 
                "Hai già 11 titolari!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (verificaLimitiRuolo(selected.getRuolo(), true)) {
            modelDisponibili.removeElement(selected);
            modelTitolari.addElement(selected);
            aggiornaContatori();
        }
    }
    
    private void rimuoviGiocatore() {
        Calciatore selected = null;
        
        if (listTitolari.getSelectedValue() != null) {
            selected = listTitolari.getSelectedValue();
            modelTitolari.removeElement(selected);
        } else if (listPanchina.getSelectedValue() != null) {
            selected = listPanchina.getSelectedValue();
            modelPanchina.removeElement(selected);
        }
        
        if (selected != null) {
            modelDisponibili.addElement(selected);
            aggiornaContatori();
        }
    }
    
    private boolean verificaLimitiRuolo(Calciatore.Ruolo ruolo, boolean titolari) {
        DefaultListModel<Calciatore> model = titolari ? modelTitolari : modelPanchina;
        long count = 0;
        
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).getRuolo() == ruolo) {
                count++;
            }
        }
        
        // Limiti per modulo 4-3-3 (da adattare in base al modulo)
        int limite = switch (ruolo) {
            case PORTIERE -> titolari ? 1 : 2;
            case DIFENSORE -> titolari ? 4 : 4;
            case CENTROCAMPISTA -> titolari ? 3 : 3;
            case ATTACCANTE -> titolari ? 3 : 3;
        };
        
        if (count >= limite) {
            JOptionPane.showMessageDialog(this, 
                "Hai raggiunto il limite per " + ruolo.name().toLowerCase() + 
                (titolari ? " titolari" : " in panchina") + "!", 
                "Limite Raggiunto", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void aggiornaModulo() {
        String modulo = (String) comboModulo.getSelectedItem();
        System.out.println("Modulo cambiato a: " + modulo);
    }
    
    private void aggiornaContatori() {
    }

    private void salvaFormazione(ActionEvent e) {
        if (modelTitolari.getSize() != 11) {
            JOptionPane.showMessageDialog(this,
                "Devi selezionare esattamente 11 titolari!",
                "Formazione Incompleta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String modulo = (String) comboModulo.getSelectedItem();
        final int idSquadra = squadra.getIdSquadraFantacalcio();
        final int giornata = GIORNATA_CORRENTE;

        // prepara le liste di ID
        final var idsTitolari = idsFrom(modelTitolari);
        final var idsPanchina = idsFrom(modelPanchina);

        btnSalvaFormazione.setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                FormazioneDAO dao = new FormazioneDAO();
                return dao.salvaFormazioneCompleta(
                    idSquadra,
                    giornata,
                    modulo,
                    idsTitolari,
                    idsPanchina
                );
            }

            @Override
            protected void done() {
                btnSalvaFormazione.setEnabled(true);
                try {
                    boolean successo = get();
                    if (successo) {
                        JOptionPane.showMessageDialog(FormationManagementDialog.this,
                            "Formazione salvata con successo per la giornata " + giornata + "!",
                            "Successo", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(FormationManagementDialog.this,
                            "Errore durante il salvataggio della formazione.",
                            "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(FormationManagementDialog.this,
                        "Errore: " + ex.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private List<Integer> idsFrom(DefaultListModel<Calciatore> model) {
        List<Integer> ids = new ArrayList<>(model.getSize());
        IntStream.range(0, model.getSize())
                .mapToObj(model::getElementAt)
                .map(Calciatore::getIdCalciatore)
                .forEach(ids::add);
        return ids;
    }

    
    private void resetFormazione(ActionEvent e) {
        int conferma = JOptionPane.showConfirmDialog(this,
            "Sei sicuro di voler resettare la formazione?",
            "Conferma Reset",
            JOptionPane.YES_NO_OPTION);
        
        if (conferma == JOptionPane.YES_OPTION) {
            // Riporta tutti i giocatori nella lista disponibili
            modelDisponibili.clear();
            modelTitolari.clear();
            modelPanchina.clear();
            
            for (Calciatore calciatore : squadra.getCalciatori()) {
                modelDisponibili.addElement(calciatore);
            }
            aggiornaContatori();
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
    
    private void styleSmallButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(120, 25));
    }
    
    // Renderer personalizzato per le liste di calciatori
    private static class CalciatoreListCellRenderer extends javax.swing.DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Calciatore calciatore) {
                String ruoloIcon = switch (calciatore.getRuolo()) {
                    case PORTIERE -> "🥅";
                    case DIFENSORE -> "🛡️";
                    case CENTROCAMPISTA -> "⚽";
                    case ATTACCANTE -> "🎯";
                };
                
                setText(ruoloIcon + " " + calciatore.getNomeCompleto() + 
                       " (" + calciatore.getCosto() + "€)");
            }
            
            return this;
        }
    }
}