package fantacalcio.gui.admin.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.UtenteDAO;
import fantacalcio.model.Utente;

public class UtentiPanel extends JPanel {
    
    private final JFrame parentFrame;
    private final UtenteDAO utenteDAO;
    
    // Componenti GUI
    private JTable tabellaUtenti;
    private DefaultTableModel modelTabella;
    private JTextField txtNome, txtCognome, txtNickname, txtEmail, txtNumero, txtDataNascita, txtPassword;
    private JButton btnAggiungi, btnModifica, btnElimina, btnPulisci;
    
    public UtentiPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.utenteDAO = new UtenteDAO();
        
        initializeComponents();
        setupLayout();
        loadData();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        createTable();
        createForm();
        createButtons();
    }
    
    private void createTable() {
        String[] colonne = {"ID", "Nome", "Cognome", "Nickname", "Email", "Telefono", "Data Nascita"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaUtenti = new JTable(modelTabella);
        tabellaUtenti.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaUtenti.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                popolaCampiDaTabella();
            }
        });
        
        // Stile tabella
        tabellaUtenti.setRowHeight(25);
        tabellaUtenti.getTableHeader().setBackground(new Color(63, 81, 181));
        tabellaUtenti.getTableHeader().setForeground(Color.WHITE);
        tabellaUtenti.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    }
    
    private void createForm() {
        txtNome = new JTextField(15);
        txtCognome = new JTextField(15);
        txtNickname = new JTextField(15);
        txtEmail = new JTextField(20);
        txtNumero = new JTextField(15);
        txtDataNascita = new JTextField(10);
        txtPassword = new JPasswordField(15);
        
        txtDataNascita.setToolTipText("Formato: YYYY-MM-DD (es: 1990-05-15)");
    }
    
    private void createButtons() {
        btnAggiungi = new JButton("Aggiungi");
        btnModifica = new JButton("Modifica");
        btnElimina = new JButton("Elimina");
        btnPulisci = new JButton("Pulisci");
        
        // Stile pulsanti
        styleButton(btnAggiungi, new Color(76, 175, 80));
        styleButton(btnModifica, new Color(255, 152, 0));
        styleButton(btnElimina, new Color(244, 67, 54));
        styleButton(btnPulisci, new Color(158, 158, 158));
        
        // Event listeners
        btnAggiungi.addActionListener(this::aggiungiUtente);
        btnModifica.addActionListener(this::modificaUtente);
        btnElimina.addActionListener(this::eliminaUtente);
        btnPulisci.addActionListener(e -> pulisciCampi());
    }
    
    private void setupLayout() {
        // Pannello tabella
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Lista Utenti:"), BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(tabellaUtenti);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        topPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Pannello form
        JPanel formPanel = createFormLayout();
        
        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnAggiungi);
        buttonPanel.add(btnModifica);
        buttonPanel.add(btnElimina);
        buttonPanel.add(btnPulisci);
        
        // Pannello inferiore
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JLabel("Inserimento/Modifica Utente:"), BorderLayout.NORTH);
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Layout finale
        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFormLayout() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Prima riga
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNome, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Cognome:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtCognome, gbc);
        
        // Seconda riga
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nickname:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNickname, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtEmail, gbc);
        
        // Terza riga
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Telefono:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNumero, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Data Nascita:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtDataNascita, gbc);
        
        // Quarta riga
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);
        
        return formPanel;
    }
    
    public void loadData() {
        modelTabella.setRowCount(0);
        
        List<Utente> utenti = utenteDAO.trovaTuttiGliUtenti();
        
        for (Utente utente : utenti) {
            Object[] riga = {
                utente.getIdUtente(),
                utente.getNome(),
                utente.getCognome(),
                utente.getNickname(),
                utente.getEmail(),
                utente.getNumero(),
                utente.getDataDiNascita().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            };
            modelTabella.addRow(riga);
        }
    }
    
    private void popolaCampiDaTabella() {
        int rigaSelezionata = tabellaUtenti.getSelectedRow();
        if (rigaSelezionata >= 0) {
            int idUtente = (Integer) modelTabella.getValueAt(rigaSelezionata, 0);
            
            utenteDAO.trovaUtentePerId(idUtente).ifPresent(utente -> {
                txtNome.setText(utente.getNome());
                txtCognome.setText(utente.getCognome());
                txtNickname.setText(utente.getNickname());
                txtEmail.setText(utente.getEmail());
                txtNumero.setText(utente.getNumero());
                txtDataNascita.setText(utente.getDataDiNascita().toString());
                txtPassword.setText(utente.getPassword());
            });
        }
    }
    
    private void aggiungiUtente(ActionEvent e) {
        try {
            Utente nuovoUtente = creaUtenteDaCampi();
            
            if (utenteDAO.inserisciUtente(nuovoUtente)) {
                JOptionPane.showMessageDialog(this, 
                    "Utente aggiunto con successo!", 
                    "Successo", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
                pulisciCampi();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Errore durante l'inserimento.\nControlla che nickname ed email siano unici.", 
                    "Errore", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Errore: " + ex.getMessage(), 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void modificaUtente(ActionEvent e) {
        int rigaSelezionata = tabellaUtenti.getSelectedRow();
        if (rigaSelezionata < 0) {
            JOptionPane.showMessageDialog(this, 
                "Seleziona un utente dalla tabella per modificarlo.", 
                "Attenzione", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int idUtente = (Integer) modelTabella.getValueAt(rigaSelezionata, 0);
            Utente utenteDaModificare = creaUtenteDaCampi();
            utenteDaModificare.setIdUtente(idUtente);
            
            if (utenteDAO.aggiornaUtente(utenteDaModificare)) {
                JOptionPane.showMessageDialog(this, 
                    "Utente modificato con successo!", 
                    "Successo", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
                pulisciCampi();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Errore durante la modifica.", 
                    "Errore", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Errore: " + ex.getMessage(), 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminaUtente(ActionEvent e) {
        int rigaSelezionata = tabellaUtenti.getSelectedRow();
        if (rigaSelezionata < 0) {
            JOptionPane.showMessageDialog(this, 
                "Seleziona un utente dalla tabella per eliminarlo.", 
                "Attenzione", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int conferma = JOptionPane.showConfirmDialog(this, 
            "Sei sicuro di voler eliminare questo utente?\nQuesta azione eliminerà anche tutte le sue squadre!", 
            "Conferma Eliminazione", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (conferma == JOptionPane.YES_OPTION) {
            int idUtente = (Integer) modelTabella.getValueAt(rigaSelezionata, 0);
            
            if (utenteDAO.eliminaUtente(idUtente)) {
                JOptionPane.showMessageDialog(this, 
                    "Utente eliminato con successo!", 
                    "Successo", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
                pulisciCampi();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Errore durante l'eliminazione.", 
                    "Errore", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private Utente creaUtenteDaCampi() throws IllegalArgumentException {
        String nome = txtNome.getText().trim();
        String cognome = txtCognome.getText().trim();
        String nickname = txtNickname.getText().trim();
        String email = txtEmail.getText().trim();
        String numero = txtNumero.getText().trim();
        String dataString = txtDataNascita.getText().trim();
        String password = txtPassword.getText().trim();
        
        // Validazioni
        if (nome.isEmpty() || cognome.isEmpty() || nickname.isEmpty() || 
            email.isEmpty() || dataString.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Tutti i campi obbligatori devono essere compilati!");
        }
        
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email non valida!");
        }
        
        LocalDate dataNascita;
        try {
            dataNascita = LocalDate.parse(dataString);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Data di nascita non valida! Usa il formato YYYY-MM-DD");
        }
        
        if (dataNascita.isAfter(LocalDate.now().minusYears(13))) {
            throw new IllegalArgumentException("L'utente deve avere almeno 13 anni!");
        }
        
        return new Utente(nome, cognome, nickname, email, numero, dataNascita, password);
    }
    
    private void pulisciCampi() {
        txtNome.setText("");
        txtCognome.setText("");
        txtNickname.setText("");
        txtEmail.setText("");
        txtNumero.setText("");
        txtDataNascita.setText("");
        txtPassword.setText("");
        tabellaUtenti.clearSelection();
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    public int getNumUtenti() {
        return utenteDAO.contaUtenti();
    }
}