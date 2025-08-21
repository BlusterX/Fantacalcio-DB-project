package fantacalcio.gui.admin.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import fantacalcio.gui.admin.AdminContext;
import fantacalcio.gui.admin.panels.AdminLeaguePanel;
import fantacalcio.gui.admin.panels.CalciatoriPanel;
import fantacalcio.gui.admin.panels.SquadrePanel;
import fantacalcio.gui.user.UserMainFrame;
import fantacalcio.model.Lega;
import fantacalcio.model.Utente;

public class AdminLeagueDialog extends JDialog implements AdminContext {
    private final Utente adminUtente;
    /** Se non null, dialog “scoped” a questa lega (filtra per ID_Campionato) */
    private final Lega legaScope;

    private JTabbedPane tabbedPane;
    // Panels
    private AdminLeaguePanel adminLeaguePanel;
    private SquadrePanel squadrePanel;
    private CalciatoriPanel calciatoriPanel;
    /** Dialog “globale” (nessun filtro) */
    public AdminLeagueDialog(Frame parent, Utente adminUtente) {
        this(parent, adminUtente, null);
    }
    /** Dialog “scoped” su una lega (filtra per campionato della lega) */
    public AdminLeagueDialog(Frame parent, Utente adminUtente, Lega legaScope) {
        super(parent, "Admin Panel - Gestione Leghe", true);
        this.adminUtente = adminUtente;
        this.legaScope = legaScope;
        initializeGUI();
        setLocationRelativeTo(parent);
    }
    private void initializeGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1200, 700));
        createTabbedPane();
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                loadCurrentTabData();
                updateTitle();
            }
        });

        pack();
    }
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        Integer campionatoFilter = (legaScope != null) ? legaScope.getIdCampionato() : null;

        if (legaScope == null) {
            adminLeaguePanel = new AdminLeaguePanel(this, adminUtente);
            tabbedPane.addTab("🏆 Gestione Leghe", adminLeaguePanel);
        } else {
            squadrePanel    = new SquadrePanel(this, campionatoFilter);
            calciatoriPanel = new CalciatoriPanel(this, squadrePanel, campionatoFilter);

            tabbedPane.addTab("⚽ Squadre",    squadrePanel);
            tabbedPane.addTab("👤 Calciatori", calciatoriPanel);
        }
        tabbedPane.addChangeListener(e -> {
            loadCurrentTabData();
            updateTitle();
        });
        add(tabbedPane, BorderLayout.CENTER);
    }
    /** Carica i dati della tab attualmente selezionata */
    private void loadCurrentTabData() {
        var sel = tabbedPane.getSelectedComponent();
        if (sel == adminLeaguePanel) {
            adminLeaguePanel.loadData();
        } else if (sel == squadrePanel) {
            squadrePanel.loadData();
        } else if (sel == calciatoriPanel) {
            calciatoriPanel.refreshSquadreCombo();
            calciatoriPanel.loadData();
        }
    }
    /** Aggiorna il titolo con i contatori correnti + eventuale scope */
    public void updateTitle() {

        String scope = (legaScope != null)
                ? " — " + legaScope.getNome()
                : "";
        setTitle(String.format("Admin Panel%s - %s",
                scope, adminUtente.getNickname()));
    }

    // ---------- AdminContext (callback dai pannelli) ----------

    @Override
    public Utente getAdminUtente() {
        return adminUtente;
    }

    @Override
    public void notifyLegheChanged() {
        if (adminLeaguePanel != null) adminLeaguePanel.loadData();
        updateTitle();
    }

    @Override
    public void notifySquadreChanged() {
        if (squadrePanel != null) squadrePanel.loadData();
        if (calciatoriPanel != null) calciatoriPanel.refreshSquadreCombo();
        updateTitle();
    }

    @Override
    public void notifyCalciatoriChanged() {
        if (calciatoriPanel != null) calciatoriPanel.loadData();
        updateTitle();
    }

    @Override
    public void openLeagueDetail(Lega lega) {
        // chiudi il dialog e apri il dettaglio nel main frame (se l’owner è UserMainFrame)
        dispose();
        if (getOwner() instanceof UserMainFrame f) {
            f.openLeagueDetail(lega);
        }
    }
}
