package com.costin.main;

import com.costin.DiscordBot;
import com.costin.McLogType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerMain implements ModInitializer {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("HHmmssSSS_ddMMyyyy");
    private static long explosionBackupCooldown = 0;
    private static long susCmdBackupCooldown = 0;

    public static void backupWorld() {
        try {
            DiscordBot.instance().sendMcLog(McLogType.BACKUP_LOG, "Backupping world...");
            File serverDir = new File("").getAbsoluteFile();
            File worldDir = new File(serverDir + "\\world");

            File backupFile = new File(serverDir.getAbsolutePath() + Utils.BACKUP_FOLDER);
            backupFile.mkdirs();

            // I want to kill myself with these zone stuff
            String formattedDateTime = formatter.format(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));

            backupFile = new File(backupFile.getAbsolutePath() + "\\" + formattedDateTime + ".bin");
            if (!backupFile.exists()) backupFile.createNewFile();

            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(backupFile));

            Files.walkFileTree(worldDir.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path next = worldDir.toPath().relativize(file);
                    if (next.toFile().getName().equals("session.lock")) {
                        return FileVisitResult.CONTINUE;
                    }
                    byte[] bytes = Files.readAllBytes(file);
                    zipOutputStream.putNextEntry(new ZipEntry(next.toString()));
                    zipOutputStream.write(bytes, 0, bytes.length);
                    zipOutputStream.closeEntry();

                    return FileVisitResult.CONTINUE;
                }
            });
            zipOutputStream.flush();
            zipOutputStream.close();
            String path = backupFile.getAbsolutePath();
            Files.move(Path.of(path), Path.of(path.substring(0, path.length() - 3) + "zip"));
            Utils.blocksPlacedSinceLastBackup = 0;
            Utils.blocksDestroyedSinceLastBackup = 0;
            DiscordBot.instance().sendMcLog(McLogType.BACKUP_LOG, "Backupped world (" + formattedDateTime + ")");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void checkAndCleanOldBackups() {
        try {
            DiscordBot.instance().sendMcLog(McLogType.BACKUP_LOG, "Checking for backups older than %s hours..".formatted(Utils.BACKUP_OLDEST_MAX_TIME_HOURS));
            File serverDir = new File("").getAbsoluteFile();
            File backupDir = new File(serverDir + Utils.BACKUP_FOLDER);
            boolean deletedSmth = false;

            File[] backups = backupDir.listFiles();
            for (File backup : backups) {
                String name = backup.getName();
                Date date = formatter.parse(name);

                long diffS = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)).getTime() - date.getTime();
                int diffH = Math.round(diffS / 1000f / 60f / 60f);
                if (diffH > Utils.BACKUP_OLDEST_MAX_TIME_HOURS) {
                    DiscordBot.instance().sendMcLog(McLogType.BACKUP_LOG, "Deleting backup: %s".formatted(name));
                    backup.delete();
                    deletedSmth = true;
                }
            }
            if (!deletedSmth) {
                DiscordBot.instance().sendMcLog(McLogType.BACKUP_LOG, "No outdated backup has been found.");
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void griefBackup(McLogType mcLogType, String msg) {
        // at the beginning, both cooldowns are 0.

        if (explosionBackupCooldown <= System.currentTimeMillis() && mcLogType == McLogType.EXPLOSION) {
            DiscordBot.instance().emergencyPing(mcLogType);
            DiscordBot.instance().sendMcLog(McLogType.BACKUP_LOG, "Potential grief of type %s detected, backupping (grief backup cooldown: %s). Log: \"%s\"".formatted(mcLogType.name(), Utils.BACKUP_GRIEF_COOLDOWN_TIME, msg));
            backupWorld();
            explosionBackupCooldown = System.currentTimeMillis() + Utils.BACKUP_GRIEF_COOLDOWN_TIME * 60 * 1000;
        }

        if (susCmdBackupCooldown <= System.currentTimeMillis() && mcLogType == McLogType.COMMANDMESSAGE__) {
            DiscordBot.instance().sendMcLog(McLogType.BACKUP_LOG, "Potentially dangerous command was ran, backupping (cmd backup cooldown: %s). Log: \"%s\"".formatted(Utils.BACKUP_GRIEF_COOLDOWN_TIME, msg));
            backupWorld();
            susCmdBackupCooldown = System.currentTimeMillis() + Utils.BACKUP_GRIEF_COOLDOWN_TIME * 60 * 1000;
        }
    }

    @Override
    public void onInitialize() {
        DiscordBot.instance().start();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> Utils.server = server);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> backupWorldCmd = CommandManager.literal("backupworld")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();

                        ServerPlayerEntity player = source.getPlayer();
                        if (player != null) {
                            player.sendMessage(Text.literal("The world will now be backupped."), false);
                        }

                        backupWorld();

                        return 1;
                    });

            dispatcher.register(backupWorldCmd);
        });
    }
}