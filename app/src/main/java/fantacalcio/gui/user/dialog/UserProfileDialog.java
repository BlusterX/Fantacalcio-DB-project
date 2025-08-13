package fantacalcio.gui.user.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fantacalcio.model.Utente;

/**
 * Dialog per visualizzare le informazioni del profilo utente
 */
public class UserProfileDialog extends JDialog {
    
    private final Utente utente;
    
    public UserProfileDialog(Frame parent, Utente utente) {
        super(parent, "Il mio profilo", true);
        this.utente = utente;
        
        initializeGUI();
        setLocationRelativeTo(parent);
    }
    
    private void initializeGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        createComponents();
        
        pack();
    }
    
    private void createComponents() {
        setLayout(new BorderLayout());
        
        // Pannello principale
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);
        
        // Avatar e nome
        JPanel headerPanel = createHeaderPanel();
        
        // Informazioni dettagliate
        JPanel infoPanel = createInfoPanel();
        
        // Pulsante chiudi
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnChiudi = new JButton("Chiudi");
        btnChiudi.setBackground(new Color(33, 150, 243));
        btnChiudi.setForeground(Color.WHITE);
        btnChiudi.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnChiudi.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnChiudi.setFocusPainted(false);
        btnChiudi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChiudi.addActionListener(e -> dispose());
        
        buttonPanel.add(btnChiudi);
        
        // Assembla layout
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        
        // Avatar (emoji)
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 80));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Nome completo
        JLabel nomeLabel = new JLabel(utente.getNomeCompleto());
        nomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        nomeLabel.setForeground(new Color(33, 150, 243));
        nomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Nickname
        JLabel nicknameLabel = new JLabel("@" + utente.getNickname());
        nicknameLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
        nicknameLabel.setForeground(new Color(100, 100, 100));
        nicknameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(avatarLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(nomeLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(nicknameLabel);
        
        return headerPanel;
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informazioni Account"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Email
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblEmailTitolo = new JLabel("📧 Email:");
        lblEmailTitolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        infoPanel.add(lblEmailTitolo, gbc);
        
        gbc.gridx = 1;
        JLabel lblEmail = new JLabel(utente.getEmail());
        lblEmail.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        infoPanel.add(lblEmail, gbc);
        
        // Telefono
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblTelefonoTitolo = new JLabel("📱 Telefono:");
        lblTelefonoTitolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        infoPanel.add(lblTelefonoTitolo, gbc);
        
        gbc.gridx = 1;
        JLabel lblTelefono = new JLabel(utente.getNumero());
        lblTelefono.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        infoPanel.add(lblTelefono, gbc);
        
        // Data di nascita ed età
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblDataTitolo = new JLabel("🎂 Data di nascita:");
        lblDataTitolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        infoPanel.add(lblDataTitolo, gbc);
        
        gbc.gridx = 1;
        String dataNascitaStr = utente.getDataDiNascita().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel lblData = new JLabel(dataNascitaStr + " (" + utente.getEta() + " anni)");
        lblData.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        infoPanel.add(lblData, gbc);
        
        // ID Utente (per debug/supporto)
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblIdTitolo = new JLabel("🆔 ID Utente:");
        lblIdTitolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        lblIdTitolo.setForeground(new Color(150, 150, 150));
        infoPanel.add(lblIdTitolo, gbc);
        
        gbc.gridx = 1;
        JLabel lblId = new JLabel("#" + utente.getIdUtente());
        lblId.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lblId.setForeground(new Color(150, 150, 150));
        infoPanel.add(lblId, gbc);
        
        return infoPanel;
    }
}