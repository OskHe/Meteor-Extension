package com.oskhe.meteorextension.hud;

import com.oskhe.meteorextension.MeteorExtension;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.misc.Vec2;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RadarHud extends HudElement {
    public static final HudElementInfo<RadarHud> INFO = new HudElementInfo<>(MeteorExtension.HUD_GROUP, "radar-extension", "Shows a Radar on your HUD thar tells you where entities are.", RadarHud::new);

    private static MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select entities to be drawn.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Double> scalePoints = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale-points")
        .description("The size of the Points.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );

    public final Setting<Boolean> drawSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("draw-self")
        .description("Whether  to draw you on the Radar")
        .defaultValue(true)
        .build()
    );

    private final Setting<AvailableCharacters> characterSelf = sgGeneral.add(new EnumSetting.Builder<AvailableCharacters>()
        .name("character-self")
        .description("Choose the character to be drawn as the location of your self.")
        .defaultValue(AvailableCharacters.PLUS)
        .visible(drawSelf::get)
        .build()
    );

    private final Setting<Boolean> followFreecam = sgGeneral.add(new BoolSetting.Builder()
        .name("follow-freecam")
        .description("Whether the radar center should follow the freecam position.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showWaypoints = sgGeneral.add(new BoolSetting.Builder()
        .name("waypoints")
        .description("Show waypoints.")
        .defaultValue(false)
        .build()
    );

    private final Setting<AvailableCharacters> characterWaypoints = sgGeneral.add(new EnumSetting.Builder<AvailableCharacters>()
        .name("character-waypoints")
        .description("Choose the character to be drawn as the location of the waypoint.")
        .defaultValue(AvailableCharacters.DOT)
        .visible(showWaypoints::get)
        .build()
    );

    private final Setting<Boolean> checkDimension = sgGeneral.add(new BoolSetting.Builder()
        .name("dimension-check")
        .description("Check whether the waypoint is in the current dimension")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("The color of the background.")
        .defaultValue(new SettingColor(0, 0, 0, 64))
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The size of the radar.")
        .defaultValue(1)
        .min(0.01)
        .sliderRange(0.01, 3)
        .build()
    );

    private final Setting<Double> zoom = sgGeneral.add(new DoubleSetting.Builder()
        .name("zoom")
        .description("Radar zoom.")
        .defaultValue(1)
        .min(0.01)
        .sliderRange(0.01, 5)
        .build()
    );

    public RadarHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(200 * scale.get(), 200 * scale.get());

        if (mc.player == null) return;

        ESP esp = Modules.get().get(ESP.class);
        if (esp == null) return;

        Freecam freecam = Modules.get().get(Freecam.class);
        if (freecam == null) return;

        renderer.post(() -> {
            double x = getX();
            double y = getY();

            Vec3d pos = mc.player.getPos();
            double yaw = mc.player.getHeadYaw() % 360;

            if (freecam.isActive() && followFreecam.get()) {
                pos = new Vec3d(freecam.pos.x, freecam.pos.y, freecam.pos.z);
                yaw = freecam.yaw % 360;
            }

            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, getWidth(), getHeight(), backgroundColor.get());
            Renderer2D.COLOR.render(null);

            drawEntities(pos, yaw);

            drawWaypoints(pos, yaw, renderer);

            if (drawSelf.get()) {
                Vec2 center = calcTextOffset(characterSelf.get().toString(), new Vec2(x, y), renderer);
                center.x += getWidth()/2d;
                center.y += getHeight()/2d;
                renderer.text(characterSelf.get().toString(), center.x, center.y, esp.getEntityTypeColor(mc.player), false, 1);
            }

        });
    }

    private void drawEntities(Vec3d posOrigin, double yaw) {
        ESP esp = Modules.get().get(ESP.class);
        if (esp == null) return;

        double x = getX();
        double y = getY();

        if (mc.world != null) {
            Renderer2D.COLOR.begin();
            for (Entity entity : mc.world.getEntities()) {
                if (shouldSkip(entity)) continue;

                Vec2 pos = new Vec2(0, 0);

                pos.x = ((entity.getX() - posOrigin.getX()) * scale.get() * zoom.get());
                pos.y = ((entity.getZ() - posOrigin.getZ()) * scale.get() * zoom.get());

                pos = rotate(pos, yaw);

                pos.x += getWidth()/2d;
                pos.y += getHeight()/2d;

                if (pos.x < 0 || pos.y < 0 || pos.x > getWidth() || pos.y > getHeight()) continue;

                Renderer2D.COLOR.quad((pos.x - scalePoints.get()/2) + x,
                    (pos.y - scalePoints.get()/2) + y,
                    scalePoints.get(),
                    scalePoints.get(),
                    esp.getEntityTypeColor(entity).a(255));
            }

            Renderer2D.COLOR.render(null);
        }
    }

    private void drawWaypoints(Vec3d posOrigin, double yaw, HudRenderer renderer) {
        double x = getX();
        double y = getY();

        if (showWaypoints.get()) {
            for (Waypoint waypoint : Waypoints.get()) {

                if (checkDimension.get())
                    if (!shouldRender(waypoint))
                        continue;

                BlockPos waypointPos = waypoint.getPos();

                Vec2 pos = new Vec2(0, 0);

                pos.x = ((waypointPos.getX() - posOrigin.getX()) * scale.get() * zoom.get());
                pos.y = ((waypointPos.getZ() - posOrigin.getZ()) * scale.get() * zoom.get());

                pos = rotate(pos, yaw);

                String symbol = characterWaypoints.get().toString();

                pos.x += getWidth()/2d;
                pos.y += getHeight()/2d;

                pos = calcTextOffset(symbol, pos, renderer);

                if (pos.x < 0 || pos.y < 0 || pos.x > getWidth() || pos.y > getHeight()) continue;

                renderer.text(symbol, pos.x + x, pos.y + y, waypoint.color.get(), false, 1);
            }
        }
    }

    private Vec2 rotate(Vec2 vec, double angle) {
        Vec2 ret = new Vec2(0, 0);

        double dist = Math.sqrt(vec.x*vec.x + vec.y*vec.y);

        double theta = Math.atan(vec.y / vec.x);

        if (vec.x > 0) {
            theta += Math.PI;
        }

        theta = theta - (angle * (Math.PI / 180f));

        ret.x = dist * Math.cos(theta);
        ret.y = dist * Math.sin(theta);

        return ret;
    }

    private Vec2 calcTextOffset(String symbol, Vec2 pos, HudRenderer renderer) {
        Vec2 ret = new Vec2(pos.x, pos.y);

        ret.x -= renderer.textWidth(symbol) / 2;
        ret.y -= renderer.textHeight() / 2;

        return ret;
    }

    private boolean shouldSkip(Entity entity) {
        return !entities.get().getBoolean(entity.getType());
    }

    private boolean shouldRender(Waypoint waypoint) {
        if (waypoint.dimension.get().equals(PlayerUtils.getDimension())) return true;
        if (waypoint.opposite.get()
            && !PlayerUtils.getDimension().equals(Dimension.End)) return true;

        return false;
    }

    public enum AvailableCharacters {
        POINT("•"),
        CIRCLE("◦"),
        PLUS("+"),
        DOT("●");

        private final String character;

        AvailableCharacters(String character) {
            this.character = character;
        }

        @Override
        public String toString() {
            return character;
        }
    }
}
