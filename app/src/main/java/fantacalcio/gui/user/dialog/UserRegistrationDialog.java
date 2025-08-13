package fantacalcio.gui.user.dialog;

import fantacalcio.dao.UtenteDAO;
import fantacalcio.model.Utente;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

/**
 * Dialog per la registrazione di nuovi utenti
 */
public class UserRegistrationDialog extends JDialog {
    
    private final UtenteDAO utenteDAO;
    private boolean registrationSuccessful = false;
    private String registeredNickname = "";
    
    // Componenti GUI
    private JTextField txtNome, txtCognome, txtNickname, txtEmail, txtNumero;
    private JPasswordField txtPassword, txtConfirmPassword;
    private DatePicker datePicker;
    private JButton btnRegistrati, btnAnnulla;
    private JLabel lblStatus;
    
    public UserRegistrationDialog(Frame parent, UtenteDAO utenteDAO) {
        super(parent, "Registrazione Nuovo Utente", true);
        this.utenteDAO = utenteDAO;
        
        initializeGUI();
        setLocationRelativeTo(parent);
    }
    
    private void initializeGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        createComponents();
        setupLayout();
        setupEventListeners();
        
        pack();
    }
    
    private void createComponents() {
        // Campi di input
        txtNome = new JTextField(20);
        txtCognome = new JTextField(20);
        txtNickname = new JTextField(20);
        txtEmail = new JTextField(25);
        txtNumero = new JTextField(15);
        txtPassword = new JPasswordField(20);
        txtConfirmPassword = new JPasswordField(20);
        
        // Date picker per data di nascita
        datePicker = new DatePicker();
        datePicker.setDate(LocalDate.now().minusYears(20)); // Default a 20 anni fa
        
        // Pulsanti
        btnRegistrati = new JButton("Registrati");
        btnAnnulla = new JButton("Annulla");
        
        // Label di stato
        lblStatus = new JLabel(" ");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Stile componenti
        styleComponents();
        
        // Tooltip informativi
        txtNickname.setToolTipText("Il nickname deve essere unico");
        txtEmail.setToolTipText("Inserisci un indirizzo email valido");
        txtNumero.setToolTipText("Numero di telefono (opzionale)");
        txtPassword.setToolTipText("Scegli una password sicura");
        txtConfirmPassword.setToolTipText("Ripeti la stessa password");
        datePicker.setToolTipText("Devi avere almeno 13 anni per registrarti");
    }
    
    private void styleComponents() {
        // Stile pulsanti
        btnRegistrati.setBackground(new Color(76, 175, 80));
        btnRegistrati.setForeground(Color.WHITE);
        btnRegistrati.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnRegistrati.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnRegistrati.setFocusPainted(false);
        btnRegistrati.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnAnnulla.setBackground(new Color(158, 158, 158));
        btnAnnulla.setForeground(Color.WHITE);
        btnAnnulla.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btnAnnulla.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnAnnulla.setFocusPainted(false);
        btnAnnulla.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Pannello principale
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);
        
        // Titolo
        JLabel titleLabel = new JLabel("Registrazione Nuovo Utente");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 150, 243));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Form
        JPanel formPanel = createFormPanel();
        
        // Pulsanti
        JPanel buttonPanel = createButtonPanel();
        
        // Assembla layout
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(lblStatus);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Prima riga: Nome e Cognome
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNome, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Cognome:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtCognome, gbc);
        
        // Seconda riga: Nickname
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nickname:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        formPanel.add(txtNickname, gbc);
        gbc.gridwidth = 1; // Reset
        
        // Terza riga: Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        formPanel.add(txtEmail, gbc);
        gbc.gridwidth = 1; // Reset
        
        // Quarta riga: Numero e Data di nascita
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Telefono:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNumero, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Data nascita:"), gbc);
        gbc.gridx = 3;
        formPanel.add(datePicker, gbc);
        
        // Quinta riga: Password
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Conferma:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtConfirmPassword, gbc);
        
        return formPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        buttonPanel.add(btnRegistrati);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(btnAnnulla);
        
        return buttonPanel;
    }
    
    private void setupEventListeners() {
        btnRegistrati.addActionListener(this::registratiAction);
        btnAnnulla.addActionListener(this::annullaAction);
        
        // Enter per conferma
        getRootPane().setDefaultButton(btnRegistrati);
    }
    
    private void registratiAction(ActionEvent e) {
        eseguiRegistrazione();
    }
    
    private void annullaAction(ActionEvent e) {
        dispose();
    }
    
    /**
     * Esegue la registrazione del nuovo utente
     */
    private void eseguiRegistrazione() {
        try {
            // Validazione e creazione utente
            Utente nuovoUtente = validaECreaUtente();
            
            // Disabilita pulsanti durante la registrazione
            setButtonsEnabled(false);
            mostraStatus("Registrazione in corso...", Color.BLUE);
            
            // Usa SwingWorker per non bloccare l'interfaccia
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return utenteDAO.inserisciUtente(nuovoUtente);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean successo = get();
                        
                        if (successo) {
                            mostraSuccesso("Registrazione completata con successo!");
                            registrationSuccessful = true;
                            registeredNickname = nuovoUtente.getNickname();
                            
                            // Chiudi dialog dopo 2 secondi
                            Timer timer = new Timer(2000, ev -> dispose());
                            timer.setRepeats(false);
                            timer.start();
                            
                        } else {
                            mostraErrore("Errore durante la registrazione. Nickname o email già esistenti?");
                            setButtonsEnabled(true);
                        }
                        
                    } catch (Exception ex) {
                        mostraErrore("Errore durante la registrazione: " + ex.getMessage());
                        setButtonsEnabled(true);
                    }
                }
            };
            
            worker.execute();
            
        } catch (IllegalArgumentException ex) {
            mostraErrore(ex.getMessage());
        }
    }
    
    /**
     * Valida i campi e crea un nuovo oggetto Utente
     */
    private Utente validaECreaUtente() throws IllegalArgumentException {
        String nome = txtNome.getText().trim();
        String cognome = txtCognome.getText().trim();
        String nickname = txtNickname.getText().trim();
        String email = txtEmail.getText().trim();
        String numero = txtNumero.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        LocalDate dataNascita = datePicker.getDate();
        
        // Validazioni
        if (nome.isEmpty() || cognome.isEmpty() || nickname.isEmpty() || 
            email.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Tutti i campi obbligatori devono essere compilati!");
        }
        
        if (nickname.length() < 3) {
            throw new IllegalArgumentException("Il nickname deve essere di almeno 3 caratteri!");
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Inserisci un indirizzo email valido!");
        }
        
        if (password.length() < 6) {
            throw new IllegalArgumentException("La password deve essere di almeno 6 caratteri!");
        }
        
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Le password non corrispondono!");
        }
        
        if (dataNascita == null) {
            throw new IllegalArgumentException("Seleziona una data di nascita valida!");
        }
        
        if (dataNascita.isAfter(LocalDate.now().minusYears(13))) {
            throw new IllegalArgumentException("Devi avere almeno 13 anni per registrarti!");
        }
        
        if (dataNascita.isBefore(LocalDate.now().minusYears(100))) {
            throw new IllegalArgumentException("Data di nascita non valida!");
        }
        
        // Se il numero è vuoto, imposta un valore di default
        if (numero.isEmpty()) {
            numero = "N/A";
        }
        
        return new Utente(nome, cognome, nickname, email, numero, dataNascita, password);
    }
    
    /**
     * Utility methods per UI
     */
    private void setButtonsEnabled(boolean enabled) {
        btnRegistrati.setEnabled(enabled);
        btnAnnulla.setEnabled(enabled);
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
    
    // Getters per il parent
    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }
    
    public String getRegisteredNickname() {
        return registeredNickname;
    }
}