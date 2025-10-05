package com.gabrielsk.modules.automation;

import net.minecraft.client.MinecraftClient;

/** A simple task interface for automation bots. */
public interface BotTask {
    /**
     * Called every tick. Return true if an action was performed.
     */
    boolean tick(MinecraftClient mc);

    /** Human-readable name. */
    String name();
}
