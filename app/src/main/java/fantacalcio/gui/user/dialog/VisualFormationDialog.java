package fantacalcio.gui.user.dialog;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fantacalcio.dao.FormazioneDAO;
import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;

/** Dialog visuale (rifatto) per creare/salvare la formazione */
public class VisualFormationDialog extends JDialog {

    private final SquadraFantacalcio squadra;
    private final Utente utente;
    private final String modulo;          // es. "4-3-3"
    private final int giornataCorrente;

    // logica
    private final int[] moduloNumeri; // [D, C, A]
    private final int titolariRichiesti; // 1 + D + C + A
    private final Map<String, Calciatore> posizioni = new HashMap<>(); // "P1","D1","C2","A3" -> Calciatore
    private final Map<String, JButton> bottoniPosizione = new HashMap<>();

    // UI
    private PitchPanel pitchPanel;
    private JButton btnSalva, btnReset;

    public VisualFormationDialog(Window parent, SquadraFantacalcio squadra, Utente utente,
                                 String modulo, int giornataCorrente) {
        super(parent, "Formazione " + modulo + " – " + squadra.getNomeSquadra(), ModalityType.APPLICATION_MODAL);
        this.squadra = squadra;
        this.utente = utente;
        this.modulo = modulo;
        this.giornataCorrente = giornataCorrente;
        this.moduloNumeri = parseModulo(modulo);
        this.titolariRichiesti = 1 + moduloNumeri[0] + moduloNumeri[1] + moduloNumeri[2];

        buildUI();
        setMinimumSize(new Dimension(1060, 720));
        setLocationRelativeTo(parent);
    }

    public VisualFormationDialog(Window parent,
                                SquadraFantacalcio squadra,
                                Utente utente,
                                String modulo) {
        this(parent,
            squadra,
            utente,
            modulo,
            new fantacalcio.dao.ScontroLegaDAO().getGiornataCorrente(squadra.getIdLega()));
    }

    /* ------------------------- UI ------------------------- */

    private void buildUI() {
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        updateCountersAndSave();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 12, 20));
        header.setBackground(new Color(23, 110, 64));

        JLabel title = new JLabel("⚽ Formazione " + modulo + "  •  Giornata " + giornataCorrente);
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));

        JLabel hint = new JLabel("Clicca sui pulsanti “+” per scegliere i giocatori del ruolo. Click destro per rimuovere.");
        hint.setForeground(new Color(220, 255, 230));
        hint.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));

        header.add(title, BorderLayout.NORTH);
        header.add(hint, BorderLayout.SOUTH);
        return header;
    }

    private JComponent buildCenter() {
        // Solo il campo, niente split, niente scrollpane
        pitchPanel = new PitchPanel();
        pitchPanel.setLayout(null); // posizionamento assoluto dei bottoni

        // crea i bottoni di posizione
        createPositionButtons();

        // ricalcola le posizioni ogni volta che il pannello viene ridimensionato
        pitchPanel.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                layoutPositionButtons();
            }
        });

        // suggerisco anche una preferred size "comoda"
        pitchPanel.setPreferredSize(new Dimension(1050, 600));
        return pitchPanel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 14, 16));
        footer.setBackground(new Color(248, 249, 251));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        btnSalva = button(" Salva (0/" + titolariRichiesti + ") ", new Color(76, 175, 80), e -> onSave());
        btnReset = button(" Reset ", new Color(255, 152, 0), e -> onReset());
        JButton chiudi = button(" Chiudi ", new Color(158, 158, 158), e -> dispose());

        buttons.add(btnSalva);
        buttons.add(btnReset);
        buttons.add(chiudi);

        footer.add(buttons, BorderLayout.EAST);
        return footer;
    }

    private void createPositionButtons() {
        // GK
        createButton("P1", Calciatore.Ruolo.PORTIERE);

        // D
        for (int i = 0; i < moduloNumeri[0]; i++)
            createButton("D" + (i + 1), Calciatore.Ruolo.DIFENSORE);

        // C
        for (int i = 0; i < moduloNumeri[1]; i++)
            createButton("C" + (i + 1), Calciatore.Ruolo.CENTROCAMPISTA);

        // A
        for (int i = 0; i < moduloNumeri[2]; i++)
            createButton("A" + (i + 1), Calciatore.Ruolo.ATTACCANTE);

        layoutPositionButtons();
    }

    private void createButton(String posId, Calciatore.Ruolo ruolo) {
        JButton b = new JButton("+") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0,0,0,35));
                g2.fillRoundRect(4, 6, w-8, h-8, 28, 28);

                // corpo
                Color base = ruoloColor(ruolo);
                boolean hover = getModel().isRollover();
                Color c1 = hover ? base.brighter() : base;
                Color c2 = hover ? base : base.darker();
                g2.setPaint(new GradientPaint(0, 0, c1, 0, h, c2));
                g2.fillRoundRect(0, 0, w-10, h-10, 28, 28);

                g2.setColor(new Color(255,255,255,160));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, w-10, h-10, 28, 28);

                g2.setColor(Color.WHITE);
                Font f = getFont().deriveFont(Font.BOLD, 14f);
                g2.setFont(f);
                String t = getText();
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w-10 - fm.stringWidth(t)) / 2;
                int ty = (h-10 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(t, tx, ty);

                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 48));
        b.setToolTipText("Scegli " + ruolo.name().toLowerCase());

        b.addActionListener(e -> openPlayerSelector(posId, ruolo));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // rimuovi
                    posizioni.remove(posId);
                    b.setText("+");
                    b.setToolTipText("Scegli " + ruolo.name().toLowerCase());
                    updateCountersAndSave();
                    pitchPanel.repaint();
                }
            }
        });
        bottoniPosizione.put(posId, b);
        pitchPanel.add(b);
    }

    private void layoutPositionButtons() {
        Rectangle field = pitchPanel.getFieldRect();
        if (field == null) return;

        // colonne: GK, D, C, A (come percentuali nella "area di gioco")
        double[] cols = { 0.06, 0.28, 0.52, 0.78 };

        // GK
        place("P1", field, cols[0], 0.50, 120, 48);

        // D
        placeRow("D", moduloNumeri[0], field, cols[1]);

        // C
        placeRow("C", moduloNumeri[1], field, cols[2]);

        // A
        placeRow("A", moduloNumeri[2], field, cols[3]);

        pitchPanel.revalidate();
        pitchPanel.repaint();
    }

    private void placeRow(String prefix, int count, Rectangle field, double colX) {
        double[] ys = distributeY(count);
        for (int i = 0; i < count; i++) {
            String id = prefix + (i + 1);
            place(id, field, colX, ys[i], 120, 48);
        }
    }

    private void place(String id, Rectangle field, double relX, double relY, int w, int h) {
        JButton b = bottoniPosizione.get(id);
        if (b == null) return;
        int x = (int) (field.x + field.width * relX) - w / 2;
        int y = (int) (field.y + field.height * relY) - h / 2;
        b.setBounds(x, y, w, h);
    }

    private double[] distributeY(int n) {
        switch (n) {
            case 1 -> {
                return new double[]{0.50};
            }
            case 2 -> {
                return new double[]{0.35, 0.65};
            }
            case 3 -> {
                return new double[]{0.25, 0.50, 0.75};
            }
            case 4 -> {
                return new double[]{0.18, 0.38, 0.62, 0.82};
            }
            case 5 -> {
                return new double[]{0.12, 0.32, 0.50, 0.68, 0.88};
            }
            case 6 -> {
                return new double[]{0.10, 0.28, 0.46, 0.54, 0.72, 0.90};
            }
            default -> {
                double[] y = new double[n];
                double step = 1.0 / (n + 1);
                for (int i = 0; i < n; i++) y[i] = step * (i + 1);
                return y;
            }
        }
    }

    private void openPlayerSelector(String posId, Calciatore.Ruolo ruolo) {
        List<Calciatore> candidati = getEligiblePlayers(ruolo);

        if (candidati.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nessun " + ruolo.name().toLowerCase() + " disponibile.",
                    "Nessun giocatore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PlayerPicker picker = new PlayerPicker(this, ruolo, candidati);
        picker.setVisible(true);

        Calciatore scelto = picker.getSelected();
        if (scelto != null) {
            posizioni.put(posId, scelto);
            JButton b = bottoniPosizione.get(posId);
            b.setText(shortName(scelto));
            b.setToolTipText(scelto.getNomeCompleto() + " • " + ruolo.name());
            updateCountersAndSave();
            pitchPanel.repaint();
        }
    }

    private List<Calciatore> getEligiblePlayers(Calciatore.Ruolo ruolo) {
        // giocatori del ruolo non ancora usati
        List<Calciatore> out = new ArrayList<>();
        for (Calciatore c : squadra.getCalciatori()) {
            if (c.getRuolo() == ruolo && !posizioni.containsValue(c)) {
                out.add(c);
            }
        }
        // ordina per cognome, poi nome
        out.sort(Comparator.comparing(Calciatore::getCognome).thenComparing(Calciatore::getNome));
        return out;
    }

    private String shortName(Calciatore c) {
        String n = c.getNome() != null && !c.getNome().isBlank() ? c.getNome().trim() : "";
        String g = c.getCognome() != null ? c.getCognome().trim() : "";
        String iniz = n.isEmpty() ? "" : (n.charAt(0) + ". ");
        return (iniz + g).trim();
    }


    private void updateCountersAndSave() {
        int tot = posizioni.size();
        btnSalva.setEnabled(tot == titolariRichiesti);
        btnSalva.setText(" Salva (" + tot + "/" + titolariRichiesti + ") ");
    }


    private void onReset() {
        int conf = JOptionPane.showConfirmDialog(this,
                "Vuoi resettare la formazione (tutti gli 11 titolari)?",
                "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;

        posizioni.clear();
        for (JButton b : bottoniPosizione.values()) {
            b.setText("+");
        }
        updateCountersAndSave();
        pitchPanel.repaint();
    }

    private void onSave() {
        if (posizioni.size() != titolariRichiesti) {
            JOptionPane.showMessageDialog(this,
                    "Seleziona esattamente " + titolariRichiesti + " giocatori.",
                    "Formazione incompleta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnSalva.setEnabled(false);

        // prepara dati per il DAO
        final int idSquadra = squadra.getIdSquadraFantacalcio();
        final String moduloScelto = modulo; // già passato al dialog
        final int giornata = giornataCorrente;

        final List<Integer> idsTitolari = posizioni.values()
                .stream().map(Calciatore::getIdCalciatore).collect(Collectors.toList());

        final Set<Calciatore> setTitolari = new HashSet<>(posizioni.values());
        final List<Integer> idsPanchina = squadra.getCalciatori()
                .stream().filter(c -> !setTitolari.contains(c))
                .map(Calciatore::getIdCalciatore).collect(Collectors.toList());

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                FormazioneDAO dao = new FormazioneDAO();
                return dao.salvaFormazioneCompleta(
                        idSquadra,
                        giornata,
                        moduloScelto,
                        idsTitolari,
                        idsPanchina
                );
            }

            @Override protected void done() {
                btnSalva.setEnabled(true);
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(VisualFormationDialog.this,
                                "Formazione salvata per la giornata " + giornata + "!",
                                "Successo", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(VisualFormationDialog.this,
                                "Errore durante il salvataggio.", "Errore",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(VisualFormationDialog.this,
                            "Errore: " + ex.getMessage(), "Errore",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }


    /* -------------------- Util -------------------- */

    private int[] parseModulo(String m) {
        String[] p = m.split("-");
        if (p.length != 3) return new int[]{4, 4, 2}; // fallback 4-4-2
        return new int[]{ Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]) };
    }

    private Color ruoloColor(Calciatore.Ruolo r) {
        return switch (r) {
            case PORTIERE -> new Color(234, 179, 8);
            case DIFENSORE -> new Color(59, 130, 246);
            case CENTROCAMPISTA -> new Color(16, 185, 129);
            case ATTACCANTE -> new Color(239, 68, 68);
            default -> new Color(99, 102, 241);
        };
    }

    private JButton button(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        return b;
    }

    /* -------------------- Pitch Panel (campo) -------------------- */

    private static class PitchPanel extends JPanel {
        private Rectangle fieldRect;

        PitchPanel() {
            setBackground(new Color(14, 98, 63));
        }

        Rectangle getFieldRect() { return fieldRect; }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // prato con bande
            Paint grass = new GradientPaint(0, 0, new Color(15, 122, 74), 0, h, new Color(10, 85, 54));
            g2.setPaint(grass);
            g2.fillRect(0, 0, w, h);

            // bande orizzontali
            int stripe = 32;
            g2.setColor(new Color(255,255,255,10));
            for (int y=0; y<h; y+=stripe*2) {
                g2.fillRect(0, y, w, stripe);
            }

            // rettangolo campo in margine
            int margin = 40;
            int fw = w - margin*2;
            int fh = h - margin*2;
            int fx = margin;
            int fy = margin;

            // mantieni rapporto 105x68 circa
            double target = 105.0/68.0;
            double current = (double) fw / fh;
            if (current > target) {
                fw = (int) (fh * target);
                fx = (w - fw)/2;
            } else {
                fh = (int) (fw / target);
                fy = (h - fh)/2;
            }

            fieldRect = new Rectangle(fx, fy, fw, fh);

            // area di gioco (cornice)
            g2.setColor(new Color(255, 255, 255, 190));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRect(fx, fy, fw, fh);

            // metà campo
            g2.drawLine(fx + fw/2, fy, fx + fw/2, fy + fh);

            // cerchio di centrocampo
            g2.drawOval(fx + fw/2 - 60, fy + fh/2 - 60, 120, 120);

            // aree di rigore e porte
            // sinistra
            g2.drawRect(fx, fy + (int)(fh*0.2), (int)(fw*0.18), (int)(fh*0.6));
            g2.drawRect(fx, fy + (int)(fh*0.35), (int)(fw*0.08), (int)(fh*0.3));
            // destra
            g2.drawRect(fx + fw - (int)(fw*0.18), fy + (int)(fh*0.2), (int)(fw*0.18), (int)(fh*0.6));
            g2.drawRect(fx + fw - (int)(fw*0.08), fy + (int)(fh*0.35), (int)(fw*0.08), (int)(fh*0.3));

            g2.dispose();

            // dimensiona preferito per permettere scroll se serve
            setPreferredSize(new Dimension(Math.max(900, w), Math.max(600, h)));
        }
    }

    private static class PlayerPicker extends JDialog {
        private Calciatore selected;

        PlayerPicker(Window parent, Calciatore.Ruolo ruolo, List<Calciatore> candidates) {
            super(parent, "Scegli " + ruolo.name(), ModalityType.APPLICATION_MODAL);
            setLayout(new BorderLayout());
            setMinimumSize(new Dimension(420, 520));
            setLocationRelativeTo(parent);

            JPanel top = new JPanel(new BorderLayout(8, 8));
            top.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
            JLabel lbl = new JLabel("Filtra per nome/cognome:");
            JTextField search = new JTextField();
            top.add(lbl, BorderLayout.WEST);
            top.add(search, BorderLayout.CENTER);

            DefaultListModel<Calciatore> model = new DefaultListModel<>();
            candidates.forEach(model::addElement);

            JList<Calciatore> list = new JList<>(model);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new DefaultListCellRenderer(){
                @Override public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    Calciatore cal = (Calciatore) value;
                    setText(cal.getNomeCompleto());
                    return c;
                }
            });

            JScrollPane sp = new JScrollPane(list);
            sp.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton ok = new JButton("Seleziona");
            JButton cancel = new JButton("Annulla");
            bottom.add(ok); bottom.add(cancel);

            add(top, BorderLayout.NORTH);
            add(sp, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            search.getDocument().addDocumentListener(new DocumentListener() {
                private void filter() {
                    String q = search.getText().trim().toLowerCase();
                    model.clear();
                    for (Calciatore c : candidates) {
                        String s = (c.getNomeCompleto() + " " + c.getRuolo().name()).toLowerCase();
                        if (s.contains(q)) model.addElement(c);
                    }
                }
                @Override public void insertUpdate(DocumentEvent e) { filter(); }
                @Override public void removeUpdate(DocumentEvent e) { filter(); }
                @Override public void changedUpdate(DocumentEvent e) { filter(); }
            });

            ok.addActionListener(e -> {
                selected = list.getSelectedValue();
                dispose();
            });
            cancel.addActionListener(e -> dispose());
        }

        Calciatore getSelected() { return selected; }
    }
}
