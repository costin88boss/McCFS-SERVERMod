package com.costin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class ServerMain implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("mccfs_servermod");
    public static boolean DEV_MODE = false;
    private static EventManager eventManager;
    private static boolean isServerOn;

    public static EventManager getEventManager() {
        return eventManager;
    }

    public static boolean isServerOn() {
        return isServerOn;
    }

    @Override
    public void onInitialize() {
        isServerOn = true;
        String botToken = System.getenv("McCFS");
        new DiscordBotMain(botToken).start();

        eventManager = new EventManager();

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            isServerOn = false;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> backupWorldCmd = CommandManager.literal("backupworld")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(context -> {
                                ServerCommandSource source = context.getSource();

                                ServerPlayerEntity player = source.getPlayer();
                                if (player != null) {
                                    player.sendMessage(Text.literal("The world will now be backupped."), false);
                                }

                                DiscordBotMain.instance().backupWorld();

                                return 1;
                            });

            dispatcher.register(backupWorldCmd);
        });
    }
}