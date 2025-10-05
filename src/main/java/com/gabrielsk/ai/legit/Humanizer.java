package com.gabrielsk.ai.legit;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utilities for human-like behavior: reaction times, CPS pacing, and jitter.
 */
public final class Humanizer {
    private Humanizer() {}

    /**
     * Randomized human reaction time in milliseconds.
     * Returns a value around baseMs with +/- variancePct (0.0-1.0) applied.
     */
    public static long reactionMs(long baseMs, double variancePct) {
        if (baseMs <= 0) return 0L;
        double v = clamp01(variancePct);
        double delta = (ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * v; // [-v, +v]
        return Math.max(0L, Math.round(baseMs * (1.0 + delta)));
    }

    /**
     * Returns a delay between clicks in ms based on a CPS range and jitter.
     * cpsMin/cpsMax in clicks per second. JitterPct 0.0-1.0.
     */
    public static long clickDelayMs(double cpsMin, double cpsMax, double jitterPct) {
        double min = Math.max(0.1, Math.min(cpsMin, cpsMax));
        double max = Math.max(min, Math.max(cpsMin, cpsMax));
        double cps = min + ThreadLocalRandom.current().nextDouble() * (max - min);
        long base = (long) Math.max(1.0, Math.round(1000.0 / cps));
        long jitter = (long) Math.round(base * clamp01(jitterPct) * (ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0));
        return Math.max(1L, base + jitter);
    }

    /** Clamp to [0,1]. */
    public static double clamp01(double v) {
        return v < 0 ? 0 : Math.min(1.0, v);
    }
}
