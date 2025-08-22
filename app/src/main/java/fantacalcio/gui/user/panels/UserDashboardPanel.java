package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

public final class UserDashboardPanel extends JPanel {

    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final SquadraFantacalcioDAO squadraDAO;

    // GUI
    private JTable tabellaSquadre;
    private DefaultTableModel modelTabella;
    private JButton btnGestisciLeghe;
    // Stats interne (se vuoi usarle altrove)
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
        createTablePanel();
        createQuickActionsPanel();
    }
    private void createTablePanel() {
        String[] colonne = {"Nome Squadra", "Giocatori", "Budget Speso", "Budget Rimanente", "Stato"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabellaSquadre = new JTable(modelTabella);
        tabellaSquadre.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaSquadre.setRowHeight(30);
        // stile header leggero
        tabellaSquadre.getTableHeader().setReorderingAllowed(false);
        tabellaSquadre.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        // larghezze
        tabellaSquadre.getColumnModel().getColumn(0).setPreferredWidth(220);
        tabellaSquadre.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabellaSquadre.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabellaSquadre.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabellaSquadre.getColumnModel().getColumn(4).setPreferredWidth(110);
    }
    private void createQuickActionsPanel() {
        btnGestisciLeghe = new JButton("Le mie Leghe");
        styleButton(btnGestisciLeghe, new Color(33, 150, 243));
        btnGestisciLeghe.addActionListener(e -> parentFrame.switchToLeagues());
    }
    private void setupLayout() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Le tue squadre"));
        JScrollPane scrollPane = new JScrollPane(tabellaSquadre);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        JPanel actionsPanel = new JPanel(new FlowLayout());
        actionsPanel.add(btnGestisciLeghe);
        add(tablePanel, BorderLayout.CENTER);
        add(actionsPanel, BorderLayout.SOUTH);
    }
    public void refreshData() {
        SwingWorker<List<SquadraFantacalcio>, Void> worker = new SwingWorker<>() {
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
        for (SquadraFantacalcio squadra : squadre) {
            String stato = squadra.isCompletata() ? "Completa" : "In costruzione";
            Object[] riga = {
                squadra.getNomeSquadra(),
                squadra.getNumeroTotaleGiocatori() + "/" + SquadraFantacalcio.TOTALE_GIOCATORI,
                squadra.getBudgetSpeso() + " €",
                squadra.getBudgetRimanente() + " €",
                stato
            };
            modelTabella.addRow(riga);
        }
    }
    private void updateStats(List<SquadraFantacalcio> squadre) {
        numeroSquadreUtente = squadre.size();
        numeroSquadreComplete = (int) squadre.stream().filter(SquadraFantacalcio::isCompletata).count();
        // se un giorno vuoi mostrarle in alto, hai già i valori qui
    }
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    public int getNumeroSquadreUtente() { return numeroSquadreUtente; }
    public int getNumeroSquadreComplete() { return numeroSquadreComplete; }
}