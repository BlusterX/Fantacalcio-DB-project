package fantacalcio.gui.admin.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import fantacalcio.dao.CalciatoreDAO;
import fantacalcio.gui.admin.AdminContext;
import fantacalcio.model.Calciatore;
import fantacalcio.model.Squadra;

/** Admin: elenco calciatori (sola lettura, con filtri) */
public final class CalciatoriPanel extends JPanel {

    private final CalciatoreDAO calciatoreDAO;
    private final SquadrePanel squadrePanel;
    private final Integer campionatoFilter;
    private final AdminContext ctx;

    private JTable tabellaCalciatori;
    private DefaultTableModel modelTabella;
    private TableRowSorter<DefaultTableModel> sorter;

    // Filtri
    private JTextField txtFiltro;
    private JComboBox<Calciatore.Ruolo> comboFiltroRuolo;
    private JComboBox<Squadra> comboFiltroSquadra;
    private JButton btnRefresh;
    private List<CalciatoreDAO.CalciatoreConSquadra> lastLoaded = Collections.emptyList();
    
    private JLabel lblTotalCalciatori, lblPortieri, lblDifensori, lblCentrocampisti, lblAttaccanti;

    public CalciatoriPanel(AdminContext ctx, SquadrePanel squadrePanel, Integer campionatoFilter) {
        this.ctx = ctx;
        this.squadrePanel = squadrePanel;
        this.campionatoFilter = campionatoFilter;
        this.calciatoreDAO = new CalciatoreDAO();

        initUI();               
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Tabella (ordine colonne coerente con i dati)
        String[] cols = {"ID", "Nome", "Cognome", "Ruolo", "Squadra", "Costo"};
        modelTabella = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabellaCalciatori = new JTable(modelTabella);
        tabellaCalciatori.setRowHeight(25);
        tabellaCalciatori.getTableHeader().setReorderingAllowed(false);

        sorter = new TableRowSorter<>(modelTabella);
        tabellaCalciatori.setRowSorter(sorter);

        // Filtri
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filter.setBorder(new TitledBorder("Filtri"));

        txtFiltro = new JTextField(16);
        comboFiltroRuolo = new JComboBox<>();
        comboFiltroSquadra = new JComboBox<>();

        comboFiltroRuolo.addItem(null); // tutti
        for (Calciatore.Ruolo r : Calciatore.Ruolo.values()) comboFiltroRuolo.addItem(r);

        comboFiltroSquadra.addItem(null); // tutte
        btnRefresh = mkBtn("Aggiorna");

        filter.add(new JLabel("Cerca:"));
        filter.add(txtFiltro);
        filter.add(new JLabel("Ruolo:"));
        filter.add(comboFiltroRuolo);
        filter.add(new JLabel("Squadra:"));
        filter.add(comboFiltroSquadra);
        filter.add(Box.createHorizontalStrut(8));
        filter.add(btnRefresh);

        txtFiltro.addActionListener(e -> applyFilters());
        comboFiltroRuolo.addActionListener(e -> applyFilters());
        comboFiltroSquadra.addActionListener(e -> applyFilters());
        btnRefresh.addActionListener(e -> loadData());

        // Stats
        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        stats.setPreferredSize(new Dimension(180, 0));
        stats.setBorder(new TitledBorder("Statistiche"));
        lblTotalCalciatori = new JLabel("Totale: 0");
        lblPortieri = new JLabel("Portieri: 0");
        lblDifensori = new JLabel("Difensori: 0");
        lblCentrocampisti = new JLabel("Centrocampisti: 0");
        lblAttaccanti = new JLabel("Attaccanti: 0");
        stats.add(Box.createVerticalStrut(8));
        stats.add(lblTotalCalciatori);
        stats.add(new JSeparator());
        stats.add(lblPortieri);
        stats.add(lblDifensori);
        stats.add(lblCentrocampisti);
        stats.add(lblAttaccanti);
        stats.add(Box.createVerticalGlue());

        add(filter, BorderLayout.NORTH);
        add(new JScrollPane(tabellaCalciatori), BorderLayout.CENTER);
        add(stats, BorderLayout.EAST);
    }

    private JButton mkBtn(String t) {
        JButton b = new JButton(t);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,214,219)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        b.setBackground(Color.WHITE);
        return b;
    }

    public void loadData() {
        modelTabella.setRowCount(0);

        // prendi i dati filtrati per campionato (se presente)
        lastLoaded = (campionatoFilter != null)
                ? calciatoreDAO.trovaTuttiICalciatoriConSquadraPerCampionato(campionatoFilter)
                : calciatoreDAO.trovaTuttiICalciatoriConSquadra();

        for (var cs : lastLoaded) {
            Calciatore c = cs.getCalciatore();
            modelTabella.addRow(new Object[]{
                c.getIdCalciatore(), c.getNome(), c.getCognome(),
                c.getRuolo().getAbbreviazione(), cs.getNomeSquadra(),
                c.getCosto() + " €"
            });
        }

        refreshSquadreCombo();
        
        updateStatsFromLoaded();
    }

    private void updateStatsFromLoaded() {
        int p = 0, d = 0, c = 0, a = 0;

        for (var cs : lastLoaded) {
            switch (cs.getCalciatore().getRuolo()) {
                case PORTIERE      -> p++;
                case DIFENSORE     -> d++;
                case CENTROCAMPISTA-> c++;
                case ATTACCANTE    -> a++;
            }
        }
        int tot = lastLoaded.size();

        lblTotalCalciatori.setText("Totale: " + tot);
        lblPortieri.setText("Portieri: " + p);
        lblDifensori.setText("Difensori: " + d);
        lblCentrocampisti.setText("Centrocampisti: " + c);
        lblAttaccanti.setText("Attaccanti: " + a);
    }




    public void refreshSquadreCombo() {
        comboFiltroSquadra.removeAllItems();
        comboFiltroSquadra.addItem(null);

        if (squadrePanel != null) {
            for (Squadra s : squadrePanel.getAllSquadre()) {
                if (campionatoFilter == null || s.getIdCampionato() == campionatoFilter) {
                    comboFiltroSquadra.addItem(s);
                }
            }
        }
    }

    private void applyFilters() {
        final String text = txtFiltro.getText().trim().toLowerCase();
        final Calciatore.Ruolo ruoloSel = (Calciatore.Ruolo) comboFiltroRuolo.getSelectedItem();
        final Squadra squadraSel = (Squadra) comboFiltroSquadra.getSelectedItem();

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                // Nome/Cognome
                if (!text.isEmpty()) {
                    String nome = e.getStringValue(1).toLowerCase();
                    String cognome = e.getStringValue(2).toLowerCase();
                    if (!nome.contains(text) && !cognome.contains(text)) return false;
                }
                // Ruolo
                if (ruoloSel != null) {
                    String abbr = e.getStringValue(3);
                    if (!ruoloSel.getAbbreviazione().equals(abbr)) return false;
                }
                // Squadra
                if (squadraSel != null) {
                    String nomeSq = e.getStringValue(4);
                    if (!squadraSel.getNome().equals(nomeSq)) return false;
                }
                return true;
            }
        });
    }

    public int getNumCalciatori() { return modelTabella.getRowCount(); }

}
