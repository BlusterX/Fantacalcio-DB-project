package fantacalcio.gui.user.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.FormazioneDAO;
import fantacalcio.dao.PunteggioGiocatoreDAO;
import fantacalcio.dao.ScontroLegaDAO;
import fantacalcio.dao.VotoGiornataDAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.PunteggioGiocatore;
import fantacalcio.model.ScontroLega;
import fantacalcio.model.VotoGiornata;

public class DettaglioScontroDialog extends JDialog {

    private static final int BM_GOL           = 1;
    private static final int BM_ASSIST        = 2;
    private static final int BM_AMMONIZIONE   = 3;
    private static final int BM_ESPULSIONE    = 4;
    private static final int BM_IMBATTIBILITA = 5;

    private final FormazioneDAO formazioneDAO = new FormazioneDAO();
    private final VotoGiornataDAO votoDAO = new VotoGiornataDAO();
    private final PunteggioGiocatoreDAO eventiDAO = new PunteggioGiocatoreDAO();

    public DettaglioScontroDialog(Window parent, int idScontro, int giornata) {
        super(parent, "Dettaglio scontro", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8,8));
        setMinimumSize(new Dimension(1300, 600));

        ScontroLegaDAO sdao = new ScontroLegaDAO();
        var opt = sdao.trovaScontroById(idScontro);
        if (opt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Scontro non trovato", "Errore", JOptionPane.ERROR_MESSAGE);
            dispose(); return;
        }
        ScontroLega s = opt.get();

        JLabel title = new JLabel(s.getNomeIncontro() + " - Giornata " + giornata, JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1,2,8,0));
        center.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        center.add(buildTablePanel(s.getNomeSquadra1(), s.getIdFormazione1(), giornata));
        center.add(buildTablePanel(s.getNomeSquadra2(), s.getIdFormazione2(), giornata));
        add(center, BorderLayout.CENTER);

        JButton chiudi = new JButton("Chiudi");
        chiudi.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(chiudi);
        add(south, BorderLayout.SOUTH);

        setLocationRelativeTo(parent);
        pack();
    }

    private static int ruoloRank(fantacalcio.model.Calciatore.Ruolo r) {
        return switch (r) {
            case PORTIERE -> 0;
            case DIFENSORE -> 1;
            case CENTROCAMPISTA -> 2;
            case ATTACCANTE -> 3;
        };
    }

    private JPanel buildTablePanel(String nomeSquadra, int idFormazione, int giornata) {
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBorder(BorderFactory.createTitledBorder(nomeSquadra));

        String[] cols = {"Ruolo","Giocatore","G","A","Amm","Esp","CS","Voto","Fanta"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 2,3,4,5 -> Integer.class;
                    case 6       -> Boolean.class;
                    case 7,8     -> Double.class;
                    default      -> String.class;
                };
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(26);
        t.getTableHeader().setReorderingAllowed(false);

        List<Calciatore> titolari = formazioneDAO.trovaTitolari(idFormazione);

        // === ORDINAMENTO: Portiere → Difensore → Centrocampista → Attaccante,
        // poi per cognome e nome (case-insensitive)
        titolari.sort(
            Comparator.comparingInt((Calciatore c) -> ruoloRank(c.getRuolo()))
                    .thenComparing(Calciatore::getCognome, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Calciatore::getNome,     String.CASE_INSENSITIVE_ORDER)
        );
        // ===

        double somma = 0.0;
        for (Calciatore c : titolari) {
            VotoGiornata vg = votoDAO.trovaVoto(c.getIdCalciatore(), giornata);
            double votoBase = (vg != null) ? vg.getVotoBase() : 0.0;

            List<PunteggioGiocatore> evs = eventiDAO.trovaEventiCalciatore(c.getIdCalciatore(), giornata);
            int gol=0, assist=0, amm=0, esp=0; boolean cs=false; double extra = 0.0;

            for (PunteggioGiocatore e : evs) {
                int id = e.getIdBonusMalus();
                int q  = e.getQuantita();
                switch (id) {
                    case BM_GOL -> gol += q;
                    case BM_ASSIST -> assist += q;
                    case BM_AMMONIZIONE -> amm += q;
                    case BM_ESPULSIONE -> esp += q;
                    case BM_IMBATTIBILITA -> cs = cs || (q > 0);
                    default -> {}
                }
                double val = eventiDAO.getValoreBonus(id);
                extra += val * q;
            }

            double votoBaseR = Math.round(votoBase * 10.0) / 10.0;
            double fantavoto = Math.max(1.0, Math.round((votoBaseR + extra) * 10.0) / 10.0);
            somma += fantavoto;

            String ruoloStr = switch (c.getRuolo()) {
                case PORTIERE -> "P";
                case DIFENSORE -> "D";
                case CENTROCAMPISTA -> "C";
                case ATTACCANTE -> "A";
            };
            String nome = c.getNome() + " " + c.getCognome();

            m.addRow(new Object[]{ ruoloStr, nome, gol, assist, amm, esp, cs, votoBaseR, fantavoto });
        }

        p.add(new JScrollPane(t), BorderLayout.CENTER);

        JLabel tot = new JLabel(String.format("Totale fanta: %.1f", somma), JLabel.RIGHT);
        tot.setFont(tot.getFont().deriveFont(Font.BOLD));
        p.add(tot, BorderLayout.SOUTH);
        return p;
    }

}
