package oskhe.meteorextension.modules.hud;

import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class LookingDirectionHud extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0.01, 5)
        .build()
    );

    private final Setting<Double> zoom = sgGeneral.add(new DoubleSetting.Builder()
        .name("zoom")
        .description("Radar zoom.")
        .defaultValue(1)
        .min(0.01)
        .sliderRange(0.01, 3)
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
        .defaultValue(new SettingColor(128, 128, 128, 255))
        .build()
    );

    public final Setting<Boolean> drawSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("draw-self")
        .description("Wether to draw you on the Radar")
        .defaultValue(true)
        .build()
    );

    /*private final Setting<Boolean> showWaypoints = sgGeneral.add(new BoolSetting.Builder()
        .name("waypoints")
        .description("Show waypoints.")
        .defaultValue(false)
        .build()
    );*/

    private final Setting<Boolean> followFreecam = sgGeneral.add(new BoolSetting.Builder()
        .name("follow-freecam")
        .description("Wether the radar center should follow the freecam position.")
        .defaultValue(true)
        .build()
    );

    public LookingDirectionHud(HUD hud) {
        super(hud, "Looking-Direction", "Shows you in which direction you are looking.");
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(100 * scale.get(), 10 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.player == null) return;

        Freecam freecam = Modules.get().get(Freecam.class);
        if (freecam == null) return;

        renderer.addPostTask(() -> {
            double x = box.getX();
            double y = box.getY();

            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, box.width, box.height, backgroundColor.get());
            Renderer2D.COLOR.render(null);

            renderer.begin(scale.get() * 2, 0, false);

            double yaw = mc.player.getHeadYaw() % 360;

            double xPos = x + box.width/2;
            double yPos = y + box.height/2;

            renderer.text("%.3f".formatted(yaw), xPos, yPos, foregroundColor.get());

            Renderer2D.COLOR.render(null);
        });
    }

    private void renderLine(float space, double pos) {
        /*if () {

        }*/

    }
}
