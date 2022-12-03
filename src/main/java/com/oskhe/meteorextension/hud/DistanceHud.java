package com.oskhe.meteorextension.hud;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import com.oskhe.meteorextension.MeteorExtension;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.hit.HitResult;

public class DistanceHud extends HudElement {
    public static final HudElementInfo<DistanceHud> INFO = new HudElementInfo<>(MeteorExtension.HUD_GROUP, "distance", "Measures the distance to the Point you are looking at.", DistanceHud::new);


    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private static final String left = "Distance: ";

    public final Setting<Integer> length = sgGeneral.add(new IntSetting.Builder()
        .name("length")
        .description("The number of digits behind the decimal separator.")
        .defaultValue(2)
        .range(0, 15)
        .sliderRange(0, 15)
        .build()
    );

    public final Setting<String> textPrimary = sgGeneral.add(new StringSetting.Builder()
        .name("text")
        .description("The primary Text")
        .defaultValue("Distance: ")
        .build()
    );

    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-distance")
        .description("The max distance to look for.")
        .defaultValue(20)
        .min(0.01)
        .sliderRange(20, 100)
        .build()
    );

    public final Setting<Boolean> includeFluids = sgGeneral.add(new BoolSetting.Builder()
        .name("include-fluids")
        .description("Wether to include fluids in the distance measuring")
        .defaultValue(true)
        .build()
    );

    public final Setting<SettingColor> colorPrimary = sgGeneral.add(new ColorSetting.Builder()
        .name("color-primary")
        .description("The primary color of the Text.")
        .defaultValue(Color.WHITE.toSetting())
        .build()
    );

    public final Setting<SettingColor> colorSecondary = sgGeneral.add(new ColorSetting.Builder()
        .name("color-secondary")
        .description("The secondary color of the Text.")
        .defaultValue(Color.LIGHT_GRAY.toSetting())
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> size = sgGeneral.add(new DoubleSetting.Builder()
        .name("size")
        .description("The size.")
        .defaultValue(1)
        .min(0.01)
        .sliderRange(0.5, 5)
        .build()
    );

    public DistanceHud(HudElementInfo<?> info) {
        super(info);
    }

    public DistanceHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;

        String primary = textPrimary.get();
        String secondary = String.format("%." + length.get() + "f", calcDist());

        setSize(renderer.textWidth(primary, shadow.get(), size.get()) + renderer.textWidth(secondary, shadow.get(), size.get()),
            renderer.textHeight(shadow.get(), size.get()));

        renderer.text(primary, x, y, colorPrimary.get(), shadow.get(), size.get());

        double offset = renderer.textWidth(textPrimary.get(), shadow.get(), size.get());

        renderer.text(secondary, x + offset, y, colorSecondary.get(), shadow.get(), size.get());
    }

    private double calcDist() {
        if (MeteorClient.mc.player == null) return 0d;

        HitResult target = MeteorClient.mc.player.raycast(maxDistance.get(), 0, includeFluids.get());

        return target.getPos().distanceTo(MeteorClient.mc.player.getPos());
    }
}
