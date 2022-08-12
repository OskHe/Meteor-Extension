package com.oskhe.meteorextension.hud;

import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.MinecraftClient;
import com.oskhe.meteorextension.MeteorExtension;

public class InFOVHud extends HudElement {
    public static final HudElementInfo<InFOVHud> INFO = new HudElementInfo<InFOVHud>(MeteorExtension.HUD_GROUP, "in-fov", "Shows you if you are currnetly in the FOV of someone.", InFOVHud::new);

    private static MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0.01, 5)
        .build()
    );

    private final Setting<Double> scaleText = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale-text")
        .description("The scale of the text.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0.01, 5)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("The color of the background.")
        .defaultValue(new SettingColor(0, 0, 0, 64))
        .build()
    );

    private final Setting<SettingColor> foregroundColor = sgGeneral.add(new ColorSetting.Builder()
        .name("foreground-color")
        .description("The color of the foreground.")
        .defaultValue(new SettingColor(255, 255, 255, 128))
        .build()
    );

    public InFOVHud() {
        super(INFO);
    }

    /*@Override
    public void update(HudRenderer renderer) {
        box.setSize(100 * scale.get(), 200 * scale.get());
    }*/

    /*@Override
    public void render(HudRenderer renderer) {

        if (mc.player == null) return;
        if (mc.world == null) return;

        double x = box.getX();
        double y = box.getY();

        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x, y, box.width, box.height, backgroundColor.get());
        Renderer2D.COLOR.render(null);

        renderer.addPostTask(() -> {
            int count = 0;

            for (Entity entity : mc.world.getEntities()) {
                if (!entity.isPlayer()) continue;

                //entity
                PlayerEntity player = (PlayerEntity) entity;

                if (player.canSee(mc.player))
                    count++;
            }

            HudRenderer renderer2 = new HudRenderer();
            renderer2.begin(scale.get(), 0, false);
            renderer2.text(count + "", x, y, foregroundColor.get());
            renderer2.end();
        });
    }*/
}
