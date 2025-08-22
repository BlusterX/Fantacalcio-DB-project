package fantacalcio.gui.user.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import fantacalcio.util.LeagueTicker;

public class LeagueDetailPanel extends JPanel {

    // === Nuovo: solo ticker a secondi (2 min totali, deadline a 30s) ===
    private LeagueTicker ticker;

    private final UserMainFrame parentFrame;
    private final Utente utenteCorrente;
    private final Lega lega;
    private final SquadraFantacalcioDAO squadraDAO;
    private final ScontroLegaDAO scontroDAO;
    private final LegaDAO legaDAO;

    private SquadraFantacalcio squadraUtente;
    private JPanel contentPanel;
    private JLabel lblCountdown; // label monospaziata per il timer

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

        JLabel loadingLabel = new JLabel("Caricamento...", JLabel.CENTER);
        loadingLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
        loadingLabel.setForeground(new Color(100, 100, 100));
        contentPanel.add(loadingLabel, BorderLayout.CENTER);
    }

    private void loadUserTeamForLeague() {
        SwingWorker<Optional<SquadraFantacalcio>, Void> worker = new SwingWorker<>() {
            @Override protected Optional<SquadraFantacalcio> doInBackground() {
                return squadraDAO.trovaSquadraUtentePerLega(utenteCorrente.getIdUtente(), lega.getIdLega());
            }
            @Override protected void done() {
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
        stopTicker(); // sicurezza
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
            @Override protected void onTeamCreatedSuccess(SquadraFantacalcio nuovaSquadra) {
                LeagueDetailPanel.this.onTeamCreated(nuovaSquadra);
            }
        };

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(createPanel, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showTeamManagement() {
        stopTicker(); // evita doppio timer
        contentPanel.removeAll();

        // Header
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

        // Info lega + tabella + (eventuale) formazione
        JPanel legaInfoPanel = createLegaInfoPanel();
        JPanel squadrePanel = createSquadreLegaTable();
        JPanel formazionePanel = null;
        if (squadraUtente.isCompletata()) {
            formazionePanel = createFormazionePanel();
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(legaInfoPanel, BorderLayout.NORTH);
        mainPanel.add(squadrePanel, BorderLayout.CENTER);
        if (formazionePanel != null) mainPanel.add(formazionePanel, BorderLayout.SOUTH);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(mainPanel, BorderLayout.CENTER);

        // Avvio ticker di test: 2:00 totali, deadline formazioni a 0:30
        startTicker2Min();

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

        // Destra: countdown (larghezza fissa) + pulsante risultati
        JPanel rightInfo = new JPanel(new BorderLayout(8, 0));

        lblCountdown = new JLabel(" "); // inizialmente vuoto
        lblCountdown.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        Dimension fixed = new Dimension(210, lblCountdown.getPreferredSize().height);
        lblCountdown.setPreferredSize(fixed);
        rightInfo.add(lblCountdown, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        if ("IN_CORSO".equals(statoLega) || "TERMINATA".equals(statoLega)) {
            JButton btnRisultati = new JButton("📊 Vedi Risultati");
            styleButton(btnRisultati, new Color(33, 150, 243));
            btnRisultati.addActionListener(e -> mostraRisultati());
            actions.add(btnRisultati);
        }
        rightInfo.add(actions, BorderLayout.EAST);

        infoPanel.add(leftInfo, BorderLayout.WEST);
        infoPanel.add(rightInfo, BorderLayout.EAST);
        return infoPanel;
    }

    // ===== Ticker a 2 minuti (test) =====
    private void startTicker2Min() {
        // sicurezza: label potrebbe non esistere se l’utente non ha squadra
        if (lblCountdown == null) return;

        ticker = new LeagueTicker(
            lega.getIdLega(),
            120, // 2:00 totali
            30,  // deadline a 0:30 (quindi 1:30 per consegnare)
            new LeagueTicker.Listener() {
                @Override public void onTick(int secondsLeft) {
                    lblCountdown.setText("⏳ Prossima giornata tra " + fmtMMSS(secondsLeft));
                }

                @Override public void onDeadlineFormazioni() {
                    // TODO: congela formazioni mancanti (clona l’ultima)
                    // Esegui su worker per non bloccare l’EDT:
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() {
                            // new SchedulerDAO().congelaFormazioniMancanti(lega.getIdLega(), scontroDAO.getGiornataCorrente(lega.getIdLega()));
                            return null;
                        }
                        @Override protected void done() {
                            // potresti mostrare un toast o aggiornare UI se serve
                        }
                    }.execute();
                }

                @Override public void onStartSimulazione() {
                    lblCountdown.setText("▶ Inizio giornata!");
                    // TODO: simulazione risultati (worker asincrono)
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() {
                            // esempio: scontroDAO.simulaGiornata(lega.getIdLega(), scontroDAO.getGiornataCorrente(lega.getIdLega()));
                            return null;
                        }
                        @Override protected void done() {
                            // Al termine aggiorna la UI o apri i risultati
                            // mostraRisultati();
                        }
                    }.execute();
                }
            }
        );
        ticker.start();
    }

    private void stopTicker() {
        if (ticker != null) {
            ticker.stop();
            ticker = null;
        }
    }

    private static String fmtMMSS(long totalSeconds) {
        long m = totalSeconds / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    // ===== fine ticker =====

    private JPanel createSquadreLegaTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("🏆 Squadre della Lega"));

        String[] colonne = {"Nome Squadra", "Giocatori", "Budget Speso", "Stato", "Proprietario"};
        DefaultTableModel model = new DefaultTableModel(colonne, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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

        SwingWorker<List<SquadraFantacalcioDAO.SquadraLegaInfo>, Void> worker =
                new SwingWorker<>() {
                    @Override protected List<SquadraFantacalcioDAO.SquadraLegaInfo> doInBackground() {
                        return squadraDAO.trovaSquadreInfoPerLega(lega.getIdLega());
                    }
                    @Override protected void done() {
                        try {
                            for (var info : get()) {
                                model.addRow(new Object[]{
                                        info.nomeSquadra,
                                        info.numeroGiocatori + "/25",
                                        info.budgetSpeso + " €",
                                        info.completata ? "✅ Completa" : "⚠️ In costruzione",
                                        info.proprietario
                                });
                            }
                        } catch (InterruptedException | ExecutionException e) {
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

        ModuleSelectionDialog moduloDialog = new ModuleSelectionDialog(
                SwingUtilities.getWindowAncestor(this)
        );
        moduloDialog.setVisible(true);

        if (moduloDialog.isConfirmed() && moduloDialog.getModuloSelezionato() != null) {
            String moduloScelto = moduloDialog.getModuloSelezionato();

            VisualFormationDialog formazioneDialog = new VisualFormationDialog(
                    SwingUtilities.getWindowAncestor(this),
                    squadraUtente,
                    utenteCorrente,
                    moduloScelto,
                    scontroDAO.getGiornataCorrente(lega.getIdLega())
            );
            formazioneDialog.setVisible(true);

            SwingUtilities.invokeLater(() -> {
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

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Stop del ticker quando il pannello viene rimosso (evita leak/thread vivi)
    @Override public void removeNotify() {
        stopTicker();
        super.removeNotify();
    }
}
