package oskhe.meteorextension.modules.hud;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import meteordevelopment.meteorclient.MeteorClient;
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
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Vec3;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import oskhe.meteorextension.MeteorExtension;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Iterator;

public class RadarHud extends HudElement {
    public enum AvailableCharacters {//*•◦+
        STAR("*"),
        POINT("•"),
        CIRCLE("◦"),
        PLUS("+");

        private String character = "";

        AvailableCharacters(String character) {
            this.character = character;
        }

        @Override
        public String toString() {
            return character;
        }
    }



    private final SettingGroup sgGeneral = settings.getDefaultGroup();

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

    /*private final Setting<Boolean> letters = sgGeneral.add(new BoolSetting.Builder()
            .name("letters")
            .description("Use entity's type first letter.")
            .defaultValue(true)
            .build()
    );*/

    private final Setting<Boolean> showWaypoints = sgGeneral.add(new BoolSetting.Builder()
            .name("waypoints")
            .description("Show waypoints.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> drawSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("draw-self")
        .description("Wether to draw you on the Radar")
        .defaultValue(true)
        //.visible(() -> mode.get() == ESP.Mode.Shader)
        .build()
    );



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

    private static long lastMillis = 0;

    public RadarHud(HUD hud) {
        super(hud, "Radar", "Shows a Radar on your HUD thar tells you where entities are.");
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(100 * scale.get(), 100 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        ESP esp = Modules.get().get(ESP.class);
        if (esp == null) return;

        renderer.addPostTask(() -> {
            double x = box.getX();
            double y = box.getY();

            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, box.width, box.height, backgroundColor.get());
            Renderer2D.COLOR.render(null);

            //Renderer2D.COLOR.
            //renderer.text("0", 0 + x + box.width/2, 0 + y + box.height/2, Color.WHITE);
            //renderer.text("5", 5 + x + box.width/2, 5 + y + box.height/2, Color.WHITE);

            if (drawSelf.get()) {
                renderer.text(character.get().toString(), box.width/2 + x, box.height/2 + y, esp.getColor(mc.player));
            }

            renderer.text("X", box.width/2 + x + 200, box.height/2 + y, esp.getColor(mc.player));
            renderer.text("Y", box.width/2 + x, box.height/2 + y + 200, esp.getColor(mc.player));


            //boolean printed = false;
            int num = 0;

            if (mc.world != null) {
                for (Entity entity : mc.world.getEntities()) {
                    if (shouldSkip(entity)) continue;

                    assert mc.player != null;

                    double xPos = ((entity.getX() - mc.player.getX()) * scale.get() * zoom.get()/* + box.width/2*/);
                    double yPos = ((entity.getZ() - mc.player.getZ()) * scale.get() * zoom.get()/*  + box.height/2*/);
                    //BigDecimal BDxPos = new BigDecimal(xPos);
                    //BigDecimal BDyPos = new BigDecimal(yPos);

                    /*if (xPos > 0) {
                        yPos *= -1d;
                        //xPos *= -1d;
                    }*/

                    double dist = Math.sqrt(xPos*xPos + yPos*yPos);
                    //BigDecimal dist = new BigDecimal(0);
                    //dist = BDxPos.pow(2).add(BDyPos.pow(2)).sqrt(MathContext.DECIMAL128);

                    double theta = Math.atan(yPos/xPos);
                    //BigDecimal theta = new BigDecimal(0);
                    //theta = new BigDecimal(Math.atan(BDyPos.divide(BDxPos).doubleValue()));

                    if (xPos > 0) {
                        theta += Math.PI;
                    }

                    double yaw = mc.player.getHeadYaw() % 360; //0-360
                    //BigDecimal BDyaw = new BigDecimal(mc.player.getHeadYaw());
                    theta = theta - (yaw * (Math.PI/180d));
                    //theta = theta.subtract(BDyaw.multiply(BigDecimal.valueOf(Math.PI/180d)));

                    /*if (xPos > 0) {
                        theta *= -1d;
                    }*/

                    if (num++ == 2 && System.currentTimeMillis()-lastMillis >= 200) {

                        //renderer.text("distance: " + dist, 0 + x + box.width/2, 0 + y + box.height/2, Color.WHITE);
                        //renderer.text("theta: " + theta, 0 + x + box.width/2, 20 + y + box.height/2, Color.WHITE);
                        MeteorExtension.LOG.info("xPos: {}, yPos: {}, distance: {}, theta: {}, thetaold: {}", s(xPos), s(yPos), s(dist), s(theta), s(Math.atan(yPos/xPos)));

                        lastMillis = System.currentTimeMillis();
                    }
                    //num ++;

                    //theta -= Math.PI/2;

                    double xPosOld = xPos;
                    double yPosOld = yPos;




                    xPos = dist * Math.cos(theta);
                    yPos = dist * Math.sin(theta);

                    /*if (xPosOld > 0) {
                        yPos *= -1d;
                    }*/

                    xPos += box.width/2;
                    yPos += box.height/2;

                    if (xPos < 0 || yPos < 0 || xPos > box.width - scale.get() || yPos > box.height - scale.get()) continue;

                    String icon = character.get().toString();

                    /*if (letters.get())
                        icon = entity.getType().getUntranslatedName().substring(0,1).toUpperCase();
                    */
                    renderer.text(icon, xPos + x, yPos + y, esp.getColor(entity));
                    //renderer.
                }
            }

            if (showWaypoints.get()) {
                Iterator<Waypoint> waypoints = Waypoints.get().iterator();
                while (waypoints.hasNext()) {
                    Waypoint waypoint = waypoints.next();

                    Vec3 c = waypoint.getCoords();
                    Vec3d coords = new Vec3d(c.x, c.y, c.z);

                    double xPos = ((coords.getX() - mc.player.getX()) * scale.get() * zoom.get() + box.width/2);
                    double yPos = ((coords.getZ() - mc.player.getZ()) * scale.get() * zoom.get()  + box.height/2);

                    if (xPos < 0 || yPos < 0 || xPos > box.width - scale.get() || yPos > box.height - scale.get()) continue;

                    String icon = "*";
                    /*if (letters.get() && waypoint.name.length() > 0)
                        icon = waypoint.name.substring(0, 1);*/

                    renderer.text(icon, xPos + x, yPos + y, waypoint.color);
                }
            }

            Renderer2D.COLOR.render(null);
        });

    }

    private String s(double d) {
        return "%.2f".formatted(d);
    }

    private boolean shouldSkip(Entity entity) {
        if (!entities.get().getBoolean(entity.getType())) return true;

        return !EntityUtils.isInRenderDistance(entity);
    }

}
