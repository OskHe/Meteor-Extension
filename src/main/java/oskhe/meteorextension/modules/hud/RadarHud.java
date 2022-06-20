package oskhe.meteorextension.modules.hud;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.modules.HudElement;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.misc.Vec2;
import meteordevelopment.meteorclient.utils.misc.Vec3;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;

public class RadarHud extends HudElement {

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

    private final Setting<AvailableCharacters> character = sgGeneral.add(new EnumSetting.Builder<AvailableCharacters>()
        .name("character")
        .description("Choose the character to be drawn as the location of the entity.")
        .defaultValue(AvailableCharacters.STAR)
        .build()
    );

    private final Setting<Boolean> letters = sgGeneral.add(new BoolSetting.Builder()
        .name("letters")
        .description("Use entity's type first letter.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select entities to be drawn.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("The color of the background.")
        .defaultValue(new SettingColor(0, 0, 0, 64))
        .build()
    );

    public final Setting<Boolean> drawSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("draw-self")
        .description("Wether to draw you on the Radar")
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

    private final Setting<Boolean> followFreecam = sgGeneral.add(new BoolSetting.Builder()
        .name("follow-freecam")
        .description("Wether the radar center should follow the freecam position.")
        .defaultValue(true)
        .build()
    );

    /*private final Setting<Boolean> showFOV = sgGeneral.add(new BoolSetting.Builder()
        .name("show-fow")
        .description("Wether to show the FOV")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> fovColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("The color of the background.")
        .defaultValue(new SettingColor(128, 128, 128, 64))
        .visible(showFOV::get)
        .build()
    );*/

    public RadarHud(HUD hud) {
        super(hud, "Radar", "Shows a Radar on your HUD thar tells you where entities are.");
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(200 * scale.get(), 200 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.player == null) return;

        ESP esp = Modules.get().get(ESP.class);
        if (esp == null) return;

        Freecam freecam = Modules.get().get(Freecam.class);
        if (freecam == null) return;

        renderer.addPostTask(() -> {
            double x = box.getX();
            double y = box.getY();

            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, box.width, box.height, backgroundColor.get());
            Renderer2D.COLOR.render(null);

            Vec3d pos = mc.player.getPos();
            double yaw = mc.player.getHeadYaw() % 360;

            if (freecam.isActive() && followFreecam.get()) {
                pos = new Vec3d(freecam.pos.x, freecam.pos.y, freecam.pos.z);
                yaw = freecam.yaw;
            }

            if (drawSelf.get()) {
                renderer.text(character.get().toString(), box.width/2 + x, box.height/2 + y, esp.getColor(mc.player));
            }

            /*if (showFOV.get()) {
                Utils.unscaledProjection();

                //Renderer2D.COLOR.triangles.begin();
                Renderer2D.COLOR.triangles.vec2(box.width / 2 + x, box.height / 2 + y);
                Renderer2D.COLOR.triangles.vec2(x, y);
                Renderer2D.COLOR.triangles.vec2(box.width + x, y);
                Renderer2D.COLOR.triangles.end();
            }*/

            drawEntities(pos, yaw, renderer);

            drawWaypoints(pos, yaw, renderer);

            Renderer2D.COLOR.render(null);
        });
    }

    private boolean shouldSkip(Entity entity) {
        //if (!entities.get().getBoolean(entity.getType())) return true;
        //return !EntityUtils.isInRenderDistance(entity);
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

        double x = box.getX();
        double y = box.getY();

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

                newPos.x += box.width/2/* - (renderer.textWidth(icon) / 2)*/;
                newPos.y += box.height/2/* - (renderer.textHeight() / 2)*/;

                if (newPos.x < 0 || newPos.y < 0 || newPos.x > box.width - scale.get() || newPos.y > box.height - scale.get()) continue;

                renderer.text(icon, newPos.x + x, newPos.y + y, esp.getColor(entity));
            }
        }
    }

    private void drawWaypoints(Vec3d pos, double yaw, HudRenderer renderer) {
        double x = box.getX();
        double y = box.getY();

        if (showWaypoints.get()) {
            for (Waypoint waypoint : Waypoints.get()) {
                Vec3 c = waypoint.getCoords();
                Vec3d coords = new Vec3d(c.x, c.y, c.z);

                double xPos = ((coords.getX() - pos.getX()) * scale.get() * zoom.get());
                double yPos = ((coords.getZ() - pos.getZ()) * scale.get() * zoom.get());

                Vec2 newPos = rotate(new Vec2(xPos, yPos), yaw);

                String icon = characterWaypoints.get().toString();

                if (letters.get() && waypoint.name.length() > 0)
                    icon = waypoint.name.substring(0, 1);

                newPos.x += box.width/2/* - (renderer.textWidth(icon) / 2)*/;
                newPos.y += box.height/2/* - (renderer.textHeight() / 2)*/;

                if (newPos.x < 0 || newPos.y < 0 || newPos.x > box.width - scale.get() || newPos.y > box.height - scale.get()) continue;

                renderer.text(icon, newPos.x + x, newPos.y + y, waypoint.color);
            }
        }
    }

    public enum AvailableCharacters {
        STAR("*"),
        POINT("•"),
        CIRCLE("◦"),
        PLUS("+"),
        DOT("●");

        private String character = "";

        AvailableCharacters(String character) {
            this.character = character;
        }

        @Override
        public String toString() {
            return character;
        }
    }
}
