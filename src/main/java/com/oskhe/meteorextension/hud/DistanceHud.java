package com.oskhe.meteorextension.hud;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.MeteorTextHud;
import com.oskhe.meteorextension.MeteorExtension;

public class DistanceHud extends HudElement {
    public static final HudElementInfo<DistanceHud> INFO = new HudElementInfo<DistanceHud>(MeteorExtension.HUD_GROUP, "distance", "Measures the distance to the Point you are looking at.", DistanceHud::new);


    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private static final String left = "Distance: ";

    public final Setting<Boolean> exact = sgGeneral.add(new BoolSetting.Builder()
        .name("exact")
        .description("Wether to show the exact distance")
        .defaultValue(true)
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

    public DistanceHud(HudElementInfo<?> info) {
        super(info);
    }

    public DistanceHud() {
        super(INFO);
    }


    /*public final Setting<Boolean> hideLeft = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-left")
        .description("Wether to show the left text")
        .defaultValue(false)
        .onChanged(on -> {
            if (on)
                setLeft("");
            else
                setLeft(left);
        })
        .build()
    );*/

    /*public DistanceHud(HUD hud) {
        super(hud, "distance", "Measures the distance to the Point you are looking at.", left);
        //super();
    }*/

    /*@Override
    protected String getRight() {
        if (mc.player == null) return "0";

        HitResult target = mc.player.raycast(maxDistance.get(), 0, includeFluids.get());

        double dist = target.getPos().distanceTo(mc.player.getPos());

        String distance = String.valueOf((int)dist);

        if (exact.get()) {
            distance = "%.3f".formatted(dist);
        }

        return distance;
    }*/
}
