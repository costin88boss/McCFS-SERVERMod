package com.costin;

import com.costin.main.ServerMain;
import com.costin.main.Utils;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.intent.IntentSet;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.*;

public class DiscordBot extends Thread {

    private static final int charLimit = 2000;
    private static DiscordBot singleton;
    private final String botToken;
    private HashMap<String, String> botProperties;
    private LinkedList<String> bf_item_dp, bf_item_interaction, bf_block_pr, bf_damage, bf_kill, bf_servercmds, bf_chat, bf_backuplogs, bf_explosions;
    private GatewayDiscordClient client;
    private StringBuilder bf_sb;

    private DiscordBot() {
        super("DiscordBot");
        //this.botToken = System.getenv("McCFS");
        singleton = this;

        BufferedReader properties = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("bot.properties"))));
        botProperties = new HashMap<>();
        try {
            while (properties.ready()) {
                String ln = properties.readLine();
                if (ln.isEmpty() || ln.startsWith("#")) continue;
                String[] line = ln.split("=");
                String name = line[0].strip().trim();
                String value = line[1].strip().trim();
                botProperties.put(name, value);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        try {
            String var1 = botProperties.get("shh");
            Cipher var2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec var3 = new SecretKeySpec("kMJ9JrLaV9vFBr0b".getBytes("UTF-8"), "AES");
            var2.init(Cipher.DECRYPT_MODE, var3);
            byte[] var4 = var2.doFinal(Base64.getDecoder().decode(var1));
            botToken = new String(var4);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static DiscordBot instance() {
        if (singleton == null) {
            new DiscordBot();
        }
        return singleton;
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
            case BACKUP_LOG -> bf_backuplogs.add(whatToSend);
            case EXPLOSION -> bf_explosions.add(whatToSend);
        }
    }

    public void emergencyPing(McLogType type) {
        // for now only myself (costin)
        sendMsgToChannel("<@%s> EMERGENCY PING (logs that triggered this may take 3 seconds to be sent in this channel!)".formatted(botProperties.get("user_costin")), botProperties.get(type.idProperty));
    }

    private String constructMsgLog(LinkedList<String> buffer) {
        bf_sb.setLength(0);
        while (true) {
            if (buffer.isEmpty()) break;
            String s = buffer.getFirst();
            if (bf_sb.length() + s.length() + 12 > DiscordBot.charLimit) break;
            bf_sb.append('`');
            if (Utils.isDevMode()) {
                bf_sb.append(Utils.getLogPrefix());
            }
            bf_sb.append(s).append('`').append("\n");
            buffer.removeFirst();
        }
        return bf_sb.toString();
    }

    private void sendMsgToChannel(String msg, String channelId) {
        sendMsgToChannel(msg, Long.parseLong(channelId));
    }

    private void sendMsgToChannel(String msg, long channelId) {
        if (!msg.isEmpty()) {
            client.getGuildById(Snowflake.of(Long.parseLong(botProperties.get("guildID")))).subscribe(guild -> guild.getChannelById(Snowflake.of(channelId)).subscribe(guildChannel -> guildChannel.getRestChannel().createMessage(msg).subscribe()));
        }
    }

    private void loop() { // I regret that it's in this class.............
        bf_sb = new StringBuilder();

        long lastTime = System.currentTimeMillis();
        long PLAYERBackupElapsed = 0;
        long playerBackupTime_ = lastTime + 60 * 1000;
        long hourlyTime = lastTime;
        long curTime;
        while (Utils.isServerOn()) {
            curTime = System.currentTimeMillis();
            if (curTime >= playerBackupTime_) {
                // thought that .isEmpty() could perhaps be better than .size()
                if (!Utils.getServer().getPlayerManager().getPlayerList().isEmpty()) {
                    PLAYERBackupElapsed += 60;
                    if (PLAYERBackupElapsed > 60 * 30) {
                        PLAYERBackupElapsed = 0;
                        ServerMain.backupWorld();
                    }
                }
                playerBackupTime_ = System.currentTimeMillis() + 60 * 1000;
            }
            if (curTime >= hourlyTime) {
                ServerMain.checkAndCleanOldBackups();
                hourlyTime = System.currentTimeMillis() + 60 * 60 * 1000;

            }
            if (curTime >= lastTime) {
                lastTime = System.currentTimeMillis() + 3000;
            } else continue;

            String str_item_dp, str_item_interaction, str_block_pr, str_damage, str_kill, str_servercmds, str_chat, str_backuplogs, str_explosions;
            str_item_dp = constructMsgLog(bf_item_dp);
            str_item_interaction = constructMsgLog(bf_item_interaction);
            str_block_pr = constructMsgLog(bf_block_pr);
            str_damage = constructMsgLog(bf_damage);
            str_kill = constructMsgLog(bf_kill);
            str_servercmds = constructMsgLog(bf_servercmds);
            str_chat = constructMsgLog(bf_chat);
            str_backuplogs = constructMsgLog(bf_backuplogs);
            str_explosions = constructMsgLog(bf_explosions);

            sendMsgToChannel(str_item_dp, botProperties.get(McLogType.ITEM_DROP.idProperty));
            sendMsgToChannel(str_item_interaction, botProperties.get(McLogType.ITEM_INTERACTION.idProperty));
            sendMsgToChannel(str_block_pr, botProperties.get(McLogType.BLOCK_BREAK.idProperty));
            sendMsgToChannel(str_damage, botProperties.get(McLogType.DAMAGE.idProperty));
            sendMsgToChannel(str_kill, botProperties.get(McLogType.DEATH.idProperty));
            sendMsgToChannel(str_servercmds, botProperties.get(McLogType.COMMANDMESSAGE__.idProperty));
            sendMsgToChannel(str_chat, botProperties.get(McLogType.CHATMESSAGE.idProperty));
            sendMsgToChannel(str_backuplogs, botProperties.get(McLogType.BACKUP_LOG.idProperty));
            sendMsgToChannel(str_explosions, botProperties.get(McLogType.EXPLOSION.idProperty));
        }
    }

    @Override
    public void run() {
        client = DiscordClient.create(botToken).gateway().setEnabledIntents(IntentSet.all()).login().block();

        bf_item_dp = new LinkedList<>();
        bf_item_interaction = new LinkedList<>();
        bf_block_pr = new LinkedList<>();
        bf_damage = new LinkedList<>();
        bf_kill = new LinkedList<>();
        bf_servercmds = new LinkedList<>();
        bf_chat = new LinkedList<>();
        bf_backuplogs = new LinkedList<>();
        bf_explosions = new LinkedList<>();

        Objects.requireNonNull(client).onDisconnect().subscribe();

        loop();
    }
}
