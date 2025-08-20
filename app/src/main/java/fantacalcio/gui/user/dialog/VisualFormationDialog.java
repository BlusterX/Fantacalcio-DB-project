package fantacalcio.gui.user.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;
import fantacalcio.util.DatabaseConnection;

/**
 * Dialog per la creazione visuale della formazione
 */
public class VisualFormationDialog extends JDialog {
    
    private final SquadraFantacalcio squadra;
    private final Utente utente;
    private final String modulo;
    private final int giornataCorrente;
    
    // Componenti GUI
    private JPanel campoPanel;
    private JTable tabellaGiocatori;
    private DefaultTableModel modelTabella;
    private JButton btnSalva, btnReset;
    
    // Logica formazione
    private final Map<String, Calciatore> posizioni; // "P1", "D1", "D2", etc. -> Calciatore
    private final Map<String, JButton> bottoniPosizioni; // Riferimenti ai bottoni sul campo
    private final int[] moduloNumeri; // [difensori, centrocampisti, attaccanti]
    
    public VisualFormationDialog(Window parent, SquadraFantacalcio squadra, Utente utente, String modulo) {
        super(parent, "Formazione " + modulo + " - " + squadra.getNomeSquadra(), ModalityType.APPLICATION_MODAL);
        this.squadra = squadra;
        this.utente = utente;
        this.modulo = modulo;
        this.giornataCorrente = 1; // TODO: Recuperare giornata corrente
        this.posizioni = new HashMap<>();
        this.bottoniPosizioni = new HashMap<>();
        this.moduloNumeri = parseModulo(modulo);
        
        initializeGUI();
        loadGiocatori();
        setLocationRelativeTo(parent);
    }
    
    private void initializeGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1000, 700));
        
        createHeaderPanel();
        createMainContent();
        createBottomPanel();
        
        pack();
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(34, 139, 34));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("⚽ Formazione " + modulo, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel infoLabel = new JLabel("Clicca sui + per aggiungere i giocatori", SwingConstants.CENTER);
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        infoLabel.setForeground(new Color(200, 255, 200));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(infoLabel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Pannello campo (sinistra)
        campoPanel = createCampoPanel();
        splitPane.setLeftComponent(campoPanel);
        
        // Pannello giocatori (destra)
        JPanel giocatoriPanel = createGiocatoriPanel();
        splitPane.setRightComponent(giocatoriPanel);
        
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createCampoPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCampo(g);
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(34, 139, 34));
        panel.setPreferredSize(new Dimension(550, 500));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2), 
            "Campo da Gioco", 
            0, 0, 
            new Font(Font.SANS_SERIF, Font.BOLD, 14), 
            Color.WHITE
        ));
        
        createPosizioni(panel);
        
        return panel;
    }
    
    private void drawCampo(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.WHITE);
        
        // Bordi campo
        g2d.drawRect(50, 50, 450, 380);
        
        // Linea di metà campo
        g2d.drawLine(50, 240, 500, 240);
        
        // Cerchio di centrocampo
        g2d.drawOval(225, 190, 100, 100);
        
        // Aree di rigore
        g2d.drawRect(50, 140, 80, 200);  // Area sinistra
        g2d.drawRect(420, 140, 80, 200); // Area destra
        
        // Porte
        g2d.drawRect(35, 190, 15, 100);  // Porta sinistra
        g2d.drawRect(500, 190, 15, 100); // Porta destra
    }
    
    private void createPosizioni(JPanel campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Portiere
        gbc.gridx = 0; gbc.gridy = 2;
        JButton btnPortiere = createPosizioneButton("P1", Calciatore.Ruolo.PORTIERE);
        campo.add(btnPortiere, gbc);
        
        // Difensori
        int numDifensori = moduloNumeri[0];
        for (int i = 0; i < numDifensori; i++) {
            gbc.gridx = 1; 
            gbc.gridy = getYPosition(i, numDifensori);
            JButton btn = createPosizioneButton("D" + (i + 1), Calciatore.Ruolo.DIFENSORE);
            campo.add(btn, gbc);
        }
        
        // Centrocampisti
        int numCentrocampisti = moduloNumeri[1];
        for (int i = 0; i < numCentrocampisti; i++) {
            gbc.gridx = 2;
            gbc.gridy = getYPosition(i, numCentrocampisti);
            JButton btn = createPosizioneButton("C" + (i + 1), Calciatore.Ruolo.CENTROCAMPISTA);
            campo.add(btn, gbc);
        }
        
        // Attaccanti
        int numAttaccanti = moduloNumeri[2];
        for (int i = 0; i < numAttaccanti; i++) {
            gbc.gridx = 3;
            gbc.gridy = getYPosition(i, numAttaccanti);
            JButton btn = createPosizioneButton("A" + (i + 1), Calciatore.Ruolo.ATTACCANTE);
            campo.add(btn, gbc);
        }
    }
    
    private int getYPosition(int index, int total) {
        // Distribuisce i giocatori verticalmente in modo equilibrato
        if (total == 1) return 2;
        if (total == 2) return index == 0 ? 1 : 3;
        if (total == 3) return index;
        if (total == 4) return index;
        if (total == 5) return index;
        return index;
    }
    
    private JButton createPosizioneButton(String posizione, Calciatore.Ruolo ruolo) {
        JButton btn = new JButton("+");
        btn.setPreferredSize(new Dimension(80, 80));
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        btn.setBackground(getRuoloColor(ruolo));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Aggiungi " + ruolo.name().toLowerCase());
        
        btn.addActionListener(e -> apriSelettoreGiocatore(posizione, ruolo));
        
        bottoniPosizioni.put(posizione, btn);
        return btn;
    }
    
    private Color getRuoloColor(Calciatore.Ruolo ruolo) {
        return switch (ruolo) {
            case PORTIERE -> new Color(255, 193, 7);
            case DIFENSORE -> new Color(63, 81, 181);
            case CENTROCAMPISTA -> new Color(76, 175, 80);
            case ATTACCANTE -> new Color(244, 67, 54);
        };
    }
    
    private JPanel createGiocatoriPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Giocatori Disponibili"));
        panel.setPreferredSize(new Dimension(350, 500));
        
        // Tabella giocatori
        String[] colonne = {"Nome", "Ruolo", "Costo"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaGiocatori = new JTable(modelTabella);
        tabellaGiocatori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaGiocatori.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(tabellaGiocatori);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Info panel
        JLabel infoLabel = new JLabel("<html><center>Doppio click su un giocatore<br>per aggiungerlo alla posizione selezionata</center></html>");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        btnSalva = new JButton("💾 Salva Formazione");
        btnReset = new JButton("🔄 Reset");
        JButton btnChiudi = new JButton("❌ Chiudi");
        
        // Stile pulsanti
        styleButton(btnSalva, new Color(76, 175, 80));
        styleButton(btnReset, new Color(255, 152, 0));
        styleButton(btnChiudi, new Color(158, 158, 158));
        
        btnSalva.addActionListener(this::salvaFormazione);
        btnReset.addActionListener(this::resetFormazione);
        btnChiudi.addActionListener(e -> dispose());
        
        bottomPanel.add(btnSalva);
        bottomPanel.add(btnReset);
        bottomPanel.add(btnChiudi);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadGiocatori() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                modelTabella.setRowCount(0);
                
                for (Calciatore calciatore : squadra.getCalciatori()) {
                    Object[] riga = {
                        calciatore.getNomeCompleto(),
                        calciatore.getRuolo().name(),
                        calciatore.getCosto() + "€"
                    };
                    modelTabella.addRow(riga);
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private void apriSelettoreGiocatore(String posizione, Calciatore.Ruolo ruolo) {
        // Filtra giocatori per ruolo
        List<Calciatore> giocatoriDisponibili = squadra.getCalciatori().stream()
            .filter(c -> c.getRuolo() == ruolo)
            .filter(c -> !posizioni.containsValue(c)) // Non già selezionati
            .toList();
        
        if (giocatoriDisponibili.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Nessun " + ruolo.name().toLowerCase() + " disponibile!",
                "Nessun giocatore",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Crea dialog di selezione
        PlayerSelectionDialog dialog = new PlayerSelectionDialog(this, giocatoriDisponibili, ruolo);
        dialog.setVisible(true);
        
        if (dialog.getGiocatoreSelezionato() != null) {
            assegnaGiocatore(posizione, dialog.getGiocatoreSelezionato());
        }
    }
    
    private void assegnaGiocatore(String posizione, Calciatore giocatore) {
        posizioni.put(posizione, giocatore);
        
        JButton btn = bottoniPosizioni.get(posizione);
        btn.setText("<html><center>" + giocatore.getCognome() + "</center></html>");
        btn.setToolTipText(giocatore.getNomeCompleto() + " (" + giocatore.getCosto() + "€)");
        
        updateSalvaButton();
        campoPanel.repaint();
    }
    
    private void updateSalvaButton() {
        // Abilita salva solo se abbiamo 11 giocatori
        int giocatoriSelezionati = posizioni.size();
        btnSalva.setEnabled(giocatoriSelezionati == 11);
        btnSalva.setText("💾 Salva Formazione (" + giocatoriSelezionati + "/11)");
    }
    
    private void salvaFormazione(ActionEvent e) {
        if (posizioni.size() != 11) {
            JOptionPane.showMessageDialog(this,
                "Devi selezionare esattamente 11 giocatori!",
                "Formazione incompleta",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return salvaFormazioneDatabase();
            }
            
            @Override
            protected void done() {
                try {
                    boolean successo = get();
                    if (successo) {
                        JOptionPane.showMessageDialog(VisualFormationDialog.this,
                            "Formazione salvata con successo!",
                            "Successo",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(VisualFormationDialog.this,
                            "Errore durante il salvataggio.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(VisualFormationDialog.this,
                        "Errore: " + ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private boolean salvaFormazioneDatabase() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            String modulo = (String) comboModulo.getSelectedItem();
            int idFormazione = -1;
            
            // 1. Prima controlla se esiste già una formazione per questa squadra
            String sqlCercaFormazione = "SELECT ID_Formazione FROM FORMAZIONE WHERE ID_Squadra = ?";
            try (PreparedStatement stmtCerca = conn.prepareStatement(sqlCercaFormazione)) {
                stmtCerca.setInt(1, squadra.getIdSquadraFantacalcio());
                
                try (ResultSet rs = stmtCerca.executeQuery()) {
                    if (rs.next()) {
                        // Formazione esistente - aggiorna
                        idFormazione = rs.getInt("ID_Formazione");
                        String sqlAggiornaFormazione = "UPDATE FORMAZIONE SET Modulo = ? WHERE ID_Formazione = ?";
                        try (PreparedStatement stmtAggiorna = conn.prepareStatement(sqlAggiornaFormazione)) {
                            stmtAggiorna.setString(1, modulo);
                            stmtAggiorna.setInt(2, idFormazione);
                            stmtAggiorna.executeUpdate();
                        }
                    } else {
                        // Nuova formazione - inserisci
                        String sqlNuovaFormazione = "INSERT INTO FORMAZIONE (Modulo, ID_Squadra) VALUES (?, ?)";
                        try (PreparedStatement stmtNuova = conn.prepareStatement(sqlNuovaFormazione, Statement.RETURN_GENERATED_KEYS)) {
                            stmtNuova.setString(1, modulo);
                            stmtNuova.setInt(2, squadra.getIdSquadraFantacalcio());
                            stmtNuova.executeUpdate();
                            
                            try (ResultSet generatedKeys = stmtNuova.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    idFormazione = generatedKeys.getInt(1);
                                } else {
                                    throw new SQLException("Impossibile ottenere ID formazione generato");
                                }
                            }
                        }
                    }
                }
            }
            
            if (idFormazione == -1) {
                throw new SQLException("Impossibile ottenere ID formazione valido");
            }
            
            // 2. Pulisci le associazioni esistenti per questa formazione
            String sqlPulisci = "DELETE FROM FORMANO WHERE ID_Formazione = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlPulisci)) {
                stmt.setInt(1, idFormazione);
                int deleted = stmt.executeUpdate();
                System.out.println("DEBUG: Cancellate " + deleted + " associazioni esistenti");
            }
            
            // 3. Inserisci i titolari
            String sqlTitolari = "INSERT INTO FORMANO (ID_Calciatore, ID_Formazione, Panchina) VALUES (?, ?, 'NO')";
            try (PreparedStatement stmt = conn.prepareStatement(sqlTitolari)) {
                for (Map.Entry<String, Calciatore> entry : posizioni.entrySet()) {
                    Calciatore calciatore = entry.getValue();
                    System.out.println("DEBUG: Inserisco titolare - ID calciatore: " + calciatore.getIdCalciatore() + 
                                    " (" + calciatore.getNomeCompleto() + "), ID formazione: " + idFormazione);
                    
                    stmt.setInt(1, calciatore.getIdCalciatore());
                    stmt.setInt(2, idFormazione);
                    stmt.addBatch();
                }
                int[] risultatiTitolari = stmt.executeBatch();
                System.out.println("DEBUG: Inseriti " + risultatiTitolari.length + " titolari");
            }
            
            // 4. Aggiungi panchinari (restanti giocatori della squadra)
            String sqlPanchinari = "INSERT INTO FORMANO (ID_Calciatore, ID_Formazione, Panchina) VALUES (?, ?, 'SI')";
            try (PreparedStatement stmt = conn.prepareStatement(sqlPanchinari)) {
                int panchinariInseriti = 0;
                
                for (Calciatore calciatore : squadra.getCalciatori()) {
                    // Se il calciatore non è tra i titolari, aggiungilo in panchina
                    if (!posizioni.containsValue(calciatore)) {
                        System.out.println("DEBUG: Inserisco panchinaro - ID calciatore: " + calciatore.getIdCalciatore() + 
                                        " (" + calciatore.getNomeCompleto());
                        
                        stmt.setInt(1, calciatore.getIdCalciatore());
                        stmt.setInt(2, idFormazione);
                        stmt.addBatch();
                        panchinariInseriti++;
                    }
                }
                
                if (panchinariInseriti > 0) {
                    int[] risultatiPanchina = stmt.executeBatch();
                    System.out.println("DEBUG: Inseriti " + risultatiPanchina.length + " panchinari");
                }
            }
            
            // 5. Associa la formazione alla giornata (tabella SCHIERAMENTO)
            String sqlSchieramento = """
                INSERT INTO SCHIERAMENTO (ID_Squadra, ID_Formazione) 
                VALUES (?, ?) 
                ON DUPLICATE KEY UPDATE ID_Formazione = VALUES(ID_Formazione)
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sqlSchieramento)) {
                stmt.setInt(1, squadra.getIdSquadraFantacalcio());
                stmt.setInt(2, idFormazione);
                stmt.setInt(3, giornataCorrente);
                int updated = stmt.executeUpdate();
                System.out.println("DEBUG: Schieramento aggiornato: " + updated + " righe modificate");
            }
            
            conn.commit();
            System.out.println("Formazione salvata con successo: " + posizioni.size() + " titolari per la giornata " + giornataCorrente);
            return true;
            
        } catch (SQLException ex) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Errore rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Errore salvataggio formazione: " + ex.getMessage());
            ex.printStackTrace(); // Aggiungi questo per vedere lo stack trace completo
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Errore ripristino autocommit: " + ex.getMessage());
            }
        }
    }
    
    private void resetFormazione(ActionEvent e) {
        int conferma = JOptionPane.showConfirmDialog(this,
            "Sei sicuro di voler resettare la formazione?",
            "Conferma Reset",
            JOptionPane.YES_NO_OPTION);
        
        if (conferma == JOptionPane.YES_OPTION) {
            posizioni.clear();
            
            // Reset bottoni
            for (JButton btn : bottoniPosizioni.values()) {
                btn.setText("+");
                btn.setToolTipText("Aggiungi giocatore");
            }
            
            updateSalvaButton();
            campoPanel.repaint();
        }
    }
    
    private int[] parseModulo(String modulo) {
        String[] parti = modulo.split("-");
        int[] numeri = new int[3];
        for (int i = 0; i < parti.length; i++) {
            numeri[i] = Integer.parseInt(parti[i]);
        }
        return numeri;
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    /**
     * Dialog interno per selezionare un giocatore per una posizione
     */
    private class PlayerSelectionDialog extends JDialog {
        private Calciatore giocatoreSelezionato = null;
        
        public PlayerSelectionDialog(Window parent, List<Calciatore> giocatori, Calciatore.Ruolo ruolo) {
            super(parent, "Seleziona " + ruolo.name(), ModalityType.APPLICATION_MODAL);
            setSize(400, 300);
            setLocationRelativeTo(parent);
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            DefaultListModel<Calciatore> model = new DefaultListModel<>();
            for (Calciatore c : giocatori) {
                model.addElement(c);
            }
            
            JList<Calciatore> list = new JList<>(model);
            list.setCellRenderer(new CalciatoreListCellRenderer());
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            JScrollPane scrollPane = new JScrollPane(list);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnSeleziona = new JButton("Seleziona");
            JButton btnAnnulla = new JButton("Annulla");
            
            buttonPanel.add(btnSeleziona);
            buttonPanel.add(btnAnnulla);
            
            btnSeleziona.addActionListener(e -> {
                giocatoreSelezionato = list.getSelectedValue();
                dispose();
            });
            
            btnAnnulla.addActionListener(e -> dispose());
            
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            setContentPane(panel);
        }
        
        public Calciatore getGiocatoreSelezionato() {
            return giocatoreSelezionato;
        }
    }
    
    /**
     * Renderer personalizzato per visualizzare i giocatori nelle liste
     */
    private static class CalciatoreListCellRenderer extends JLabel implements javax.swing.ListCellRenderer<Calciatore> {
        public CalciatoreListCellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends Calciatore> list,
                Calciatore calciatore, int index, boolean isSelected, boolean cellHasFocus) {
            
            setText(calciatore.getNomeCompleto() + " (" + calciatore.getCosto() + "€)");
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
}