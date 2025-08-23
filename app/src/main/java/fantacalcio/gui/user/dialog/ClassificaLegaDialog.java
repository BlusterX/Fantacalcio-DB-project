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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.ClassificaDAO;
import fantacalcio.model.Lega;

public class ClassificaLegaDialog extends JDialog {

    private final Lega lega;
    private final ClassificaDAO classificaDAO = new ClassificaDAO();

    private JTable table;
    private DefaultTableModel model;
    private JLabel title;
    private Integer idSquadraDaEvidenziare; // opzionale

    public ClassificaLegaDialog(Window parent, Lega lega) {
        this(parent, lega, null);
    }

    public ClassificaLegaDialog(Window parent, Lega lega, Integer idSquadraDaEvidenziare) {
        super(parent, "Classifica – " + lega.getNome(), ModalityType.APPLICATION_MODAL);
        this.lega = lega;
        this.idSquadraDaEvidenziare = idSquadraDaEvidenziare;
        initUI();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));
        setMinimumSize(new Dimension(800, 520));

        title = new JLabel("🏅 Classifica: " + lega.getNome(), JLabel.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(new Color(33,150,243));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Pos", "Squadra", "Pt", "V", "N", "P", "Fanta Fatti", "Fanta Subiti", "Diff"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0,2,3,4,5 -> Integer.class;
                    case 6,7,8 -> Double.class;
                    default -> String.class;
                };
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(63,81,181));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        // larghezze consigliate
        table.getColumnModel().getColumn(0).setPreferredWidth(40);   // Pos
        table.getColumnModel().getColumn(1).setPreferredWidth(260);  // Squadra
        table.getColumnModel().getColumn(2).setPreferredWidth(40);   // Pt

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("🔄 Aggiorna");
        styleButton(btnRefresh, new Color(158,158,158));
        btnRefresh.addActionListener(e -> loadData());
        JButton btnClose = new JButton("Chiudi");
        styleButton(btnClose, new Color(244,67,54));
        btnClose.addActionListener(e -> dispose());
        south.add(btnRefresh); south.add(btnClose);
        add(south, BorderLayout.SOUTH);

        loadData();
        pack();
    }

    private void loadData() {
        model.setRowCount(0);

        new SwingWorker<List<ClassificaDAO.RigaClassificaLega>, Void>() {
            @Override protected List<ClassificaDAO.RigaClassificaLega> doInBackground() {
                return classificaDAO.classificaPerLega(lega.getIdLega());
            }
            @Override protected void done() {
                try {
                    List<ClassificaDAO.RigaClassificaLega> righe = get();
                    int pos = 1;
                    Integer rowToSelect = null;

                    for (var r : righe) {
                        Object[] row = {
                                pos,
                                r.nomeSquadra,
                                r.punti,
                                r.vittorie,
                                r.pareggi,
                                r.sconfitte,
                                round1(r.punteggioTotale),
                                round1(r.punteggioSubito),
                                round1(r.diffPunti)
                        };
                        model.addRow(row);

                        if (idSquadraDaEvidenziare != null && r.idSquadraFantacalcio == idSquadraDaEvidenziare) {
                            rowToSelect = pos - 1;
                        }
                        pos++;
                    }

                    if (rowToSelect != null && rowToSelect >= 0 && rowToSelect < table.getRowCount()) {
                        table.setRowSelectionInterval(rowToSelect, rowToSelect);
                        table.scrollRectToVisible(table.getCellRect(rowToSelect, 0, true));
                    }

                    title.setText("🏅 Classifica: " + lega.getNome() + " — " + righe.size() + " squadre");
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(ClassificaLegaDialog.this,
                            "Errore caricamento classifica: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static double round1(double v){ return Math.round(v*10.0)/10.0; }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
