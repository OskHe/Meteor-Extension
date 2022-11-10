package com.oskhe.meteorextension.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.command.CommandSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ModuleBindsCommand  extends Command {
    public ModuleBindsCommand() {
        super("module-binds", "prints all Modules grouped by their keybinding");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            //info("hi");
            Map<String, String> map = new HashMap<>();

            List<Keybind> binds = Modules.get().getAll().stream().map(module -> module.keybind).collect(Collectors.toList());

            /*for (Keybind bind : binds) {
                if(map.get(bind.toString())) {

                }
            }*/


            return SINGLE_SUCCESS;
        });
    }


}
