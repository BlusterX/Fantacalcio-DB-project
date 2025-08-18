package fantacalcio.gui.user.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

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
    private int giornataCorrente;
    
    // Componenti GUI
    private JComboBox<Integer> comboGiornata;
    private JTable tabellaRisultati;
    private DefaultTableModel modelTabella;
    private JLabel lblTitolo;
    
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
        
        // Combo per selezione giornata
        comboGiornata = new JComboBox<>();
        for (int i = 1; i <= giornataCorrente; i++) {
            comboGiornata.addItem(i);
        }
        comboGiornata.setSelectedItem(giornataCorrente);
        comboGiornata.addActionListener(e -> loadRisultati());
        
        // Tabella risultati
        String[] colonne = {"Partita", "Risultato", "Stato", "Data"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaRisultati = new JTable(modelTabella);
        tabellaRisultati.setRowHeight(35);
        tabellaRisultati.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaRisultati.getTableHeader().setForeground(Color.WHITE);
        tabellaRisultati.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Larghezza colonne
        tabellaRisultati.getColumnModel().getColumn(0).setPreferredWidth(300); // Partita
        tabellaRisultati.getColumnModel().getColumn(1).setPreferredWidth(120); // Risultato
        tabellaRisultati.getColumnModel().getColumn(2).setPreferredWidth(100); // Stato
        tabellaRisultati.getColumnModel().getColumn(3).setPreferredWidth(150); // Data
    }
    
    private void setupLayout() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        headerPanel.add(lblTitolo, BorderLayout.CENTER);
        
        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout());
        controlsPanel.add(new JLabel("Giornata:"));
        controlsPanel.add(comboGiornata);
        
        JButton btnAggiorna = new JButton("🔄 Aggiorna");
        styleButton(btnAggiorna, new Color(158, 158, 158));
        btnAggiorna.addActionListener(e -> loadRisultati());
        controlsPanel.add(Box.createHorizontalStrut(20));
        controlsPanel.add(btnAggiorna);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(tabellaRisultati);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Partite della Giornata"));
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton btnChiudi = new JButton("Chiudi");
        styleButton(btnChiudi, new Color(244, 67, 54));
        btnChiudi.addActionListener(e -> dispose());
        bottomPanel.add(btnChiudi);
        
        // Layout finale
        add(headerPanel, BorderLayout.NORTH);
        add(controlsPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Correggi layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(controlsPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadRisultati() {
        Integer giornataSelezionata = (Integer) comboGiornata.getSelectedItem();
        if (giornataSelezionata == null) return;
        
        SwingWorker<List<ScontroLega>, Void> worker = new SwingWorker<List<ScontroLega>, Void>() {
            @Override
            protected List<ScontroLega> doInBackground() throws Exception {
                return scontroDAO.trovaSccontriGiornata(lega.getIdLega(), giornataSelezionata);
            }
            
            @Override
            protected void done() {
                try {
                    List<ScontroLega> scontri = get();
                    updateTable(scontri, giornataSelezionata);
                } catch (Exception e) {
                    System.err.println("Errore caricamento risultati: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateTable(List<ScontroLega> scontri, int giornata) {
        modelTabella.setRowCount(0);
        
        for (ScontroLega scontro : scontri) {
            String partita = scontro.getNomeSquadra1() + " vs " + scontro.getNomeSquadra2();
            String risultato = scontro.getRisultato();
            String stato = scontro.getStatoEmoji() + " " + scontro.getDescrizioneStato();
            String data = scontro.getDataInizio() != null ? 
                scontro.getDataInizio().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")) : 
                "Da programmare";
            
            Object[] riga = {partita, risultato, stato, data};
            modelTabella.addRow(riga);
        }
        
        // Aggiorna titolo con numero partite
        lblTitolo.setText("📊 Risultati Lega: " + lega.getNome() + 
                         " - Giornata " + giornata + " (" + scontri.size() + " partite)");
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