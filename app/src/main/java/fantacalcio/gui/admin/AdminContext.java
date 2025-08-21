package fantacalcio.gui.admin;

import fantacalcio.model.Lega;
import fantacalcio.model.Utente;

public interface AdminContext {
    Utente getAdminUtente();
    void notifyLegheChanged();
    void notifySquadreChanged();
    void notifyCalciatoriChanged();
    void openLeagueDetail(Lega lega);
}