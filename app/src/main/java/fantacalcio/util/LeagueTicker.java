package fantacalcio.util;

public final class LeagueTicker {
    private final int idLega;
    private final javax.swing.Timer timer;

    private int secondsLeft;
    private final int deadlineAtSecondsLeft;

    public interface Listener {
        void onTick(int secondsLeft);
        void onDeadlineFormazioni();
        void onStartSimulazione();
    }

    /**
     * @param idLega id lega
     * @param totalSeconds secondi totali (per 2 minuti: 120)
     * @param deadlineAtSecondsLeft scatta quando restano questi secondi (per 30s: 30)
     */
    public LeagueTicker(int idLega, int totalSeconds, int deadlineAtSecondsLeft, Listener l) {
        this.idLega = idLega;
        this.secondsLeft = totalSeconds;
        this.deadlineAtSecondsLeft = deadlineAtSecondsLeft;

        this.timer = new javax.swing.Timer(1000, e -> {
            secondsLeft = Math.max(0, secondsLeft - 1);
            l.onTick(secondsLeft);

            if (secondsLeft == deadlineAtSecondsLeft) {
                l.onDeadlineFormazioni();
            }
            if (secondsLeft == 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                l.onStartSimulazione();
            }
        });
    }

    public void start() { timer.start(); }
    public void stop()  { timer.stop();  }

    public int getSecondsLeft() { return secondsLeft; }
}
