package oskhe.meteorextension;

import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.commands.Commands;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.item.Items;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oskhe.meteorextension.modules.hud.LookingDirectionHud;
import oskhe.meteorextension.modules.hud.RadarHud;

import java.lang.invoke.MethodHandles;

public class MeteorExtension extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger(MeteorExtension.class);
    public static final Category CATEGORY_MAIN = new Category("Extension", Items.EXPERIENCE_BOTTLE.getDefaultStack());

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon Meteor-Extension");

        // Required when using @EventHandler
        MeteorClient.EVENT_BUS.registerLambdaFactory("oskhe.meteorextension", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        //Modules modules = Systems.get(Modules.class);

        //Commands commands = Systems.get(Commands.class);

        // HUD
        HUD hud = Systems.get(HUD.class);
        hud.elements.add(new RadarHud(hud));
        //hud.elements.add(new LookingDirectionHud(hud));
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY_MAIN);
    }

    @Override
    public String getWebsite() {
        return "https://github.com/oskhe/meteor-extension/";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("OskHe", "Meteor-Extension", "main");
    }

    @Override
    public String getCommit() {
        ModMetadata metedata = FabricLoader
            .getInstance()
            .getModContainer("meteor-extension")
            .get().getMetadata();

        if (!metedata.containsCustomValue("github:sha"))
            return null;

        String commit = metedata.getCustomValue("github:sha").getAsString();

        return commit.isEmpty() ? null : commit.trim();
    }
}
