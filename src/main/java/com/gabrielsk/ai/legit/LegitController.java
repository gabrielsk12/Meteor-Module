package com.gabrielsk.ai.legit;

/**
 * Shared per-module legit controller for pacing and limits.
 */
public class LegitController {
    private long lastActionAt = 0L;
    private long minDelayMs = 50L;

    public LegitController() {}

    public LegitController withMinDelayMs(long ms) {
        this.minDelayMs = Math.max(0L, ms);
        return this;
    }

    /** Returns true if enough time elapsed since last allowed action. */
    public boolean canAct() {
        long now = System.currentTimeMillis();
        return now - lastActionAt >= minDelayMs;
    }

    /** Mark an action just happened, applying humanized delay for next time. */
    public void markActed(long baseMs, double variancePct) {
        long delay = Humanizer.reactionMs(baseMs, variancePct);
        this.minDelayMs = Math.max(1L, delay);
        this.lastActionAt = System.currentTimeMillis();
    }

    /** Configure using CPS range and jitter. */
    public void useCps(double cpsMin, double cpsMax, double jitterPct) {
        this.minDelayMs = Humanizer.clickDelayMs(cpsMin, cpsMax, jitterPct);
    }
}
