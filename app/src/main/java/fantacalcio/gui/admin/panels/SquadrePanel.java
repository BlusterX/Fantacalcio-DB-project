package fantacalcio.gui.admin.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.CalciatoreDAO;
import fantacalcio.dao.SquadraDAO;
import fantacalcio.gui.admin.AdminContext;
import fantacalcio.model.Squadra;
public final class SquadrePanel extends JPanel {
    private final AdminContext ctx;
    private final SquadraDAO squadraDAO;
    private final CalciatoreDAO calciatoreDAO;
    private final Integer campionatoFilter;


    private JTable tabellaSquadre;
    private DefaultTableModel modelTabella;
    private JButton btnRefresh;
    private JLabel lblNumSquadre, lblTotCalciatori;

    public SquadrePanel(AdminContext ctx, Integer campionatoFilter) {
        this.ctx = ctx;
        this.campionatoFilter = campionatoFilter;
        this.squadraDAO = new SquadraDAO();
        this.calciatoreDAO = new CalciatoreDAO();

        initUI();
        loadData();
    }
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        String[] cols = {"Nome Squadra", "N. Calciatori"};
        modelTabella = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabellaSquadre = new JTable(modelTabella);
        tabellaSquadre.setRowHeight(25);
        tabellaSquadre.getTableHeader().setReorderingAllowed(false);

        // Top bar (titolo + refresh)
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Squadre");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        top.add(title, BorderLayout.WEST);
        btnRefresh = mkBtn("Aggiorna");
        btnRefresh.addActionListener(e -> loadData());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(btnRefresh);
        top.add(actions, BorderLayout.EAST);

        // Stats
        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        stats.setPreferredSize(new Dimension(200, 0));
        stats.setBorder(BorderFactory.createTitledBorder("Statistiche"));
        lblNumSquadre = new JLabel("Squadre: 0");
        lblTotCalciatori = new JLabel("Tot. Calciatori: 0");
        stats.add(Box.createVerticalStrut(8));
        stats.add(lblNumSquadre);
        stats.add(lblTotCalciatori);
        stats.add(Box.createVerticalGlue());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tabellaSquadre), BorderLayout.CENTER);
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
        List<Squadra> squadre = (campionatoFilter != null)
                ? squadraDAO.trovaSquadrePerCampionato(campionatoFilter)
                : squadraDAO.trovaTutteLeSquadre();

        int totalCalciatori = 0;
        for (Squadra s : squadre) {
            int n = calciatoreDAO.contaCalciatoriPerSquadra(s.getIdSquadra());
            totalCalciatori += n;
            modelTabella.addRow(new Object[]{ s.getNome(), n });
        }
        updateStats(squadre.size(), totalCalciatori);
    }

    private void updateStats(int numSquadre, int totalCalciatori) {
        lblNumSquadre.setText("Squadre: " + numSquadre);
        lblTotCalciatori.setText("Tot. Calciatori: " + totalCalciatori);
    }

    public java.util.List<Squadra> getAllSquadre() {
        return squadraDAO.trovaTutteLeSquadre();
    }

    public int getNumSquadre() { return modelTabella.getRowCount(); }

}
