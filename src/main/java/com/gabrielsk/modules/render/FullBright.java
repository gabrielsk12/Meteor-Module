package com.gabrielsk.modules.render;

import com.gabrielsk.GabrielSKAddon;
import meteordevelopment.meteorclient.systems.modules.Module;

public class FullBright extends Module {
    private double oldGamma;
    
    public FullBright() {
        super(GabrielSKAddon.CATEGORY, "fullbright", "Makes everything bright.");
    }
    
    @Override
    public void onActivate() {
        if (mc.options != null) {
            oldGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(16.0);
        }
    }
    
    @Override
    public void onDeactivate() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(oldGamma);
        }
    }
}
