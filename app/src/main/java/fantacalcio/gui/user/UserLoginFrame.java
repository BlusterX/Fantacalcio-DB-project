package fantacalcio.gui.user;

import fantacalcio.dao.UtenteDAO;
import fantacalcio.gui.user.dialog.UserRegistrationDialog;
import fantacalcio.model.Utente;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;

/**
 * Finestra di login per gli utenti del fantacalcio
 */
public class UserLoginFrame extends JFrame {
    
    private final UtenteDAO utenteDAO;
    
    // Componenti GUI
    private JTextField txtNickname;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegistrati, btnAdmin;
    private JLabel lblStatus;
    
    public UserLoginFrame() {
        this.utenteDAO = new UtenteDAO();
        
        initializeGUI();
        setLocationRelativeTo(null);
    }
    
    private void initializeGUI() {
        setTitle("FantaCalcio - Accesso Utente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        createComponents();
        setupLayout();
        setupEventListeners();
        
        pack();
    }
    
    private void createComponents() {
        // Campi di input
        txtNickname = new JTextField(20);
        txtPassword = new JPasswordField(20);
        
        // Pulsanti
        btnLogin = new JButton("Accedi");
        btnRegistrati = new JButton("Registrati");
        btnAdmin = new JButton("Admin Panel");
        
        // Label di stato
        lblStatus = new JLabel(" ");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Stile componenti
        styleComponents();
    }
    
    private void styleComponents() {
        // Stile pulsanti
        btnLogin.setBackground(new Color(76, 175, 80));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnRegistrati.setBackground(new Color(33, 150, 243));
        btnRegistrati.setForeground(Color.WHITE);
        btnRegistrati.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnRegistrati.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnRegistrati.setFocusPainted(false);
        btnRegistrati.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnAdmin.setBackground(new Color(158, 158, 158));
        btnAdmin.setForeground(Color.WHITE);
        btnAdmin.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        btnAdmin.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnAdmin.setFocusPainted(false);
        btnAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Stile campi di input
        txtNickname.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        txtPassword.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        // Placeholder effect
        txtNickname.setToolTipText("Inserisci il tuo nickname");
        txtPassword.setToolTipText("Inserisci la tua password");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Pannello principale
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);
        
        // Logo/Titolo
        JLabel titleLabel = new JLabel("🏆 FantaCalcio");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        titleLabel.setForeground(new Color(33, 150, 243));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Crea la tua squadra dei sogni");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Pannello form
        JPanel formPanel = createFormPanel();
        
        // Pannello pulsanti
        JPanel buttonPanel = createButtonPanel();
        
        // Pannello admin (piccolo, in basso)
        JPanel adminPanel = new JPanel(new FlowLayout());
        adminPanel.setBackground(Color.WHITE);
        adminPanel.add(btnAdmin);
        
        // Assembla layout
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(lblStatus);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(adminPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Nickname
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblNickname = new JLabel("Nickname:");
        lblNickname.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        formPanel.add(lblNickname, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtNickname, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        formPanel.add(lblPassword, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtPassword, gbc);
        
        return formPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        buttonPanel.add(btnLogin);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(btnRegistrati);
        
        return buttonPanel;
    }
    
    private void setupEventListeners() {
        // Enter key per login
        KeyListener enterKeyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    eseguiLogin();
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {}
            
            @Override
            public void keyTyped(KeyEvent e) {}
        };
        
        txtNickname.addKeyListener(enterKeyListener);
        txtPassword.addKeyListener(enterKeyListener);
        
        // Event listeners pulsanti
        btnLogin.addActionListener(this::loginAction);
        btnRegistrati.addActionListener(this::registratiAction);
        btnAdmin.addActionListener(this::adminAction);
    }
    
    private void loginAction(ActionEvent e) {
        eseguiLogin();
    }
    
    private void registratiAction(ActionEvent e) {
        apriDialogRegistrazione();
    }
    
    private void adminAction(ActionEvent e) {
        apriAdminPanel();
    }
    
    /**
     * Esegue il login dell'utente
     */
    private void eseguiLogin() {
        String nickname = txtNickname.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Validazioni base
        if (nickname.isEmpty() || password.isEmpty()) {
            mostraErrore("Inserisci nickname e password!");
            return;
        }
        
        // Disabilita pulsanti durante il login
        setButtonsEnabled(false);
        mostraStatus("Accesso in corso...", Color.BLUE);
        
        // Usa SwingWorker per non bloccare l'interfaccia
        SwingWorker<Optional<Utente>, Void> worker = new SwingWorker<Optional<Utente>, Void>() {
            @Override
            protected Optional<Utente> doInBackground() throws Exception {
                return utenteDAO.verificaLogin(nickname, password);
            }
            
            @Override
            protected void done() {
                try {
                    Optional<Utente> utente = get();
                    
                    if (utente.isPresent()) {
                        mostraSuccesso("Accesso effettuato con successo!");
                        
                        // Piccola pausa per mostrare il messaggio
                        Timer timer = new Timer(1000, e -> {
                            apriInterfacciaUtente(utente.get());
                        });
                        timer.setRepeats(false);
                        timer.start();
                        
                    } else {
                        mostraErrore("Nickname o password errati!");
                        setButtonsEnabled(true);
                        txtPassword.setText("");
                        txtNickname.requestFocus();
                    }
                    
                } catch (Exception ex) {
                    mostraErrore("Errore durante l'accesso: " + ex.getMessage());
                    setButtonsEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Apre il dialog di registrazione
     */
    private void apriDialogRegistrazione() {
        UserRegistrationDialog dialog = new UserRegistrationDialog(this, utenteDAO);
        dialog.setVisible(true);
        
        // Se la registrazione è andata a buon fine, popola i campi
        if (dialog.isRegistrationSuccessful()) {
            txtNickname.setText(dialog.getRegisteredNickname());
            txtPassword.requestFocus();
            mostraSuccesso("Registrazione completata! Ora puoi effettuare l'accesso.");
        }
    }
    
    /**
     * Apre l'admin panel
     */
    private void apriAdminPanel() {
        try {
            // Importa e crea MainFrame admin
            fantacalcio.gui.MainFrame adminFrame = new fantacalcio.gui.MainFrame();
            adminFrame.setVisible(true);
            
            // Chiudi questa finestra
            dispose();
            
        } catch (Exception ex) {
            mostraErrore("Errore apertura admin panel: " + ex.getMessage());
        }
    }
    
    /**
     * Apre l'interfaccia principale dell'utente
     */
    private void apriInterfacciaUtente(Utente utente) {
        try {
            UserMainFrame userFrame = new UserMainFrame(utente);
            userFrame.setVisible(true);
            
            // Chiudi questa finestra
            dispose();
            
        } catch (Exception ex) {
            mostraErrore("Errore apertura interfaccia utente: " + ex.getMessage());
            setButtonsEnabled(true);
        }
    }
    
    /**
     * Utility methods per UI
     */
    private void setButtonsEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnRegistrati.setEnabled(enabled);
        txtNickname.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
    }
    
    private void mostraStatus(String messaggio, Color colore) {
        lblStatus.setText(messaggio);
        lblStatus.setForeground(colore);
    }
    
    private void mostraErrore(String messaggio) {
        mostraStatus("❌ " + messaggio, Color.RED);
    }
    
    private void mostraSuccesso(String messaggio) {
        mostraStatus("✅ " + messaggio, new Color(76, 175, 80));
    }
    
    /**
     * Main method per testare la finestra
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getLookAndFeel());
                } catch (Exception e) {
                    System.out.println("Impossibile impostare il Look and Feel del sistema");
                }
                
                new UserLoginFrame().setVisible(true);
            }
        });
    }
}