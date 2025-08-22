package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

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
import fantacalcio.gui.user.dialog.ModuleSelectionDialog;
import fantacalcio.gui.user.dialog.VisualFormationDialog;
import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;

public final class ManageTeamPanel extends JPanel {

    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final SquadraFantacalcioDAO squadraDAO;

    // GUI
    private JTable tabellaSquadre;
    private DefaultTableModel modelTabellaSquadre;
    private JTable tabellaGiocatori;
    private DefaultTableModel modelTabellaGiocatori;

    private JButton btnApriFormazione, btnRefresh;
    private JLabel lblSquadraSelezionata, lblStatistiche;

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
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabellaSquadre = new JTable(modelTabellaSquadre);
        tabellaSquadre.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaSquadre.setRowHeight(30);
        tabellaSquadre.getTableHeader().setReorderingAllowed(false);
        tabellaSquadre.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        tabellaSquadre.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) caricaSquadraSelezionata();
        });
    }

    private void createGiocatoriTable() {
        String[] colonneGiocatori = {"Ruolo", "Nome", "Cognome", "Squadra Serie A", "Costo"};
        modelTabellaGiocatori = new DefaultTableModel(colonneGiocatori, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabellaGiocatori = new JTable(modelTabellaGiocatori);
        tabellaGiocatori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaGiocatori.setRowHeight(25);
        tabellaGiocatori.getTableHeader().setReorderingAllowed(false);
        tabellaGiocatori.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        tabellaGiocatori.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) aprireGestioneFormazione();
            }
        });
    }

    private void createButtons() {
        // MOD: rimossi Modifica/Elimina; aggiunto Apri Formazione
        btnApriFormazione = new JButton("Apri Formazione");
        btnApriFormazione.setBackground(new Color(33, 150, 243));
        btnApriFormazione.setForeground(Color.WHITE);
        btnApriFormazione.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnApriFormazione.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnApriFormazione.setFocusPainted(false);
        btnApriFormazione.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApriFormazione.addActionListener(this::apriFormazioneDaBottone);

        btnRefresh = new JButton("Aggiorna");
        btnRefresh.setBackground(new Color(158, 158, 158));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        JPanel topPanel = createSquadrePanel();
        JPanel bottomPanel = createDettagliPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.4);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createSquadrePanel() {
        JPanel squadrePanel = new JPanel(new BorderLayout());
        squadrePanel.setBorder(BorderFactory.createTitledBorder("Le tue squadre"));

        JScrollPane scrollSquadre = new JScrollPane(tabellaSquadre);
        scrollSquadre.setPreferredSize(new Dimension(800, 200));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnApriFormazione);
        buttonPanel.add(Box.createHorizontalStrut(12));
        buttonPanel.add(btnRefresh);

        squadrePanel.add(scrollSquadre, BorderLayout.CENTER);
        squadrePanel.add(buttonPanel, BorderLayout.SOUTH);
        return squadrePanel;
    }

    private JPanel createDettagliPanel() {
        JPanel dettagliPanel = new JPanel(new BorderLayout());
        dettagliPanel.setBorder(BorderFactory.createTitledBorder("Dettagli squadra"));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        headerPanel.add(lblSquadraSelezionata, BorderLayout.CENTER);
        headerPanel.add(lblStatistiche, BorderLayout.SOUTH);

        JScrollPane scrollGiocatori = new JScrollPane(tabellaGiocatori);
        scrollGiocatori.setPreferredSize(new Dimension(800, 300));

        dettagliPanel.add(headerPanel, BorderLayout.NORTH);
        dettagliPanel.add(scrollGiocatori, BorderLayout.CENTER);
        return dettagliPanel;
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
                    updateSquadreTable(squadre);
                    squadraSelezionata = null;
                    updateDettagli();
                } catch (InterruptedException | ExecutionException e) {
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
            String stato = squadra.isCompletata() ? "Completa" : "In costruzione";
            String ultimaModifica = squadra.getDataUltimaModifica() != null
                    ? squadra.getDataUltimaModifica().format(formatter) : "—";
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

        SwingWorker<SquadraFantacalcio, Void> worker = new SwingWorker<>() {
            @Override
            protected SquadraFantacalcio doInBackground() throws Exception {
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
                } catch (InterruptedException | ExecutionException e) {
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
            return;
        }

        lblSquadraSelezionata.setText(squadraSelezionata.getNomeSquadra());
        lblStatistiche.setText(squadraSelezionata.getStatistiche());

        // colora stato
        lblStatistiche.setForeground(squadraSelezionata.isCompletata()
                ? new Color(76, 175, 80) : new Color(255, 152, 0));

        updateGiocatoriTable();
    }

    private int ruoloRank(fantacalcio.model.Calciatore.Ruolo r) {
        if (r == null) return 99;
        return switch (r) {
            case PORTIERE      -> 0;
            case DIFENSORE     -> 1;
            case CENTROCAMPISTA-> 2;
            case ATTACCANTE    -> 3;
        };
    }

    private void updateGiocatoriTable() {
        modelTabellaGiocatori.setRowCount(0);
        if (squadraSelezionata == null) return;

        // copia difensiva + null-safe
        List<Calciatore> giocatori = new ArrayList<>(
                squadraSelezionata.getCalciatori() != null
                        ? squadraSelezionata.getCalciatori()
                        : List.of()
        );

        // ordinamento: ruolo (P,D,C,A) → cognome → nome (case-insensitive)
        giocatori.sort(
            Comparator.comparingInt((Calciatore c) -> ruoloRank(c.getRuolo()))
                    .thenComparing(Calciatore::getCognome, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Calciatore::getNome, String.CASE_INSENSITIVE_ORDER)
        );

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.ITALY);

        for (Calciatore calciatore : giocatori) {
            Object[] riga = {
                calciatore.getRuolo() != null ? calciatore.getRuolo().getAbbreviazione() : "—",
                calciatore.getNome(),
                calciatore.getCognome(),
                "Serie A", // placeholder
                currency.format(calciatore.getCosto())
            };
            modelTabellaGiocatori.addRow(riga);
        }
    }

    private void apriFormazioneDaBottone(ActionEvent e) {
        aprireGestioneFormazione();
    }

    private void aprireGestioneFormazione() {
        if (squadraSelezionata == null) {
            JOptionPane.showMessageDialog(this,
                "Seleziona prima una squadra.",
                "Nessuna squadra selezionata",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!squadraSelezionata.isCompletata()) {
            JOptionPane.showMessageDialog(this,
                "La squadra deve essere completata prima di poter creare una formazione!",
                "Squadra incompleta",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // dialog selezione modulo
        ModuleSelectionDialog moduloDialog = new ModuleSelectionDialog(
            SwingUtilities.getWindowAncestor(this)
        );
        moduloDialog.setVisible(true);


        if (moduloDialog.isConfirmed() && moduloDialog.getModuloSelezionato() != null) {
            String moduloScelto = moduloDialog.getModuloSelezionato();

            VisualFormationDialog formazioneDialog = new VisualFormationDialog(
                SwingUtilities.getWindowAncestor(this),
                squadraSelezionata,
                parentFrame.getUtenteCorrente(),
                moduloScelto
            );
            formazioneDialog.setVisible(true);
        }
    }
}