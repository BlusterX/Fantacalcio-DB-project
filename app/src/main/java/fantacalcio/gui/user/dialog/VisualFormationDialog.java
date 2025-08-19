DROP DATABASE IF EXISTS fantacalcio; CREATE DATABASE fantacalcio; USE fantacalcio; CREATE TABLE UTENTE ( ID_Utente INT PRIMARY KEY AUTO_INCREMENT, Nome VARCHAR(50) NOT NULL, Cognome VARCHAR(50) NOT NULL, Nickname VARCHAR(30) NOT NULL UNIQUE, Email VARCHAR(100) NOT NULL UNIQUE,

pasted

BlusterX/Fantacalcio-DB-project
main

GITHUB

Ti ho passato il progetto e il db creato. Comunque d'ora in poi, apparte che non sia necessario, non mandarmi tutto il codice ma solo i pezzi da cambiare(se sono pochi).
Come posso continuare il progetto riguardo agli scontri tra gli utenti in una lega. Mi spiego, in questo momento una volta che sono entrati da 2 a 12 giocatori in una lega, l'admin della lega può decidere di startare la lega e dovrebbero così iniziare gli scontri(simulati), cioè ogni giocatore in base alla sua fascia, ha tot probabilità di segnare o fare assist, ecc... si sommano i punti di tutti i giocatori nella formazione di quella determinata giornata e si vede chi ha vinto tra le 2 squadre che si sono sfidate quella determinata giornata.
Non vorrei usare la tabella Voto_giornata e Punteggio_Giocatore, ma se sono indispensabili secondo te va bene, dimmi se le usi o meno...
Per il resto usa le tabelle che già ci sono.

Modifica
Analizzando il tuo progetto, vedo che hai già una buona struttura per gestire gli scontri tra squadre. Ti suggerisco di usare le tabelle esistenti senza Voto_Giornata e Punteggio_Giocatore, basandoti principalmente sulle fasce dei giocatori per il calcolo dei punteggi.

Ecco come implementare il sistema di scontri:

1. Modifica al DAO per avviare la lega

MatchSimulationService.java
Codice 
2. Aggiorna ScontroLegaDAO per gestire l'avvio automatico
Aggiungi questo metodo a ScontroLegaDAO.java:

java
/**
 * Avvia automaticamente una lega se ha abbastanza squadre complete
 */
public boolean tentaAvvioAutomaticoLega(int idLega) {
    if (!verificaAvvioLega(idLega)) {
        return false;
    }
    
    // Genera calendario
    List<Integer> squadreIds = getSquadreCompleteLega(idLega);
    if (!generaCalendario(idLega, squadreIds)) {
        return false;
    }
    
    // Avvia la lega
    return avviaLega(idLega);
}

/**
 * Simula una giornata specifica
 */
public void simulaGiornataLega(int idLega) {
    String statoLega = getStatoLega(idLega);
    if (!"IN_CORSO".equals(statoLega)) {
        System.out.println("La lega non è in corso - stato: " + statoLega);
        return;
    }
    
    int giornataCorrente = getGiornataCorrente(idLega);
    
    MatchSimulationService simulator = new MatchSimulationService();
    simulator.simulaGiornata(idLega, giornataCorrente);
    
    System.out.println("Giornata " + giornataCorrente + " simulata per lega " + idLega);
}
3. Aggiorna LeagueDetailPanel per mostrare pulsanti admin
Nel metodo createLegaInfoPanel() di LeagueDetailPanel.java, aggiungi:

java
// Se l'utente è admin della lega, mostra controlli aggiuntivi
if (isUserAdmin()) {
    JButton btnAvviaLega = new JButton("🚀 Avvia Lega");
    JButton btnSimulaGiornata = new JButton("⚽ Simula Giornata");
    
    styleButton(btnAvviaLega, new Color(76, 175, 80));
    styleButton(btnSimulaGiornata, new Color(255, 152, 0));
    
    btnAvviaLega.addActionListener(e -> avviaLega());
    btnSimulaGiornata.addActionListener(e -> simulaGiornata());
    
    // Abilita/disabilita in base allo stato
    btnAvviaLega.setEnabled("CREATA".equals(statoLega));
    btnSimulaGiornata.setEnabled("IN_CORSO".equals(statoLega));
    
    rightInfo.add(btnAvviaLega);
    rightInfo.add(Box.createHorizontalStrut(5));
    rightInfo.add(btnSimulaGiornata);
}

private boolean isUserAdmin() {
    // Verifica se l'utente corrente è admin della lega
    return legaDAO.trovaLeghePerAdmin(utenteCorrente.getIdUtente())
                  .stream()
                  .anyMatch(l -> l.getIdLega() == lega.getIdLega());
}

private void avviaLega() {
    int conferma = JOptionPane.showConfirmDialog(this,
        "Avviare la lega '" + lega.getNome() + "'?\n\n" +
        "Questo genererà automaticamente il calendario degli scontri.",
        "Conferma Avvio Lega",
        JOptionPane.YES_NO_OPTION);
    
    if (conferma == JOptionPane.YES_OPTION) {
        if (scontroDAO.tentaAvvioAutomaticoLega(lega.getIdLega())) {
            JOptionPane.showMessageDialog(this,
                "Lega avviata con successo!\nGli scontri inizieranno a breve.",
                "Lega Avviata",
                JOptionPane.INFORMATION_MESSAGE);
            // Refresh della vista
            SwingUtilities.invokeLater(() -> {
                // Ricarica il panel
            });
        } else {
            JOptionPane.showMessageDialog(this,
                "Impossibile avviare la lega.\nVerifica che ci siano abbastanza squadre complete.",
                "Errore",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void simulaGiornata() {
    int conferma = JOptionPane.showConfirmDialog(this,
        "Simulare la giornata corrente?\n\n" +
        "Questo calcolerà i risultati di tutte le partite.",
        "Simula Giornata",
        JOptionPane.YES_NO_OPTION);
    
    if (conferma == JOptionPane.YES_OPTION) {
        scontroDAO.simulaGiornataLega(lega.getIdLega());
        JOptionPane.showMessageDialog(this,
            "Giornata simulata con successo!",
            "Simulazione Completata",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
Come funziona:
Avvio Lega: L'admin può avviare la lega quando ha almeno 2-12 squadre complete
Generazione Calendario: Crea automaticamente gli scontri round-robin
Simulazione: Ogni giornata, calcola i punteggi basandosi su:
Formazione schierata (solo titolari contano)
Fascia di ogni giocatore (probabilità diverse di eventi)
Eventi random: gol, assist, ammonizioni, espulsioni, imbattibilità
Calcolo Punteggio:
Voto base random (4.5-8.5)
Bonus gol/assist basati su fascia e ruolo
Malus per cartellini
Non ho usato Voto_Giornata e Punteggio_Giocatore come richiesto - tutto si basa sulle fasce esistenti e calcoli in tempo reale.

Vuoi che implementi anche una classifica automatica che si aggiorna dopo ogni giornata?




Riguardo al punto 2 non voglio che la lega si avvia automaticamente, deve essere l'admin che decide di avviarla, perchè dopo nessuno potrà entrare ne uscire dalla lega. E simulare una giornata specifica non mi sembra avere tanto senso anche perchè le partite dovranno essere simulate di fila(dalla 1 alla 38esima).
Riguardo al punto 3, vorrei che l'admin può avviare la lega solo tramite il pannel admin(aggiungi lì un bottone). Il punto 1 ho aggiunto il codice come hai detto

Modifica
Hai ragione, modifico l'approccio. Ecco le correzioni:

1. Rimuovi il metodo di avvio automatico da ScontroLegaDAO
Sostituisci il metodo tentaAvvioAutomaticoLega con:

java
/**
 * Avvia una lega manualmente (solo admin)
 */
public boolean avviaLegaManualmente(int idLega) {
    // Verifica prerequisiti
    if (!verificaAvvioLega(idLega)) {
        return false;
    }
    
    // Genera calendario completo (tutte le 38 giornate se necessario)
    List<Integer> squadreIds = getSquadreCompleteLega(idLega);
    if (!generaCalendarioCompleto(idLega, squadreIds)) {
        return false;
    }
    
    // Avvia la lega
    return avviaLega(idLega);
}

/**
 * Genera calendario completo per tutte le giornate
 */
private boolean generaCalendarioCompleto(int idLega, List<Integer> squadreIds) {
    if (squadreIds.size() % 2 != 0 || squadreIds.size() < 2) {
        return false;
    }
    
    String sql = "INSERT INTO SCONTRO_LEGA (ID_Lega, ID_Squadra_1, ID_Squadra_2, Giornata) VALUES (?, ?, ?, ?)";
    
    try (Connection conn = dbConnection.getConnection()) {
        conn.setAutoCommit(false);
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int numSquadre = squadreIds.size();
            int numGiornate = (numSquadre - 1) * 2; // Andata e ritorno
            
            // Andata
            for (int giornata = 1; giornata <= numSquadre - 1; giornata++) {
                generaPartiteGiornata(stmt, idLega, squadreIds, giornata);
            }
            
            // Ritorno (squadre invertite)
            for (int giornata = numSquadre; giornata <= numGiornate; giornata++) {
                int giornataAndata = giornata - (numSquadre - 1);
                generaPartiteGiornataInvertite(stmt, idLega, squadreIds, giornata, giornataAndata);
            }
            
            stmt.executeBatch();
            conn.commit();
            
            System.out.println("Calendario completo generato: " + numGiornate + " giornate");
            return true;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
        
    } catch (SQLException e) {
        System.err.println("Errore generazione calendario: " + e.getMessage());
        return false;
    }
}

/**
 * Simula tutte le giornate in sequenza
 */
public void simulaTutteLeGiornate(int idLega) {
    String statoLega = getStatoLega(idLega);
    if (!"IN_CORSO".equals(statoLega)) {
        System.out.println("La lega non è in corso");
        return;
    }
    
    MatchSimulationService simulator = new MatchSimulationService();
    int giornataCorrente = getGiornataCorrente(idLega);
    int maxGiornate = getMaxGiornate(idLega);
    
    // Simula dalla giornata corrente fino alla fine
    for (int giornata = giornataCorrente; giornata <= maxGiornate; giornata++) {
        simulator.simulaGiornata(idLega, giornata);
        aggiornaGiornataCorrente(idLega, giornata + 1);
        
        System.out.println("Simulata giornata " + giornata + "/" + maxGiornate);
    }
    
    // Termina la lega
    terminaLega(idLega);
}

private int getMaxGiornate(int idLega) {
    String sql = "SELECT MAX(Giornata) FROM SCONTRO_LEGA WHERE ID_Lega = ?";
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idLega);
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 38;
        }
    } catch (SQLException e) {
        return 38; // Default
    }
}

private void aggiornaGiornataCorrente(int idLega, int nuovaGiornata) {
    String sql = "UPDATE LEGA SET Giornata_Corrente = ? WHERE ID_Lega = ?";
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, nuovaGiornata);
        stmt.setInt(2, idLega);
        stmt.executeUpdate();
    } catch (SQLException e) {
        System.err.println("Errore aggiornamento giornata: " + e.getMessage());
    }
}

private void terminaLega(int idLega) {
    String sql = "UPDATE LEGA SET Stato = 'TERMINATA' WHERE ID_Lega = ?";
    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idLega);
        stmt.executeUpdate();
        System.out.println("Lega " + idLega + " terminata");
    } catch (SQLException e) {
        System.err.println("Errore terminazione lega: " + e.getMessage());
    }
}
2. Aggiungi pulsanti al pannello admin
In AdminLeaguePanel.java, modifica il metodo createRightPanel():

java
private JPanel createRightPanel() {
    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
    rightPanel.setBorder(BorderFactory.createTitledBorder("Statistiche e Azioni"));
    rightPanel.setPreferredSize(new Dimension(250, 0));
    
    // Statistiche esistenti
    JPanel statsPanel = createStatsPanel();
    
    // Separator
    JSeparator separator = new JSeparator();
    
    // NUOVO: Pannello gestione lega
    JPanel gestioneLegaPanel = createGestioneLegaPanel();
    
    // Pannello popolamento esistente
    JPanel populatePanel = createPopulatePanel();
    
    rightPanel.add(statsPanel);
    rightPanel.add(Box.createVerticalStrut(10));
    rightPanel.add(separator);
    rightPanel.add(Box.createVerticalStrut(10));
    rightPanel.add(gestioneLegaPanel);
    rightPanel.add(Box.createVerticalStrut(10));
    rightPanel.add(populatePanel);
    rightPanel.add(Box.createVerticalGlue());
    
    return rightPanel;
}

private JPanel createGestioneLegaPanel() {
    JPanel gestionePanel = new JPanel();
    gestionePanel.setLayout(new BoxLayout(gestionePanel, BoxLayout.Y_AXIS));
    gestionePanel.setBorder(BorderFactory.createTitledBorder("Gestione Lega"));
    
    JLabel lblInfo = new JLabel("Seleziona lega:");
    lblInfo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    
    JButton btnAvviaLega = new JButton("🚀 Avvia Lega");
    JButton btnSimulaCompleta = new JButton("⚽ Simula Stagione");
    
    styleButton(btnAvviaLega, new Color(76, 175, 80));
    styleButton(btnSimulaCompleta, new Color(255, 152, 0));
    
    btnAvviaLega.addActionListener(this::avviaLega);
    btnSimulaCompleta.addActionListener(this::simulaStagioneCompleta);
    
    gestionePanel.add(lblInfo);
    gestionePanel.add(Box.createVerticalStrut(5));
    gestionePanel.add(btnAvviaLega);
    gestionePanel.add(Box.createVerticalStrut(5));
    gestionePanel.add(btnSimulaCompleta);
    
    JLabel lblInfoGestione = new JLabel("<html><small>Avvia: genera calendario e inizia<br>Simula: calcola tutti i risultati</small></html>");
    lblInfoGestione.setForeground(new Color(100, 100, 100));
    gestionePanel.add(Box.createVerticalStrut(5));
    gestionePanel.add(lblInfoGestione);
    
    return gestionePanel;
}

private void avviaLega(ActionEvent e) {
    Lega legaSelezionata = (Lega) comboLeghePopola.getSelectedItem();
    
    if (legaSelezionata == null) {
        JOptionPane.showMessageDialog(this,
            "Seleziona una lega da avviare!",
            "Attenzione",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Verifica stato lega
    String statoLega = scontroDAO.getStatoLega(legaSelezionata.getIdLega());
    if (!"CREATA".equals(statoLega)) {
        JOptionPane.showMessageDialog(this,
            "La lega è già stata avviata o è terminata.\nStato attuale: " + statoLega,
            "Lega non disponibile",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Conta squadre complete
    List<Integer> squadreComplete = getSquadreCompleteLega(legaSelezionata.getIdLega());
    
    String message = "Avviare la lega '" + legaSelezionata.getNome() + "'?\n\n";
    message += "Squadre complete: " + squadreComplete.size() + "\n";
    message += "Questo genererà il calendario completo degli scontri.\n\n";
    message += "⚠️ ATTENZIONE: Dopo l'avvio nessuno potrà più entrare o uscire dalla lega!\n\n";
    message += "Procedere?";
    
    int conferma = JOptionPane.showConfirmDialog(this,
        message,
        "Conferma Avvio Lega",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
    
    if (conferma == JOptionPane.YES_OPTION) {
        if (scontroDAO.avviaLegaManualmente(legaSelezionata.getIdLega())) {
            JOptionPane.showMessageDialog(this,
                "Lega '" + legaSelezionata.getNome() + "' avviata con successo!\n" +
                "Calendario generato per tutte le giornate.\n" +
                "Ora puoi simulare la stagione completa.",
                "Lega Avviata",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Errore nell'avvio della lega.\n" +
                "Verifica che ci siano abbastanza squadre complete (minimo 2, numero pari).",
                "Errore",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void simulaStagioneCompleta(ActionEvent e) {
    Lega legaSelezionata = (Lega) comboLeghePopola.getSelectedItem();
    
    if (legaSelezionata == null) {
        JOptionPane.showMessageDialog(this,
            "Seleziona una lega da simulare!",
            "Attenzione",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Verifica stato lega
    String statoLega = scontroDAO.getStatoLega(legaSelezionata.getIdLega());
    if (!"IN_CORSO".equals(statoLega)) {
        JOptionPane.showMessageDialog(this,
            "La lega deve essere 'IN_CORSO' per essere simulata.\nStato attuale: " + statoLega,
            "Lega non pronta",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int giornataCorrente = scontroDAO.getGiornataCorrente(legaSelezionata.getIdLega());
    
    String message = "Simulare l'intera stagione per '" + legaSelezionata.getNome() + "'?\n\n";
    message += "Giornata corrente: " + giornataCorrente + "\n";
    message += "Questo calcolerà automaticamente tutti i risultati rimanenti.\n\n";
    message += "Procedere?";
    
    int conferma = JOptionPane.showConfirmDialog(this,
        message,
        "Simula Stagione Completa",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    
    if (conferma == JOptionPane.YES_OPTION) {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Simulazione in corso...");
                scontroDAO.simulaTutteLeGiornate(legaSelezionata.getIdLega());
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                // Potresti mostrare una progress bar qui
                for (String msg : chunks) {
                    System.out.println(msg);
                }
            }
            
            @Override
            protected void done() {
                JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                    "Stagione simulata completamente!\n" +
                    "La lega '" + legaSelezionata.getNome() + "' è ora terminata.\n" +
                    "Puoi consultare i risultati finali.",
                    "Simulazione Completata",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        };
        
        worker.execute();
    }
}
3. Rimuovi i pulsanti da LeagueDetailPanel
Nel LeagueDetailPanel.java, rimuovi i pulsanti di gestione lega dall'interfaccia utente normale (mantieni solo la visualizzazione dei risultati se la lega è in corso).

Ora il flusso è:

Admin panel: Avvia lega → Genera calendario completo
Admin panel: Simula stagione → Calcola tutti i risultati in sequenza
Interfaccia utente: Visualizza solo risultati e classifiche
Questo ti va bene come approccio?




Nel admin panel, scontroDAO mi da errore, idem getSquadreCompleteLega

Modifica
Hai ragione, devi importare e dichiarare le dipendenze. Aggiungi questi elementi a AdminLeaguePanel.java:

1. Aggiungi gli import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.model.Calciatore;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;

/**
 * Dialog per la creazione visuale della formazione
 */
public class VisualFormationDialog extends JDialog {
    
    private final SquadraFantacalcio squadra;
    private final Utente utente;
    private final String modulo;
    private final int giornataCorrente;
    
    // Componenti GUI
    private JPanel campoPanel;
    private JTable tabellaGiocatori;
    private DefaultTableModel modelTabella;
    private JButton btnSalva, btnReset;
    
    // Logica formazione
    private final Map<String, Calciatore> posizioni; // "P1", "D1", "D2", etc. -> Calciatore
    private final Map<String, JButton> bottoniPosizioni; // Riferimenti ai bottoni sul campo
    private final int[] moduloNumeri; // [difensori, centrocampisti, attaccanti]
    
    public VisualFormationDialog(Window parent, SquadraFantacalcio squadra, Utente utente, String modulo) {
        super(parent, "Formazione " + modulo + " - " + squadra.getNomeSquadra(), ModalityType.APPLICATION_MODAL);
        this.squadra = squadra;
        this.utente = utente;
        this.modulo = modulo;
        this.giornataCorrente = 1; // TODO: Recuperare giornata corrente
        this.posizioni = new HashMap<>();
        this.bottoniPosizioni = new HashMap<>();
        this.moduloNumeri = parseModulo(modulo);
        
        initializeGUI();
        loadGiocatori();
        setLocationRelativeTo(parent);
    }
    
    private void initializeGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1000, 700));
        
        createHeaderPanel();
        createMainContent();
        createBottomPanel();
        
        pack();
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(34, 139, 34));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("⚽ Formazione " + modulo, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel infoLabel = new JLabel("Clicca sui + per aggiungere i giocatori", SwingConstants.CENTER);
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        infoLabel.setForeground(new Color(200, 255, 200));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(infoLabel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Pannello campo (sinistra)
        campoPanel = createCampoPanel();
        splitPane.setLeftComponent(campoPanel);
        
        // Pannello giocatori (destra)
        JPanel giocatoriPanel = createGiocatoriPanel();
        splitPane.setRightComponent(giocatoriPanel);
        
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createCampoPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCampo(g);
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(34, 139, 34));
        panel.setPreferredSize(new Dimension(550, 500));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2), 
            "Campo da Gioco", 
            0, 0, 
            new Font(Font.SANS_SERIF, Font.BOLD, 14), 
            Color.WHITE
        ));
        
        createPosizioni(panel);
        
        return panel;
    }
    
    private void drawCampo(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.WHITE);
        
        // Bordi campo
        g2d.drawRect(50, 50, 450, 380);
        
        // Linea di metà campo
        g2d.drawLine(50, 240, 500, 240);
        
        // Cerchio di centrocampo
        g2d.drawOval(225, 190, 100, 100);
        
        // Aree di rigore
        g2d.drawRect(50, 140, 80, 200);  // Area sinistra
        g2d.drawRect(420, 140, 80, 200); // Area destra
        
        // Porte
        g2d.drawRect(35, 190, 15, 100);  // Porta sinistra
        g2d.drawRect(500, 190, 15, 100); // Porta destra
    }
    
    private void createPosizioni(JPanel campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Portiere
        gbc.gridx = 0; gbc.gridy = 2;
        JButton btnPortiere = createPosizioneButton("P1", Calciatore.Ruolo.PORTIERE);
        campo.add(btnPortiere, gbc);
        
        // Difensori
        int numDifensori = moduloNumeri[0];
        for (int i = 0; i < numDifensori; i++) {
            gbc.gridx = 1; 
            gbc.gridy = getYPosition(i, numDifensori);
            JButton btn = createPosizioneButton("D" + (i + 1), Calciatore.Ruolo.DIFENSORE);
            campo.add(btn, gbc);
        }
        
        // Centrocampisti
        int numCentrocampisti = moduloNumeri[1];
        for (int i = 0; i < numCentrocampisti; i++) {
            gbc.gridx = 2;
            gbc.gridy = getYPosition(i, numCentrocampisti);
            JButton btn = createPosizioneButton("C" + (i + 1), Calciatore.Ruolo.CENTROCAMPISTA);
            campo.add(btn, gbc);
        }
        
        // Attaccanti
        int numAttaccanti = moduloNumeri[2];
        for (int i = 0; i < numAttaccanti; i++) {
            gbc.gridx = 3;
            gbc.gridy = getYPosition(i, numAttaccanti);
            JButton btn = createPosizioneButton("A" + (i + 1), Calciatore.Ruolo.ATTACCANTE);
            campo.add(btn, gbc);
        }
    }
    
    private int getYPosition(int index, int total) {
        // Distribuisce i giocatori verticalmente in modo equilibrato
        if (total == 1) return 2;
        if (total == 2) return index == 0 ? 1 : 3;
        if (total == 3) return index;
        if (total == 4) return index;
        if (total == 5) return index;
        return index;
    }
    
    private JButton createPosizioneButton(String posizione, Calciatore.Ruolo ruolo) {
        JButton btn = new JButton("+");
        btn.setPreferredSize(new Dimension(80, 80));
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        btn.setBackground(getRuoloColor(ruolo));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Aggiungi " + ruolo.name().toLowerCase());
        
        btn.addActionListener(e -> apriSelettoreGiocatore(posizione, ruolo));
        
        bottoniPosizioni.put(posizione, btn);
        return btn;
    }
    
    private Color getRuoloColor(Calciatore.Ruolo ruolo) {
        return switch (ruolo) {
            case PORTIERE -> new Color(255, 193, 7);
            case DIFENSORE -> new Color(63, 81, 181);
            case CENTROCAMPISTA -> new Color(76, 175, 80);
            case ATTACCANTE -> new Color(244, 67, 54);
        };
    }
    
    private JPanel createGiocatoriPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Giocatori Disponibili"));
        panel.setPreferredSize(new Dimension(350, 500));
        
        // Tabella giocatori
        String[] colonne = {"Nome", "Ruolo", "Costo"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabellaGiocatori = new JTable(modelTabella);
        tabellaGiocatori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaGiocatori.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(tabellaGiocatori);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Info panel
        JLabel infoLabel = new JLabel("<html><center>Doppio click su un giocatore<br>per aggiungerlo alla posizione selezionata</center></html>");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        btnSalva = new JButton("💾 Salva Formazione");
        btnReset = new JButton("🔄 Reset");
        JButton btnChiudi = new JButton("❌ Chiudi");
        
        // Stile pulsanti
        styleButton(btnSalva, new Color(76, 175, 80));
        styleButton(btnReset, new Color(255, 152, 0));
        styleButton(btnChiudi, new Color(158, 158, 158));
        
        btnSalva.addActionListener(this::salvaFormazione);
        btnReset.addActionListener(this::resetFormazione);
        btnChiudi.addActionListener(e -> dispose());
        
        bottomPanel.add(btnSalva);
        bottomPanel.add(btnReset);
        bottomPanel.add(btnChiudi);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadGiocatori() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                modelTabella.setRowCount(0);
                
                for (Calciatore calciatore : squadra.getCalciatori()) {
                    Object[] riga = {
                        calciatore.getNomeCompleto(),
                        calciatore.getRuolo().name(),
                        calciatore.getCosto() + "€"
                    };
                    modelTabella.addRow(riga);
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private void apriSelettoreGiocatore(String posizione, Calciatore.Ruolo ruolo) {
        // Filtra giocatori per ruolo
        List<Calciatore> giocatoriDisponibili = squadra.getCalciatori().stream()
            .filter(c -> c.getRuolo() == ruolo)
            .filter(c -> !posizioni.containsValue(c)) // Non già selezionati
            .toList();
        
        if (giocatoriDisponibili.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Nessun " + ruolo.name().toLowerCase() + " disponibile!",
                "Nessun giocatore",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Crea dialog di selezione
        PlayerSelectionDialog dialog = new PlayerSelectionDialog(this, giocatoriDisponibili, ruolo);
        dialog.setVisible(true);
        
        if (dialog.getGiocatoreSelezionato() != null) {
            assegnaGiocatore(posizione, dialog.getGiocatoreSelezionato());
        }
    }
    
    private void assegnaGiocatore(String posizione, Calciatore giocatore) {
        posizioni.put(posizione, giocatore);
        
        JButton btn = bottoniPosizioni.get(posizione);
        btn.setText("<html><center>" + giocatore.getCognome() + "</center></html>");
        btn.setToolTipText(giocatore.getNomeCompleto() + " (" + giocatore.getCosto() + "€)");
        
        updateSalvaButton();
        campoPanel.repaint();
    }
    
    private void updateSalvaButton() {
        // Abilita salva solo se abbiamo 11 giocatori
        int giocatoriSelezionati = posizioni.size();
        btnSalva.setEnabled(giocatoriSelezionati == 11);
        btnSalva.setText("💾 Salva Formazione (" + giocatoriSelezionati + "/11)");
    }
    
    private void salvaFormazione(ActionEvent e) {
        if (posizioni.size() != 11) {
            JOptionPane.showMessageDialog(this,
                "Devi selezionare esattamente 11 giocatori!",
                "Formazione incompleta",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return salvaFormazioneDatabase();
            }
            
            @Override
            protected void done() {
                try {
                    boolean successo = get();
                    if (successo) {
                        JOptionPane.showMessageDialog(VisualFormationDialog.this,
                            "Formazione salvata con successo!",
                            "Successo",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(VisualFormationDialog.this,
                            "Errore durante il salvataggio.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(VisualFormationDialog.this,
                        "Errore: " + ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void resetFormazione(ActionEvent e) {
        int conferma = JOptionPane.showConfirmDialog(this,
            "Sei sicuro di voler resettare la formazione?",
            "Conferma Reset",
            JOptionPane.YES_NO_OPTION);
        
        if (conferma == JOptionPane.YES_OPTION) {
            posizioni.clear();
            
            // Reset bottoni
            for (JButton btn : bott