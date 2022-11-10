package com.oskhe.meteorextension;

import com.oskhe.meteorextension.commands.ModuleBindsCommand;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.mixin.StarscriptAccessor;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.Starscript;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.oskhe.meteorextension.hud.DistanceHud;
import com.oskhe.meteorextension.hud.RadarHud;

import java.lang.invoke.MethodHandles;

public class MeteorExtension extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger(MeteorExtension.class);
    public static final Category CATEGORY_MAIN = new Category("Extension", Items.EXPERIENCE_BOTTLE.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("Extension");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor-Extension");

        // Required when using @EventHandler
        //MeteorClient.EVENT_BUS.registerLambdaFactory("com.oskhe.meteorextension", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        //     __o
        //    / /\_
        //    _/\
        //      /

        Hud.get().register(RadarHud.INFO);
        Hud.get().register(DistanceHud.INFO);

        //Commands.get().add(new ModuleBindsCommand());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY_MAIN);
    }

    @Override
    public String getPackage() {
        return "com.oskhe.meteorextension";
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
