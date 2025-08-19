package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.FormazioneDAO;
import fantacalcio.dao.LegaDAO;
import fantacalcio.dao.ScontroLegaDAO;
import fantacalcio.dao.SquadraFantacalcioDAO;
import fantacalcio.gui.user.UserMainFrame;
import fantacalcio.gui.user.dialog.ModuleSelectionDialog;
import fantacalcio.gui.user.dialog.RisultatiLegaDialog;
import fantacalcio.gui.user.dialog.VisualFormationDialog;
import fantacalcio.model.Calciatore;
import fantacalcio.model.Formazione;
import fantacalcio.model.Lega;
import fantacalcio.model.SquadraFantacalcio;
import fantacalcio.model.Utente;
import fantacalcio.util.DatabaseConnection;

public class LeagueDetailPanel extends JPanel {
    
    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final Lega lega;
    private final SquadraFantacalcioDAO squadraDAO;
    private final ScontroLegaDAO scontroDAO;
    private final LegaDAO legaDAO;
    
    private SquadraFantacalcio squadraUtente;
    private JPanel contentPanel;
    
    public LeagueDetailPanel(UserMainFrame parentFrame, Utente utente, Lega lega) {
        this.parentFrame = parentFrame;
        this.utenteCorrente = utente;
        this.lega = lega;
        this.squadraDAO = new SquadraFantacalcioDAO();
        this.scontroDAO = new ScontroLegaDAO();
        this.legaDAO = new LegaDAO();

        initializeGUI();
        loadUserTeamForLeague();
    }
    
    private void initializeGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
        
        // Mostra messaggio di caricamento iniziale
        JLabel loadingLabel = new JLabel("Caricamento...", JLabel.CENTER);
        loadingLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
        loadingLabel.setForeground(new Color(100, 100, 100));
        contentPanel.add(loadingLabel, BorderLayout.CENTER);
    }
    
    private void loadUserTeamForLeague() {
        SwingWorker<Optional<SquadraFantacalcio>, Void> worker = new SwingWorker<Optional<SquadraFantacalcio>, Void>() {
            @Override
            protected Optional<SquadraFantacalcio> doInBackground() throws Exception {
                return squadraDAO.trovaSquadraUtentePerLega(utenteCorrente.getIdUtente(), lega.getIdLega());
            }
            
            @Override
            protected void done() {
                try {
                    Optional<SquadraFantacalcio> squadraOpt = get();
                    
                    if (squadraOpt.isPresent()) {
                        squadraUtente = squadraOpt.get();
                        showTeamManagement();
                    } else {
                        showTeamCreation();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(LeagueDetailPanel.this,
                        "Errore caricamento squadra: " + e.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void showTeamCreation() {
        contentPanel.removeAll();
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("⚽ Crea la tua squadra per questa lega");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 150, 243));
        
        JLabel infoLabel = new JLabel("Non hai ancora una squadra per la lega \"" + lega.getNome() + "\"");
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        infoLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(infoLabel, BorderLayout.SOUTH);
        
        CreateTeamPanel createPanel = new CreateTeamPanel(parentFrame, utenteCorrente, lega.getIdLega()) {
            @Override
            protected void onTeamCreatedSuccess(SquadraFantacalcio nuovaSquadra) {
                LeagueDetailPanel.this.onTeamCreated(nuovaSquadra);
            }
        };
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(createPanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showTeamManagement() {
        contentPanel.removeAll();
        
        // Header con info squadra dell'utente
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("🏆 " + squadraUtente.getNomeSquadra());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 150, 243));
        
        JLabel statsLabel = new JLabel(squadraUtente.getStatistiche());
        statsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        statsLabel.setForeground(squadraUtente.isCompletata() ? new Color(76, 175, 80) : new Color(255, 152, 0));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statsLabel, BorderLayout.SOUTH);
        
        // Info lega
        JPanel legaInfoPanel = createLegaInfoPanel();
        
        // Tabella squadre della lega
        JPanel squadrePanel = createSquadreLegaTable();
        
        // Pannello formazione (solo se squadra completa)
        JPanel formazionePanel = null;
        if (squadraUtente.isCompletata()) {
            formazionePanel = createFormazionePanel();
        }
        
        // Layout principale
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(legaInfoPanel, BorderLayout.NORTH);
        mainPanel.add(squadrePanel, BorderLayout.CENTER);
        if (formazionePanel != null) {
            mainPanel.add(formazionePanel, BorderLayout.SOUTH);
        }
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createLegaInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("📊 Info Lega"));
        
        String statoLega = scontroDAO.getStatoLega(lega.getIdLega());
        int giornataCorrente = scontroDAO.getGiornataCorrente(lega.getIdLega());
        
        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftInfo.add(new JLabel("Stato: " + statoLega));
        leftInfo.add(Box.createHorizontalStrut(20));
        leftInfo.add(new JLabel("Giornata: " + giornataCorrente));
        
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Pulsante risultati (se lega avviata)
        if ("IN_CORSO".equals(statoLega) || "TERMINATA".equals(statoLega)) {
            JButton btnRisultati = new JButton("📊 Vedi Risultati");
            styleButton(btnRisultati, new Color(33, 150, 243));
            btnRisultati.addActionListener(e -> mostraRisultati());
            rightInfo.add(btnRisultati);
        }

        infoPanel.add(leftInfo, BorderLayout.WEST);
        infoPanel.add(rightInfo, BorderLayout.EAST);
        
        return infoPanel;
    }

    private JPanel createSquadreLegaTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("🏆 Squadre della Lega"));
        
        String[] colonne = {"Nome Squadra", "Giocatori", "Budget Speso", "Stato", "Proprietario"};
        DefaultTableModel model = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tabella = new JTable(model);
        tabella.setRowHeight(30);
        tabella.getTableHeader().setBackground(new Color(63, 81, 181));
        tabella.getTableHeader().setForeground(Color.WHITE);
        
        caricaSquadreLega(model);
        
        JScrollPane scrollPane = new JScrollPane(tabella);
        scrollPane.setPreferredSize(new Dimension(800, 250));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnAggiorna = new JButton("🔄 Aggiorna");
        styleButton(btnAggiorna, new Color(158, 158, 158));
        btnAggiorna.addActionListener(e -> caricaSquadreLega(model));
        buttonPanel.add(btnAggiorna);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return tablePanel;
    }

    private JPanel createFormazionePanel() {
        JPanel formazionePanel = new JPanel(new BorderLayout());
        formazionePanel.setBorder(BorderFactory.createTitledBorder("⚽ Gestione Formazione"));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Controllo se la formazione è già stata salvata per la giornata corrente
        int giornataCorrente = scontroDAO.getGiornataCorrente(lega.getIdLega());
        boolean formazioneEsiste = verificaFormazioneEsistente(giornataCorrente);
        
        if (formazioneEsiste) {
            infoPanel.add(new JLabel("✅ Formazione salvata per la giornata " + giornataCorrente));
            
            JButton btnModificaFormazione = new JButton("✏️ Modifica Formazione");
            styleButton(btnModificaFormazione, new Color(255, 152, 0));
            btnModificaFormazione.addActionListener(e -> apriGestioneFormazione());
            
            JButton btnVisualizzaFormazione = new JButton("👁️ Visualizza");
            styleButton(btnVisualizzaFormazione, new Color(158, 158, 158));
            btnVisualizzaFormazione.addActionListener(e -> visualizzaFormazione());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(btnVisualizzaFormazione);
            buttonPanel.add(btnModificaFormazione);
            
            formazionePanel.add(infoPanel, BorderLayout.WEST);
            formazionePanel.add(buttonPanel, BorderLayout.EAST);
        } else {
            infoPanel.add(new JLabel("⚠️ Nessuna formazione impostata per la giornata " + giornataCorrente));
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnCreaFormazione = new JButton("⚙️ Crea Formazione");
            styleButton(btnCreaFormazione, new Color(76, 175, 80));
            btnCreaFormazione.addActionListener(e -> apriGestioneFormazione());
            buttonPanel.add(btnCreaFormazione);
            
            formazionePanel.add(infoPanel, BorderLayout.WEST);
            formazionePanel.add(buttonPanel, BorderLayout.EAST);
        }
        
        return formazionePanel;
    }

    private boolean verificaFormazioneEsistente(int giornata) {
        FormazioneDAO formazioneDAO = new FormazioneDAO();
        Integer idFormazione = formazioneDAO.getFormazioneSquadraGiornata(
            squadraUtente.getIdSquadraFantacalcio(), giornata);
        return idFormazione != null;
    }

    private void visualizzaFormazione() {
        int giornataCorrente = scontroDAO.getGiornataCorrente(lega.getIdLega());
        FormazioneDAO formazioneDAO = new FormazioneDAO();
        Integer idFormazione = formazioneDAO.getFormazioneSquadraGiornata(
            squadraUtente.getIdSquadraFantacalcio(), giornataCorrente);
        
        if (idFormazione != null) {
            Formazione formazione = formazioneDAO.getFormazioneById(idFormazione);
            List<Calciatore> titolari = formazioneDAO.trovaTitolari(idFormazione);
            List<Calciatore> panchinari = formazioneDAO.trovaPanchinari(idFormazione);
            
            StringBuilder info = new StringBuilder();
            info.append("🏆 FORMAZIONE GIORNATA ").append(giornataCorrente).append("\n\n");
            info.append("📐 Modulo: ").append(formazione.getModulo()).append("\n\n");
            
            info.append("⭐ TITOLARI (").append(titolari.size()).append("):\n");
            for (Calciatore c : titolari) {
                info.append("• ").append(c.getRuolo().getAbbreviazione())
                    .append(" ").append(c.getNomeCompleto()).append("\n");
            }
            
            info.append("\n🪑 PANCHINA (").append(panchinari.size()).append("):\n");
            for (Calciatore c : panchinari) {
                info.append("• ").append(c.getRuolo().getAbbreviazione())
                    .append(" ").append(c.getNomeCompleto()).append("\n");
            }
            
            JTextArea textArea = new JTextArea(info.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, 
                "Formazione - " + squadraUtente.getNomeSquadra(), 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void caricaSquadreLega(DefaultTableModel model) {
        model.setRowCount(0);
        
        SwingWorker<List<SquadraLegaInfo>, Void> worker = new SwingWorker<List<SquadraLegaInfo>, Void>() {
            @Override
            protected List<SquadraLegaInfo> doInBackground() throws Exception {
                return getSquadreDaLega(lega.getIdLega());
            }
            
            @Override
            protected void done() {
                try {
                    List<SquadraLegaInfo> squadre = get();
                    for (SquadraLegaInfo squadra : squadre) {
                        Object[] riga = {
                            squadra.nomeSquadra,
                            squadra.numeroGiocatori + "/25",
                            squadra.budgetSpeso + " €",
                            squadra.completata ? "✅ Completa" : "⚠️ In costruzione",
                            squadra.proprietario
                        };
                        model.addRow(riga);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LeagueDetailPanel.this,
                        "Errore caricamento squadre: " + e.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void apriGestioneFormazione() {
        if (!squadraUtente.isCompletata()) {
            JOptionPane.showMessageDialog(this,
                "La squadra deve essere completata prima di poter creare una formazione!",
                "Squadra incompleta",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Prima mostra il dialog per scegliere il modulo
        ModuleSelectionDialog moduloDialog = new ModuleSelectionDialog(
            SwingUtilities.getWindowAncestor(this)
        );
        moduloDialog.setVisible(true);
        
        if (moduloDialog.isConfirmed() && moduloDialog.getModuloSelezionato() != null) {
            String moduloScelto = moduloDialog.getModuloSelezionato();
            
            // Poi apri il nuovo dialog visuale
            VisualFormationDialog formazioneDialog = new VisualFormationDialog(
                SwingUtilities.getWindowAncestor(this),
                squadraUtente,
                utenteCorrente,
                moduloScelto
            );
            formazioneDialog.setVisible(true);
            
            // Refresh del panel dopo che il dialog è stato chiuso
            SwingUtilities.invokeLater(() -> {
                // Ricarica le informazioni sulla formazione
                removeAll();
                showTeamManagement();
                revalidate();
                repaint();
            });
        }
    }


    private void mostraRisultati() {
        RisultatiLegaDialog dialog = new RisultatiLegaDialog(
            SwingUtilities.getWindowAncestor(this),
            lega,
            scontroDAO.getGiornataCorrente(lega.getIdLega())
        );
        dialog.setVisible(true);
    }

    private List<Integer> getSquadreCompleteLega(int idLega) {
        List<Integer> squadreIds = new ArrayList<>();
        String sql = """
            SELECT s.ID_Squadra 
            FROM SQUADRA_FANTACALCIO s
            JOIN PARTECIPA p ON s.ID_Squadra = p.ID_Squadra
            WHERE p.ID_Lega = ? AND s.Completata = true
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    squadreIds.add(rs.getInt("ID_Squadra"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore caricamento squadre complete: " + e.getMessage());
        }
        
        return squadreIds;
    }
    
    public void onTeamCreated(SquadraFantacalcio nuovaSquadra) {
        this.squadraUtente = nuovaSquadra;
        showTeamManagement();
        parentFrame.updateTitle();
        
        JOptionPane.showMessageDialog(this,
            "Squadra '" + nuovaSquadra.getNomeSquadra() + "' creata e collegata alla lega!",
            "Successo",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static class SquadraLegaInfo {
        String nomeSquadra;
        int numeroGiocatori;
        int budgetSpeso;
        boolean completata;
        String proprietario;
        
        SquadraLegaInfo(String nome, int giocatori, int budget, boolean completa, String owner) {
            this.nomeSquadra = nome;
            this.numeroGiocatori = giocatori;
            this.budgetSpeso = budget;
            this.completata = completa;
            this.proprietario = owner;
        }
    }

    private List<SquadraLegaInfo> getSquadreDaLega(int idLega) {
        List<SquadraLegaInfo> squadre = new ArrayList<>();
        String sql = """
            SELECT s.Nome, s.Budget_totale - s.Budget_rimanente as Budget_Speso, 
                s.Completata, u.Nickname,
                (SELECT COUNT(*) FROM COMPOSIZIONE c WHERE c.ID_Squadra = s.ID_Squadra) as Num_Giocatori
            FROM SQUADRA_FANTACALCIO s
            JOIN PARTECIPA p ON s.ID_Squadra = p.ID_Squadra
            JOIN CREA cr ON s.ID_Squadra = cr.ID_Squadra
            JOIN UTENTE u ON cr.ID_Utente = u.ID_Utente
            WHERE p.ID_Lega = ?
            ORDER BY s.Completata DESC, s.Nome
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idLega);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    squadre.add(new SquadraLegaInfo(
                        rs.getString("Nome"),
                        rs.getInt("Num_Giocatori"),
                        rs.getInt("Budget_Speso"),
                        rs.getBoolean("Completata"),
                        rs.getString("Nickname")
                    ));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore caricamento squadre lega: " + e.getMessage());
        }
        
        return squadre;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}