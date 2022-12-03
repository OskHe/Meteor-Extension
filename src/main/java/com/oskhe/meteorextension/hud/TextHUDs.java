package com.oskhe.meteorextension.hud;

import com.oskhe.meteorextension.MeteorExtension;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;

public class TextHUDs {
    public static final HudElementInfo<TextHud> INFO = new HudElementInfo<>(MeteorExtension.HUD_GROUP,
        "extension-text", "Displays arbitrary text with Starscript.", TextHUDs::create);

    static {
        addPreset("distance", "Distance: #1{extension.distance}");
    }

    private static TextHud create() {
        return new TextHud(INFO);
    }

    private static HudElementInfo<TextHud>.Preset addPreset(String title, String text) {
        return INFO.addPreset(title, textHud -> {
            if (text != null) textHud.text.set(text);
        });
    }
}
