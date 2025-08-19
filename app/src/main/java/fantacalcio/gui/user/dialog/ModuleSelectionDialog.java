package fantacalcio.gui.user.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

/**
 * Dialog per la selezione del modulo di gioco
 */
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
        setMinimumSize(new Dimension(500, 400));
        
        createHeaderPanel();
        createModuliPanel();
        createButtonPanel();
        
        pack();
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 150, 243));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("⚽ Scegli il Modulo di Gioco", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Seleziona la formazione tattica per la tua squadra", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(200, 230, 255));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createModuliPanel() {
        JPanel moduliPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        moduliPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        ButtonGroup group = new ButtonGroup();
        
        for (String[] modulo : moduli) {
            JPanel moduloCard = createModuloCard(modulo[0], modulo[1], modulo[2]);
            
            JRadioButton radioButton = new JRadioButton();
            radioButton.setActionCommand(modulo[0]);
            radioButton.addActionListener(e -> moduloSelezionato = modulo[0]);
            
            group.add(radioButton);
            
            // Nascondi il radio button e usa il click sulla card
            moduloCard.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    radioButton.setSelected(true);
                    moduloSelezionato = modulo[0];
                    updateCardSelection(moduliPanel, moduloCard);
                }
                
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    moduloCard.setBackground(new Color(240, 248, 255));
                    moduloCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!radioButton.isSelected()) {
                        moduloCard.setBackground(Color.WHITE);
                    }
                }
            });
            
            moduliPanel.add(moduloCard);
        }
        
        add(moduliPanel, BorderLayout.CENTER);
    }
    
    private JPanel createModuloCard(String modulo, String tipo, String descrizione) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Header della card
        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setBackground(Color.WHITE);
        
        JLabel moduloLabel = new JLabel(modulo);
        moduloLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        moduloLabel.setForeground(new Color(33, 150, 243));
        moduloLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel tipoLabel = new JLabel(tipo);
        tipoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        tipoLabel.setForeground(getTipoColor(tipo));
        tipoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerCard.add(moduloLabel, BorderLayout.CENTER);
        headerCard.add(tipoLabel, BorderLayout.SOUTH);
        
        // Descrizione
        JLabel descLabel = new JLabel("<html><center>" + descrizione + "</center></html>");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(headerCard, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private Color getTipoColor(String tipo) {
        return switch (tipo) {
            case "Offensivo" -> new Color(244, 67, 54);
            case "Equilibrato" -> new Color(76, 175, 80);
            case "Difensivo", "Ultra Difensivo" -> new Color(63, 81, 181);
            default -> new Color(158, 158, 158);
        };
    }
    
    private void updateCardSelection(JPanel moduliPanel, JPanel selectedCard) {
        // Reset tutti i background
        for (int i = 0; i < moduliPanel.getComponentCount(); i++) {
            JPanel card = (JPanel) moduliPanel.getComponent(i);
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
        }
        
        // Evidenzia la card selezionata
        selectedCard.setBackground(new Color(227, 242, 253));
        selectedCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 3),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
    }
    
    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JButton btnConferma = new JButton("✅ Conferma Modulo");
        JButton btnAnnulla = new JButton("❌ Annulla");
        
        // Stile pulsanti
        btnConferma.setBackground(new Color(76, 175, 80));
        btnConferma.setForeground(Color.WHITE);
        btnConferma.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnConferma.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btnConferma.setFocusPainted(false);
        btnConferma.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnAnnulla.setBackground(new Color(158, 158, 158));
        btnAnnulla.setForeground(Color.WHITE);
        btnAnnulla.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnAnnulla.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnAnnulla.setFocusPainted(false);
        btnAnnulla.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Event listeners
        btnConferma.addActionListener(this::confermaModulo);
        btnAnnulla.addActionListener(e -> dispose());
        
        buttonPanel.add(btnConferma);
        buttonPanel.add(btnAnnulla);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void confermaModulo(ActionEvent e) {
        if (moduloSelezionato == null) {
            JLabel errorLabel = new JLabel("⚠️ Seleziona un modulo prima di continuare!");
            errorLabel.setForeground(Color.RED);
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Mostra errore temporaneo
            JPanel buttonPanel = (JPanel) ((JButton) e.getSource()).getParent();
            buttonPanel.add(errorLabel, 0);
            buttonPanel.revalidate();
            
            // Rimuovi errore dopo 2 secondi
            javax.swing.Timer timer = new javax.swing.Timer(2000, ev -> {
                buttonPanel.remove(errorLabel);
                buttonPanel.revalidate();
                buttonPanel.repaint();
            });
            timer.setRepeats(false);
            timer.start();
            
            return;
        }
        
        confirmed = true;
        dispose();
    }
    
    // Getters
    public String getModuloSelezionato() {
        return moduloSelezionato;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}