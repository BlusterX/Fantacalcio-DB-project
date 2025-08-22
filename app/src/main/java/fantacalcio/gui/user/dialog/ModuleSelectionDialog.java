package fantacalcio.gui.user.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/** Dialog minimale per la selezione del modulo di gioco */
public class ModuleSelectionDialog extends JDialog {

    private String moduloSelezionato = null;
    private boolean confirmed = false;

    // Moduli disponibili con descrizioni
    private final String[][] moduli = {
        {"3-4-3", "Offensivo", "3 Difensori, 4 Centrocampisti, 3 Attaccanti"},
        {"3-5-2", "Equilibrato", "3 Difensori, 5 Centrocampisti, 2 Attaccanti"},
        {"4-3-3", "Classico", "4 Difensori, 3 Centrocampisti, 3 Attaccanti"},
        {"4-4-2", "Tradizionale", "4 Difensori, 4 Centrocampisti, 2 Attaccanti"},
        {"4-5-1", "Difensivo", "4 Difensori, 5 Centrocampisti, 1 Attaccante"},
        {"5-3-2", "Ultra Difensivo", "5 Difensori, 3 Centrocampisti, 2 Attaccanti"}
    };

    public ModuleSelectionDialog(Window parent) {
        super(parent, "Scegli il Modulo", ModalityType.APPLICATION_MODAL);
        initializeGUI();
        setLocationRelativeTo(parent);
    }

    private void initializeGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(420, 320));

        add(createHeader(), BorderLayout.NORTH);
        add(createList(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);

        pack();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        JLabel title = new JLabel("Seleziona il modulo", SwingConstants.LEFT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel createList() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        ButtonGroup group = new ButtonGroup();

        for (String[] m : moduli) {
            String modulo = m[0], tipo = m[1], desc = m[2];
            JPanel row = createRow(modulo, tipo, desc);
            JRadioButton rb = (JRadioButton) row.getClientProperty("rb");

            group.add(rb);
            container.add(row);
            container.add(Box.createVerticalStrut(6));
        }
        container.add(Box.createVerticalGlue());
        return container;
    }

    private JPanel createRow(String modulo, String tipo, String descrizione) {
        JPanel row = new JPanel();
        row.setLayout(new BorderLayout(8, 2));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 225, 225)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Radio + titolo modulo
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);

        JRadioButton rb = new JRadioButton(modulo);
        rb.setOpaque(false);
        rb.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        rb.setActionCommand(modulo);
        rb.addActionListener(e -> {
            moduloSelezionato = modulo;
            highlightSelected(row, true);
        });

        left.add(rb, BorderLayout.WEST);

        // Destra: tipo + descrizione
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel tipoLbl = new JLabel(tipo);
        tipoLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tipoLbl.setForeground(new Color(50, 63, 75));

        JLabel descLbl = new JLabel(descrizione);
        descLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        descLbl.setForeground(new Color(120, 120, 120));

        right.add(tipoLbl);
        right.add(descLbl);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);

        // Interazione: click su tutta la riga seleziona il radio
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                rb.setSelected(true);
                moduloSelezionato = modulo;
                highlightSelected(row, true);
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!rb.isSelected()) highlightSelected(row, false);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (!rb.isSelected()) resetRowLook(row);
            }
        });

        // metto il riferimento al radio nella riga (comodo per l'highlight)
        row.putClientProperty("rb", rb);
        return row;
    }

    private void highlightSelected(JPanel row, boolean selected) {
        if (selected) {
            row.setBackground(new Color(235, 244, 255));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        } else {
            row.setBackground(new Color(245, 248, 252));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        }
        row.revalidate();
        row.repaint();
    }

    private void resetRowLook(JPanel row) {
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 225, 225)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        row.revalidate();
        row.repaint();
    }

    private JPanel createButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));

        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(new Color(200, 0, 0));
        errorLabel.setVisible(false);

        JButton btnConferma = new JButton("Conferma");
        JButton btnAnnulla = new JButton("Annulla");

        btnConferma.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAnnulla.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnConferma.addActionListener((ActionEvent e) -> {
            if (moduloSelezionato == null) {
                errorLabel.setText("Seleziona un modulo.");
                errorLabel.setVisible(true);
                // nasconde il messaggio dopo 2s
                Timer t = new Timer(2000, ev -> { errorLabel.setVisible(false); });
                t.setRepeats(false);
                t.start();
                return;
            }
            confirmed = true;
            dispose();
        });

        btnAnnulla.addActionListener(e -> dispose());

        buttonPanel.add(errorLabel);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(btnAnnulla);
        buttonPanel.add(btnConferma);
        return buttonPanel;
    }

    // Getters
    public String getModuloSelezionato() { return moduloSelezionato; }
    public boolean isConfirmed() { return confirmed; }
}
