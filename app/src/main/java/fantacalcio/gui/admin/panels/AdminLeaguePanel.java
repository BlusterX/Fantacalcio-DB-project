package fantacalcio.gui.admin.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import fantacalcio.dao.LegaDAO;
import fantacalcio.dao.TipoCampionatoDAO;
import fantacalcio.gui.admin.AdminContext;
import fantacalcio.model.Lega;
import fantacalcio.model.Utente;
import fantacalcio.util.DataPopulator;
import fantacalcio.util.DataPopulator.CampionatoPredefinito;

/** Admin: gestione leghe (versione minimale) */
public final class AdminLeaguePanel extends JPanel {
    private final AdminContext ctx;
    private final Utente adminUtente;
    private final LegaDAO legaDAO;
    
    private final TipoCampionatoDAO tipoCampionatoDAO = new TipoCampionatoDAO();

    private JTable tabellaLeghe;
    private DefaultTableModel modelTabella;
    // Crea lega
    private JTextField txtNomeLega;
    private JComboBox<String> comboCampionato; // "Serie A" / "Premier League"
    private JButton btnCreaLega, btnEliminaLega, btnRefresh, btnCopiaCodice;
    // Selezione corrente
    private Lega legaSelezionata;

    public AdminLeaguePanel(AdminContext ctx, Utente adminUtente) {
        this.ctx = ctx;
        this.adminUtente = adminUtente;
        this.legaDAO = new LegaDAO();

        initializeComponents();
        setupLayout();
        loadData();
    }
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        // tabella
        String[] colonne = {"ID", "Nome", "Codice", "Stato", "Campionato", "Partecipanti"};
        modelTabella = new DefaultTableModel(colonne, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabellaLeghe = new JTable(modelTabella);
        tabellaLeghe.setRowHeight(28);
        tabellaLeghe.getTableHeader().setReorderingAllowed(false);
        tabellaLeghe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaLeghe.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelect();
        });
        tabellaLeghe.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 2) {
                int r = tabellaLeghe.getSelectedRow();
                if (r >= 0) {
                    int id = (Integer) modelTabella.getValueAt(r, 0);
                    String cod = (String) modelTabella.getValueAt(r, 2);
                    // ricarico l'oggetto preciso
                    List<Lega> mie = legaDAO.trovaLeghePerAdmin(adminUtente.getIdUtente());
                    Lega lega = mie.stream()
                            .filter(x -> x.getIdLega() == id && x.getCodiceAccesso().equals(cod))
                            .findFirst().orElse(null);
                    if (lega != null) {
                        // apri dialog “scoped” sulla lega
                        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(AdminLeaguePanel.this);
                        java.awt.Frame owner = (w instanceof java.awt.Frame)
                                ? (java.awt.Frame) w
                                : (w != null && w.getOwner() instanceof java.awt.Frame ? (java.awt.Frame) w.getOwner() : null);

                        new fantacalcio.gui.admin.dialogs.AdminLeagueDialog(owner, adminUtente, lega).setVisible(true);
                    }
                }
            }
        }
    });
        // form crea
        txtNomeLega = new JTextField(20);
        comboCampionato = new JComboBox<>(new String[]{"Serie A", "Premier League"});

        // bottoni
        btnCreaLega   = mkBtn("Crea");
        btnEliminaLega= mkBtn("Elimina");
        btnRefresh    = mkBtn("Aggiorna");
        btnCopiaCodice= mkBtn("Copia codice");

        btnEliminaLega.setEnabled(false);
        btnCopiaCodice.setEnabled(false);

        btnCreaLega.addActionListener(e -> creaLega());
        btnEliminaLega.addActionListener(e -> eliminaLega());
        btnRefresh.addActionListener(e -> loadData());
        btnCopiaCodice.addActionListener(e -> copiaCodice());
    }

    private JButton mkBtn(String t) {
        JButton b = new JButton(t);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,214,219)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        b.setBackground(Color.WHITE);
        return b;
    }

    private void setupLayout() {
        // top: crea lega
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        form.setBorder(BorderFactory.createTitledBorder("Crea nuova lega"));
        form.add(new JLabel("Nome:"));
        form.add(txtNomeLega);
        form.add(new JLabel("Campionato:"));
        form.add(comboCampionato);
        form.add(btnCreaLega);
        form.add(Box.createHorizontalStrut(8));
        form.add(btnRefresh);
        // center: tabella
        JScrollPane sp = new JScrollPane(tabellaLeghe);
        // bottom: azioni riga
        JPanel rowActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rowActions.add(btnCopiaCodice);
        rowActions.add(btnEliminaLega);

        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(rowActions, BorderLayout.SOUTH);
    }

    public void loadData() {
        SwingWorker<List<Lega>, Void> w = new SwingWorker<>() {
            @Override protected List<Lega> doInBackground() {
                return legaDAO.trovaLeghePerAdmin(adminUtente.getIdUtente());
            }
            @Override protected void done() {
                try {
                    List<Lega> leghe = get();
                    fillTable(leghe);
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                            "Errore caricamento leghe: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private void fillTable(List<Lega> leghe) {
        modelTabella.setRowCount(0);
        legaSelezionata = null;
        btnEliminaLega.setEnabled(false);
        btnCopiaCodice.setEnabled(false);

        for (Lega l : leghe) {
            String labelCamp = "—";
            if (l.getIdCampionato() != null) {
                try {
                    var tcOpt = tipoCampionatoDAO.findById(l.getIdCampionato());
                    labelCamp = tcOpt.map(t -> t.getNome() + " " + t.getAnno()).orElse("—");
                } catch (Exception ignore) { /* noop */ }
            }
            int partecipanti = legaDAO.contaPartecipanti(l.getIdLega());
            modelTabella.addRow(new Object[]{
                    l.getIdLega(), l.getNome(), l.getCodiceAccesso(), l.getStato(), labelCamp, partecipanti
            });
        }
    }

    private void onSelect() {
        int r = tabellaLeghe.getSelectedRow();
        if (r < 0) {
            legaSelezionata = null;
            btnEliminaLega.setEnabled(false);
            btnCopiaCodice.setEnabled(false);
            return;
        }
        int id = (Integer) modelTabella.getValueAt(r, 0);
        String cod = (String) modelTabella.getValueAt(r, 2);

        // ricarico l'oggetto esatto
        List<Lega> mie = legaDAO.trovaLeghePerAdmin(adminUtente.getIdUtente());
        legaSelezionata = mie.stream()
                .filter(x -> x.getIdLega() == id && x.getCodiceAccesso().equals(cod))
                .findFirst().orElse(null);

        btnEliminaLega.setEnabled(legaSelezionata != null);
        btnCopiaCodice.setEnabled(legaSelezionata != null);
    }

    private void creaLega() {
        String nome = txtNomeLega.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci un nome lega.",
                    "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String scelta = (String) comboCampionato.getSelectedItem();
        int anno = 2024;
        Integer idCamp = ensureCampionato(scelta, anno);
        if (idCamp == null) {
            JOptionPane.showMessageDialog(this, "Impossibile determinare ID campionato.",
                    "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Lega nuova = new Lega(nome, adminUtente.getIdUtente());
        nuova.setIdCampionato(idCamp);

        // UI busy
        btnCreaLega.setEnabled(false);
        btnRefresh.setEnabled(false);

        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                // 1) crea la lega
                boolean created = legaDAO.creaLega(nuova);
                if (!created) return false;

                // 2) popola squadre + calciatori dal CSV del campionato scelto
                try {
                    DataPopulator pop = new DataPopulator();
                    pop.popolaSquadreECalciatoriDaCsv(
                            enumCampionato(scelta),
                            csvFor(scelta)
                    );
                } catch (Exception ex) {
                    // non fallire tutta l’operazione: logga e prosegui
                    System.err.println("Popolamento CSV fallito: " + ex.getMessage());
                }
                return true;
            }

            @Override
            protected void done() {
                // UI un-busy
                btnCreaLega.setEnabled(true);
                btnRefresh.setEnabled(true);

                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                                "Lega creata!\nCodice invito: " + nuova.getCodiceAccesso()
                                + "\n\nDati del campionato '" + scelta + "' importati dal CSV.",
                                "OK", JOptionPane.INFORMATION_MESSAGE);

                        txtNomeLega.setText("");
                        loadData();

                        // aggiorna le altre tab del dialog (combo calciatori ecc.)
                        ctx.notifySquadreChanged();
                        ctx.notifyCalciatoriChanged();
                    } else {
                        JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                                "Creazione non riuscita.",
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(AdminLeaguePanel.this,
                            "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }
    
    private Integer ensureCampionato(String nome, int anno) {
        // crea-se-serve oppure ritorna quello esistente
        var tc = tipoCampionatoDAO.getOrCreate(nome, anno);
        return (tc != null) ? tc.getIdCampionato() : null;
    }


    private void eliminaLega() {
        if (legaSelezionata == null) return;
        int conferma = JOptionPane.showConfirmDialog(this,
                "Eliminare la lega '" + legaSelezionata.getNome() + "'?",
                "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conferma != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                return legaDAO.eliminaLega(legaSelezionata.getIdLega(), adminUtente.getIdUtente());
            }
            @Override protected void done() {
                try {
                    if (get()) { loadData(); }
                    else JOptionPane.showMessageDialog(AdminLeaguePanel.this, "Eliminazione non riuscita.", "Errore", JOptionPane.ERROR_MESSAGE);
                } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(AdminLeaguePanel.this, "Errore: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private void copiaCodice() {
        if (legaSelezionata == null) return;
        StringSelection sel = new StringSelection(legaSelezionata.getCodiceAccesso());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        JOptionPane.showMessageDialog(this, "Codice copiato:\n" + legaSelezionata.getCodiceAccesso(),
                "Copiato", JOptionPane.INFORMATION_MESSAGE);
    }

    private CampionatoPredefinito enumCampionato(String scelta) {
        return "Serie A".equalsIgnoreCase(scelta)
                ? CampionatoPredefinito.SERIE_A_24_25
                : CampionatoPredefinito.PREMIER_LEAGUE_24_25;
    }
    private String csvFor(String scelta) {
        return "Serie A".equalsIgnoreCase(scelta)
                ? "/calciatori_serie_a.csv"
                : "/calciatori_premier_league.csv";
    }

    // Stats getter (se servono al titolo del dialog)
    public int getNumLeghe() {
        return modelTabella.getRowCount();
    }
    public int getPartecipantiTotali() {
        int tot = 0;
        for (int r = 0; r < modelTabella.getRowCount(); r++) {
            tot += (Integer) modelTabella.getValueAt(r, 5);
        }
        return tot;
    }
}
