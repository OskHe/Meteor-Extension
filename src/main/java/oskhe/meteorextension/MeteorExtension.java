package oskhe.meteorextension;

import meteordevelopment.meteorclient.systems.Systems;
import oskhe.meteorextension.commands.ExampleCommand;
import oskhe.meteorextension.modules.AnotherExample;
import oskhe.meteorextension.modules.Example;
import oskhe.meteorextension.modules.hud.HudExample;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oskhe.meteorextension.modules.hud.RadarHud;

import java.lang.invoke.MethodHandles;

public class MeteorExtension extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger(MeteorExtension.class);
    //public static final Category CATEGORY_MAIN = new Category("Extension");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon Meteor-Extension");

        // Required when using @EventHandler
        MeteorClient.EVENT_BUS.registerLambdaFactory("oskhe.meteorextension", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        // Modules
        //Modules.get().add(new Example());
        //Modules.get().add(new AnotherExample());

        // Commands
        //Commands.get().add(new ExampleCommand());

        // HUD
        HUD hud = Systems.get(HUD.class);
        //HUD.get().elements.add(new HudExample());
        hud.elements.add(new RadarHud(hud));
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
        return "";
    }
}
