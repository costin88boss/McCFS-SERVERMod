package com.costin.main;

import com.costin.DiscordBot;
import com.costin.McLogType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

// Gotta get a more accurate name for this class
public class Utils {
    public static final Logger LOGGER = LoggerFactory.getLogger("mccfs_servermod");
    public static final String BACKUP_FOLDER = "/AUTO_BACKUP/";
    public static final int BACKUP_OLDEST_MAX_TIME_HOURS = 48;
    public static final int BACKUP_GRIEF_COOLDOWN_TIME = 30; // minutes
    public static final String[] susCommands = new String[]{
            /* worldedit */ "/", ".s", "ascend", "biomeinfo", "biomelist", "brush", "butcher", "/calculate", "ceil", "/center", "/chunk", "chunkinfo", "clearclipboard", "clearhistory", "/cone", "/contract", "/copy", "/count", "cs", "/curve", "/cut", "cycler", "/cyl", "/deform", "delchunks", "deltree", "descend", "/distr", "/drain", "/drawsel", "/expand", "extinguish", "/faces", "farwand", "/fast", "/feature", "/fill", "/fillr", "fixlava", "fixwater", "/flip", "floodfill", "/flora", "/forest", "forestgen", "/generate", "/generatebiome", "gmask", "green", "/hcyl", "/help", "/hollow", "/hpos1", "/hpos2", "/hpyramid", "/hsphere", "info", "/inset", "jumpto", "/limit", "/line", "listchunks", "lrbuild", "mask", "material", "/move", "/naturalize", "navwand", "none", "/outset", "/overlay", "/paste", "/perf", "placement", "/pos", "/pos1", "/pos2", "pumpkins", "/pyramid", "range", "redo", "/regen", "remove", "removeabove", "removebelow", "removenear", "/reorder", "repl", "/replace", "replacenear", "restore", "/rotate", "schematic", "searchitem", "/sel", "selwand", "/set", "/setbiome", "/shift", "size", "/size", "/smooth", "snapshot", "snow", "/snowsmooth", "/sphere", "/stack", "/structure", "superpickaxe", "thaw", "thru", "/timeout", "toggleeditwand", "toggleplace", "tool", "tracemask", "tree", "/trim", "undo", "unstuck", "up", "/update", "/walls", "/wand", "/watchdog", "/world", "worldwide",
            "fill",
    };
    private static final String logPrefix;
    private static final boolean devMode;
    private static final EventManager eventManager;
    protected static MinecraftServer server;
    protected static int blocksPlacedSinceLastBackup;
    protected static int blocksDestroyedSinceLastBackup;
    private static boolean serverOn;

    static {
        devMode = runningInIDE();
        if (devMode) {
            logPrefix = "[DEV SRV]";
        } else logPrefix = "";
        eventManager = new EventManager();
        serverOn = true;
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (blocksPlacedSinceLastBackup - blocksDestroyedSinceLastBackup / 2 > 10) {
                DiscordBot.instance().sendMcLog(McLogType.BACKUP_LOG, "placed - destroyed / 2 > 10, a backup will happen.");
                ServerMain.backupWorld();
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> Utils.serverOn = false);
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static String getLogPrefix() {
        return logPrefix;
    }

    public static boolean isDevMode() {
        return devMode;
    }

    public static boolean runningInIDE() {
        File thisDir = new File("").getAbsoluteFile().getParentFile();
        File testFile = new File(thisDir + "/gradle.properties");
        return testFile.exists();
    }

    public static EventManager getEventManager() {
        return eventManager;
    }

    public static boolean isServerOn() {
        return serverOn;
    }
}
