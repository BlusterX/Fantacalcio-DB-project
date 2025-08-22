package fantacalcio.gui.user.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import fantacalcio.dao.FormazioneDAO;
import fantacalcio.model.Calciatore;

public class DettaglioScontroDialog extends JDialog {
    public DettaglioScontroDialog(Window parent,
                                  String nome1, int idForm1,
                                  String nome2, int idForm2) {
        super(parent, "Formazioni: " + nome1 + " vs " + nome2, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(700, 500));

        FormazioneDAO fdao = new FormazioneDAO();
        var f1 = fdao.getFormazioneById(idForm1);
        var f2 = fdao.getFormazioneById(idForm2);

        String modulo1 = (f1 != null && f1.getModulo()!=null) ? f1.getModulo() : "—";
        String modulo2 = (f2 != null && f2.getModulo()!=null) ? f2.getModulo() : "—";

        java.util.List<Calciatore> t1 = fdao.trovaTitolari(idForm1);
        java.util.List<Calciatore> p1 = fdao.trovaPanchinari(idForm1);
        java.util.List<Calciatore> t2 = fdao.trovaTitolari(idForm2);
        java.util.List<Calciatore> p2 = fdao.trovaPanchinari(idForm2);

        JPanel left  = panelFormazione(nome1, modulo1, t1, p1);
        JPanel right = panelFormazione(nome2, modulo2, t2, p2);

        JPanel center = new JPanel(new GridLayout(1,2,12,0));
        center.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        center.add(left); center.add(right);

        JButton chiudi = new JButton("Chiudi");
        chiudi.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(chiudi);

        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        setLocationRelativeTo(parent);
        pack();
    }

    private JPanel panelFormazione(String nome, String modulo, List<Calciatore> tit, List<Calciatore> pan) {
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBorder(BorderFactory.createTitledBorder(nome + "  •  " + modulo));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        StringBuilder sb = new StringBuilder();
        sb.append("⭐ Titolari (").append(tit.size()).append(")\n");
        for (Calciatore c : tit) sb.append("• ").append(c.getRuolo().getAbbreviazione())
                .append(" ").append(c.getNomeCompleto()).append("\n");
        sb.append("\n🪑 Panchina (").append(pan.size()).append(")\n");
        for (Calciatore c : pan) sb.append("• ").append(c.getRuolo().getAbbreviazione())
                .append(" ").append(c.getNomeCompleto()).append("\n");
        area.setText(sb.toString());

        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }
}
