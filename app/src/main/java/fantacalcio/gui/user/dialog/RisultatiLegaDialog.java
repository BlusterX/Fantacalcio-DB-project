package fantacalcio.gui.user.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.ScontroLegaDAO;
import fantacalcio.model.Lega;
import fantacalcio.model.ScontroLega;

/**
 * Dialog per visualizzare i risultati delle partite di una lega
 */
public class RisultatiLegaDialog extends JDialog {
    
    private final Lega lega;
    private final ScontroLegaDAO scontroDAO;
    private final int giornataCorrente;
    private boolean dblClickHooked = false;
    private Runnable onChange;
    private boolean suppressComboEvents = false;

    // Componenti GUI
    private JComboBox<Integer> comboGiornata;
    private JTable tabellaRisultati;
    private DefaultTableModel modelTabella;
    private JLabel lblTitolo;

    public RisultatiLegaDialog(Window parent, Lega lega, int giornataCorrente, Runnable onChange) {
        super(parent, "Risultati Lega - " + lega.getNome(), ModalityType.APPLICATION_MODAL);
        this.lega = lega;
        this.giornataCorrente = giornataCorrente;
        this.scontroDAO = new ScontroLegaDAO();
        this.onChange = onChange;
        initializeGUI();
        setLocationRelativeTo(parent);
    }
    
    public RisultatiLegaDialog(Window parent, Lega lega, int giornataCorrente) {
        super(parent, "Risultati Lega - " + lega.getNome(), ModalityType.APPLICATION_MODAL);
        this.lega = lega;
        this.giornataCorrente = giornataCorrente;
        this.scontroDAO = new ScontroLegaDAO();
        
        initializeGUI();
        setLocationRelativeTo(parent);
    }
    
    private void initializeGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 600));
        
        createComponents();
        setupLayout();
        loadRisultati();
        
        pack();
    }
    
    private void createComponents() {
        // Header
        lblTitolo = new JLabel("📊 Risultati Lega: " + lega.getNome());
        lblTitolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        lblTitolo.setForeground(new Color(33, 150, 243));
        
        comboGiornata = new JComboBox<>();
        int maxGiornate = scontroDAO.getMaxGiornatePublic(lega.getIdLega());
        if (maxGiornate <= 0) maxGiornate = 1;
        for (int i = 1; i <= maxGiornate; i++) comboGiornata.addItem(i);
        comboGiornata.setSelectedItem(Math.min(giornataCorrente, maxGiornate));

        comboGiornata.addActionListener(e -> {
                    if (!suppressComboEvents) loadRisultati();
                });
        String[] colonne = {"#","Partita","Risultato","Stato","Data"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        
        tabellaRisultati = new JTable(modelTabella);
        tabellaRisultati.setRowHeight(35);
        tabellaRisultati.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaRisultati.getTableHeader().setForeground(Color.WHITE);
        tabellaRisultati.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        tabellaRisultati.removeColumn(tabellaRisultati.getColumnModel().getColumn(0));
        // Larghezza colonne
        tabellaRisultati.getColumnModel().getColumn(0).setPreferredWidth(300); // Partita
        tabellaRisultati.getColumnModel().getColumn(1).setPreferredWidth(120); // Risultato
        tabellaRisultati.getColumnModel().getColumn(2).setPreferredWidth(100); // Stato
        tabellaRisultati.getColumnModel().getColumn(3).setPreferredWidth(150); // Data
        if (!dblClickHooked) {
            dblClickHooked = true;
            tabellaRisultati.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int viewRow = tabellaRisultati.getSelectedRow();
                        if (viewRow >= 0) { 
                            int modelRow = tabellaRisultati.convertRowIndexToModel(viewRow);
                            int idScontro = (Integer) modelTabella.getValueAt(modelRow, 0);
                            apriDettaglioScontro(idScontro);
                        }
                    }
                }
            });
        }
    }

    private void refreshComboGiornate(Integer preferita) {
        int max = scontroDAO.getMaxGiornatePublic(lega.getIdLega());
        suppressComboEvents = true;
        try {
            comboGiornata.removeAllItems();
            for (int i = 1; i <= Math.max(1, max); i++) comboGiornata.addItem(i);

            Integer target = (preferita != null && preferita >= 1 && preferita <= Math.max(1, max))
                    ? preferita
                    : Math.max(1, max);
            comboGiornata.setSelectedItem(target);
        } finally {
            suppressComboEvents = false;
        }
    }

    
    private void setupLayout() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        headerPanel.add(lblTitolo, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout());
        controlsPanel.add(new JLabel("Giornata:"));
        controlsPanel.add(comboGiornata);
        JButton btnAggiorna = new JButton("🔄 Aggiorna");
        styleButton(btnAggiorna, new Color(158, 158, 158));
        btnAggiorna.addActionListener(e -> loadRisultati());
        controlsPanel.add(Box.createHorizontalStrut(20));
        controlsPanel.add(btnAggiorna);

        JScrollPane scrollPane = new JScrollPane(tabellaRisultati);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Partite della Giornata"));

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton btnChiudi = new JButton("Chiudi");
        styleButton(btnChiudi, new Color(244, 67, 54));
        btnChiudi.addActionListener(e -> dispose());
        bottomPanel.add(btnChiudi);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(controlsPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        JButton btnSimulaGiornata = new JButton("▶ Simula giornata");
        styleButton(btnSimulaGiornata, new Color(76, 175, 80));

        JButton btnSimulaTutto = new JButton("⏩ Simula tutta la lega");
        styleButton(btnSimulaTutto, new Color(33, 150, 243));

        btnSimulaGiornata.addActionListener(e -> {
        Integer g = (Integer) comboGiornata.getSelectedItem();
        if (g == null) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() {
                new fantacalcio.service.MatchSimulationService().simulaGiornata(lega.getIdLega(), g);
                return null;
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                int next = scontroDAO.getGiornataCorrente(lega.getIdLega());
                refreshComboGiornate(next);
                loadRisultati();
                if (onChange != null) onChange.run();
                }
            }.execute();
        });

        btnSimulaTutto.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<Void,Void>() {
                @Override protected Void doInBackground() {
                    new fantacalcio.service.MatchSimulationService().simulaTuttaLaLega(lega.getIdLega());
                    return null;
                }
                @Override protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    refreshComboGiornate(null);
                    loadRisultati();
                    if (onChange != null) onChange.run();
                }
            }.execute();
        });

        controlsPanel.add(Box.createHorizontalStrut(12));
        controlsPanel.add(btnSimulaGiornata);
        controlsPanel.add(btnSimulaTutto);
    }

    
    private void loadRisultati() {
        Integer giornataSelezionata = (Integer) comboGiornata.getSelectedItem();
        if (giornataSelezionata == null) return;
        
        SwingWorker<List<ScontroLega>, Void> worker = new SwingWorker<List<ScontroLega>, Void>() {
            @Override
            protected List<ScontroLega> doInBackground() throws Exception {
                return scontroDAO.trovaScontriGiornata(lega.getIdLega(), giornataSelezionata);
            }
            
            @Override
            protected void done() {
                try {
                    List<ScontroLega> scontri = get();
                    updateTable(scontri, giornataSelezionata);
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Errore caricamento risultati: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateTable(List<ScontroLega> scontri, int giornata) {
        modelTabella.setRowCount(0);
        var fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm");

        for (ScontroLega s : scontri) {
            String partita   = s.getNomeSquadra1() + " vs " + s.getNomeSquadra2();
            String risultato = (s.getRisultato() != null && !s.getRisultato().isBlank()) ? s.getRisultato() : "—";
            String stato = s.getStatoEmoji() + " " + s.getDescrizioneStato();
            String data = (s.getDataInizio() != null) ? s.getDataInizio().format(fmt) : "Da programmare";

            // 0 = ID_Scontro (COLUMNA NASCOSTA)
            modelTabella.addRow(new Object[]{
                s.getIdScontro(), partita, risultato, stato, data
            });
        }

        lblTitolo.setText("📊 Risultati Lega: " + lega.getNome()
                + " - Giornata " + giornata + " (" + scontri.size() + " partite)");
    }


    private void apriDettaglioScontro(int idScontro) {
        Integer g = (Integer) comboGiornata.getSelectedItem();
        if (g == null) return;
        new DettaglioScontroDialog(this, idScontro, g).setVisible(true);
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}