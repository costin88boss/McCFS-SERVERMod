package com.costin;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.intent.IntentSet;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DiscordBotMain extends Thread {

    private static final int charLimit = 2000;
    private static DiscordBotMain singleton;
    private final String botToken;
    HashMap<String, String> botProperties;
    LinkedList<String> bf_item_dp, bf_item_interaction, bf_block_pr, bf_damage, bf_kill, bf_servercmds, bf_chat, bf_backuplogs;

    public DiscordBotMain(String botToken) {
        super("DiscordBot");
        this.botToken = botToken;
        singleton = this;
    }

    public static DiscordBotMain instance() {
        if (singleton == null) {
            throw new RuntimeException("Bot singleton called no init ajasdasjiudsam afasfaskaksd");
        }
        return singleton;
    }

    public void backupWorld() {
        try {
            sendMcLog(McLogType.BACKUP_LOG, "Backupping world...");
            File serverDir = new File(new File("").toPath().toAbsolutePath().toString());
            File worldDir = new File(serverDir + "\\world");

            File backupFile = new File(serverDir.getAbsolutePath() + "\\AUTO_BACKUP\\");
            backupFile.mkdirs();

            String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS_ddMMyyyy"));

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
                    zipOutputStream.putNextEntry(new ZipEntry(next.toString()));
                    byte[] bytes = Files.readAllBytes(file);
                    zipOutputStream.write(bytes, 0, bytes.length);
                    zipOutputStream.closeEntry();

                    return FileVisitResult.CONTINUE;
                }
            });
            zipOutputStream.flush();
            zipOutputStream.close();
            String path = backupFile.getAbsolutePath();
            Files.move(Path.of(path), Path.of(path.substring(0, path.length() - 3) + "zip"));
            sendMcLog(McLogType.BACKUP_LOG, "Backupped world (" + formattedDateTime + ")");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void sendMcLog(McLogType type, String whatToSend) {
        switch (type) {
            case DEATH -> bf_kill.add(whatToSend);
            case DAMAGE -> bf_damage.add(whatToSend);
            case ITEM_DROP, ITEM_PICKUP -> bf_item_dp.add(whatToSend);
            case BLOCK_PLACE, BLOCK_BREAK -> bf_block_pr.add(whatToSend);
            case ITEM_INTERACTION -> bf_item_interaction.add(whatToSend);
            case CHATMESSAGE -> bf_chat.add(whatToSend);
            case COMMANDMESSAGE__ -> bf_servercmds.add(whatToSend);
            case BACKUP_LOG -> {
                ServerMain.LOGGER.info(whatToSend);
                bf_backuplogs.add(whatToSend);
            }
        }
    }

    @Override
    public void run() {
        GatewayDiscordClient client = DiscordClient.create(botToken).gateway().setEnabledIntents(IntentSet.all()).login().block();

        bf_item_dp = new LinkedList<>();
        bf_item_interaction = new LinkedList<>();
        bf_block_pr = new LinkedList<>();
        bf_damage = new LinkedList<>();
        bf_kill = new LinkedList<>();
        bf_servercmds = new LinkedList<>();
        bf_chat = new LinkedList<>();
        bf_backuplogs = new LinkedList<>();

        BufferedReader properties = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("bot.properties"))));
        botProperties = new HashMap<>();
        try {
            while (properties.ready()) {
                String ln = properties.readLine();
                if (ln.isEmpty()) continue;
                String[] line = ln.split("=");
                String name = line[0];
                String value = line[1];
                botProperties.put(name, value);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        String thisDir = new File("").toPath().toAbsolutePath().toString();
        if(thisDir.contains(botProperties.get("dev_loc"))) {
            ServerMain.DEV_MODE = true;
        }

        Objects.requireNonNull(client).onDisconnect().subscribe();

        long lastTime = System.currentTimeMillis();
        long lastBackupTime = System.currentTimeMillis();
        long curTime;
        while (ServerMain.isServerOn()) {
            curTime = System.currentTimeMillis();
            if (curTime >= lastBackupTime) {
                backupWorld(); // will backup on server start as well.
                lastBackupTime = System.currentTimeMillis() + 60 * 60 * 1000; // every hour backup
            }
            if (curTime >= lastTime) {
                lastTime = System.currentTimeMillis() + 3000;
            } else continue;

            StringBuilder sb = new StringBuilder();
            String str_item_dp, str_item_interaction, str_block_pr, str_damage, str_kill, str_servercmds, str_chat, str_backuplogs;
            sb.setLength(0);
            while (true) {
                if (bf_item_dp.isEmpty()) break;
                String s = bf_item_dp.getFirst();
                if (sb.length() + s.length() + 12 > DiscordBotMain.charLimit) break;
                sb.append('`');
                if (ServerMain.DEV_MODE) {
                    sb.append("[DEV SRV]");
                }
                sb.append(s).append('`').append("\n");
                bf_item_dp.removeFirst();
            }
            str_item_dp = sb.toString();
            sb.setLength(0);
            while (true) {
                if (bf_item_interaction.isEmpty()) break;
                String s = bf_item_interaction.getFirst();
                if (sb.length() + s.length() + 12 > DiscordBotMain.charLimit) break;
                sb.append('`');
                if (ServerMain.DEV_MODE) {
                    sb.append("[DEV SRV]");
                }
                sb.append(s).append('`').append("\n");
                bf_item_interaction.removeFirst();
            }
            str_item_interaction = sb.toString();
            sb.setLength(0);
            while (true) {
                if (bf_block_pr.isEmpty()) break;
                String s = bf_block_pr.getFirst();
                if (sb.length() + s.length() + 12 > DiscordBotMain.charLimit) break;
                sb.append('`');
                if (ServerMain.DEV_MODE) {
                    sb.append("[DEV SRV]");
                }
                sb.append(s).append('`').append("\n");
                bf_block_pr.removeFirst();
            }
            str_block_pr = sb.toString();
            sb.setLength(0);
            while (true) {
                if (bf_damage.isEmpty()) break;
                String s = bf_damage.getFirst();
                if (sb.length() + s.length() + 12 > DiscordBotMain.charLimit) break;
                sb.append('`');
                if (ServerMain.DEV_MODE) {
                    sb.append("[DEV SRV]");
                }
                sb.append(s).append('`').append("\n");
                bf_damage.removeFirst();
            }
            str_damage = sb.toString();
            sb.setLength(0);
            while (true) {
                if (bf_kill.isEmpty()) break;
                String s = bf_kill.getFirst();
                if (sb.length() + s.length() + 12 > DiscordBotMain.charLimit) break;
                sb.append('`');
                if (ServerMain.DEV_MODE) {
                    sb.append("[DEV SRV]");
                }
                sb.append(s).append('`').append("\n");
                bf_kill.removeFirst();
            }
            str_kill = sb.toString();
            sb.setLength(0);
            while (true) {
                if (bf_servercmds.isEmpty()) break;
                String s = bf_servercmds.getFirst();
                if (sb.length() + s.length() + 12 > DiscordBotMain.charLimit) break;
                sb.append('`');
                if (ServerMain.DEV_MODE) {
                    sb.append("[DEV SRV]");
                }
                sb.append(s).append('`').append("\n");
                bf_servercmds.removeFirst();
            }
            str_servercmds = sb.toString();
            sb.setLength(0);
            while (true) {
                if (bf_chat.isEmpty()) break;
                String s = bf_chat.getFirst();
                if (sb.length() + s.length() + 12 > DiscordBotMain.charLimit) break;
                sb.append('`');
                if (ServerMain.DEV_MODE) {
                    sb.append("[DEV SRV]");
                }
                sb.append(s).append('`').append("\n");
                bf_chat.removeFirst();
            }
            str_chat = sb.toString();
            sb.setLength(0);
            while (true) {
                if (bf_backuplogs.isEmpty()) break;
                String s = bf_backuplogs.getFirst();
                if (sb.length() + s.length() + 12 > DiscordBotMain.charLimit) break;
                sb.append('`');
                if (ServerMain.DEV_MODE) {
                    sb.append("[DEV SRV]");
                }
                sb.append(s).append('`').append("\n");
                bf_backuplogs.removeFirst();
            }
            str_backuplogs = sb.toString();

            if (!str_item_dp.isEmpty()) {
                client.getGuildById(Snowflake.of(Long.parseLong(botProperties.get("guildID")))).subscribe(guild -> {
                    guild.getChannelById(Snowflake.of(Long.parseLong(botProperties.get("id_itemDP")))).subscribe(guildChannel -> {
                        guildChannel.getRestChannel().createMessage(str_item_dp).subscribe();
                    });
                });
            }
            if (!str_item_interaction.isEmpty()) {
                client.getGuildById(Snowflake.of(Long.parseLong(botProperties.get("guildID")))).subscribe(guild -> {
                    guild.getChannelById(Snowflake.of(Long.parseLong(botProperties.get("id_iteminteraction")))).subscribe(guildChannel -> {
                        guildChannel.getRestChannel().createMessage(str_item_interaction).subscribe();
                    });
                });
            }
            if (!str_block_pr.isEmpty()) {
                client.getGuildById(Snowflake.of(Long.parseLong(botProperties.get("guildID")))).subscribe(guild -> {
                    guild.getChannelById(Snowflake.of(Long.parseLong(botProperties.get("id_blockPR")))).subscribe(guildChannel -> {
                        guildChannel.getRestChannel().createMessage(str_block_pr).subscribe();
                    });
                });
            }
            if (!str_damage.isEmpty()) {
                client.getGuildById(Snowflake.of(Long.parseLong(botProperties.get("guildID")))).subscribe(guild -> {
                    guild.getChannelById(Snowflake.of(Long.parseLong(botProperties.get("id_damage")))).subscribe(guildChannel -> {
                        guildChannel.getRestChannel().createMessage(str_damage).subscribe();
                    });
                });
            }
            if (!str_kill.isEmpty()) {
                client.getGuildById(Snowflake.of(Long.parseLong(botProperties.get("guildID")))).subscribe(guild -> {
                    guild.getChannelById(Snowflake.of(Long.parseLong(botProperties.get("id_kill")))).subscribe(guildChannel -> {
                        guildChannel.getRestChannel().createMessage(str_kill).subscribe();
                    });
                });
            }
            if (!str_servercmds.isEmpty()) {
                client.getGuildById(Snowflake.of(Long.parseLong(botProperties.get("guildID")))).subscribe(guild -> {
                    guild.getChannelById(Snowflake.of(Long.parseLong(botProperties.get("id_servercmds")))).subscribe(guildChannel -> {
                        guildChannel.getRestChannel().createMessage(str_servercmds).subscribe();
                    });
                });
            }
            if (!str_chat.isEmpty()) {
                client.getGuildById(Snowflake.of(Long.parseLong(botProperties.get("guildID")))).subscribe(guild -> {
                    guild.getChannelById(Snowflake.of(Long.parseLong(botProperties.get("id_chat")))).subscribe(guildChannel -> {
                        guildChannel.getRestChannel().createMessage(str_chat).subscribe();
                    });
                });
            }
            if (!str_backuplogs.isEmpty()) {
                client.getGuildById(Snowflake.of(Long.parseLong(botProperties.get("guildID")))).subscribe(guild -> {
                    guild.getChannelById(Snowflake.of(Long.parseLong(botProperties.get("id_backuplogs")))).subscribe(guildChannel -> {
                        guildChannel.getRestChannel().createMessage(str_backuplogs).subscribe();
                    });
                });
            }
        }
    }
}
