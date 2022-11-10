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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
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

    private final Setting<Boolean> letters = sgGeneral.add(new BoolSetting.Builder()
        .name("letters")
        .description("Use entity's type first letter.")
        .defaultValue(false)
        .build()
    );

    private final Setting<AvailableCharacters> character = sgGeneral.add(new EnumSetting.Builder<AvailableCharacters>()
        .name("character")
        .description("Choose the character to be drawn as the location of the entity.")
        .defaultValue(AvailableCharacters.STAR)
        .visible(() -> !letters.get())
        .build()
    );

    public final Setting<Boolean> drawSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("draw-self")
        .description("Wether to draw you on the Radar")
        .defaultValue(true)
        .build()
    );

    private final Setting<AvailableCharacters> characterSelf = sgGeneral.add(new EnumSetting.Builder<AvailableCharacters>()
        .name("character-self")
        .description("Choose the character to be drawn as the location of your self.")
        .defaultValue(AvailableCharacters.DOT)
        .visible(drawSelf::get)
        .build()
    );

    private final Setting<Boolean> followFreecam = sgGeneral.add(new BoolSetting.Builder()
        .name("follow-freecam")
        .description("Wether the radar center should follow the freecam position.")
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

    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("The color of the background.")
        .defaultValue(new SettingColor(0, 0, 0, 64))
        .build()
    );

    private final Setting<Double> scaleCharacterd = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale-characters")
        .description("The size of the characters.")
        .defaultValue(1)
        .min(0.1)
        .sliderRange(0.01, 3)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The size of the radar.")
        .defaultValue(1)
        .min(0.1)
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
                yaw = freecam.yaw;
            }

            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, getWidth(), getHeight(), backgroundColor.get());
            Renderer2D.COLOR.render(null);

            drawEntities(pos, yaw, renderer);

            drawWaypoints(pos, yaw, renderer);

            if (drawSelf.get()) {
                renderer.text(characterSelf.get().toString(), getWidth()/2d + x, getHeight()/2d + y, esp.getEntityTypeColor(mc.player), false, scaleCharacterd.get());
            }
        });
    }

    private boolean shouldSkip(Entity entity) {
        return !entities.get().getBoolean(entity.getType());
    }

    private Vec2 rotate(Vec2 vec, double angle) {
        double dist = Math.sqrt(vec.x*vec.x + vec.y*vec.y);

        double theta = Math.atan(vec.y / vec.x);

        if (vec.x > 0) {
            theta += Math.PI;
        }

        theta = theta - (angle * (Math.PI / 180f));

        vec.x = dist * Math.cos(theta);
        vec.y = dist * Math.sin(theta);

        return vec;
    }

    private void drawEntities(Vec3d pos, double yaw, HudRenderer renderer) {
        ESP esp = Modules.get().get(ESP.class);
        if (esp == null) return;

        double x = getX();
        double y = getY();

        if (mc.world != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (shouldSkip(entity)) continue;

                assert mc.player != null;

                //entity.pos

                double xPos = ((entity.getX() - pos.getX()) * scale.get() * zoom.get());
                double yPos = ((entity.getZ() - pos.getZ()) * scale.get() * zoom.get());

                Vec2 newPos = rotate(new Vec2(xPos, yPos), yaw);

                String icon = character.get().toString();

                if (letters.get())
                    icon = entity.getType().getUntranslatedName().substring(0,1).toUpperCase();

                newPos.x += getWidth()/2d/* - (renderer.textWidth(icon) / 2)*/;
                newPos.y += getHeight()/2d/* - (renderer.textHeight() / 2)*/;

                if (newPos.x < 0 || newPos.y < 0 || newPos.x > getWidth() - scale.get() || newPos.y > getHeight() - scale.get()) continue;

                renderer.text(icon, newPos.x + x, newPos.y + y, esp.getEntityTypeColor(entity), false, scaleCharacterd.get());
            }
        }
    }

    private void drawWaypoints(Vec3d pos, double yaw, HudRenderer renderer) {
        double x = getX();
        double y = getY();

        if (showWaypoints.get()) {
            for (Waypoint waypoint : Waypoints.get()) {
                BlockPos c = waypoint.getPos();
                Vec3d coords = new Vec3d(c.getX(), c.getY(), c.getZ());

                double xPos = ((coords.getX() - pos.getX()) * scale.get() * zoom.get());
                double yPos = ((coords.getZ() - pos.getZ()) * scale.get() * zoom.get());

                Vec2 newPos = rotate(new Vec2(xPos, yPos), yaw);

                String icon = characterWaypoints.get().toString();

                if (letters.get() && waypoint.name.get().length() > 0)
                    icon = waypoint.name.get().substring(0, 1);

                newPos.x += getWidth()/2d/* - (renderer.textWidth(icon) / 2)*/;
                newPos.y += getHeight()/2d/* - (renderer.textHeight() / 2)*/;

                //SettingColor color = waypoint.color.get();

                if (newPos.x < 0 || newPos.y < 0 || newPos.x > getWidth() - scale.get() || newPos.y > getHeight() - scale.get()) continue;

                renderer.text(icon, newPos.x + x, newPos.y + y, waypoint.color.get(), false, scaleCharacterd.get());
            }
        }
    }

    public enum AvailableCharacters {
        STAR("*"),
        POINT("•"),
        LITTLE_POINT("."),
        CIRCLE("◦"),
        PLUS("+"),
        DOT("●");

        private String character;

        AvailableCharacters(String character) {
            this.character = character;
        }

        @Override
        public String toString() {
            return character;
        }
    }
}
