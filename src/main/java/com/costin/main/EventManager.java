package com.costin.main;

import com.costin.DiscordBot;
import com.costin.McLogType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class EventManager {

    public EventManager() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> onBlockBreak(world.getDimensionEntry().getIdAsString().split(":")[1], state.getBlock().getName(), player.getName(), pos));
    }

    public void onDeath(String dim, String msg) {
        DiscordBot.instance().sendMcLog(McLogType.DEATH, "[" + dim + "]" + msg);
    }

    public void onDamage(String dim, String msg) {
        DiscordBot.instance().sendMcLog(McLogType.DAMAGE, "[" + dim + "]" + msg);
    }

    public void onItemDrop(String dim, Text itemName, int amount, Text who) {
        if (itemName.getString().equalsIgnoreCase("air")) return;

        String msg = String.format("%s dropped %s %s's", who.getString(), amount, itemName.getString());
        DiscordBot.instance().sendMcLog(McLogType.ITEM_DROP, "[" + dim + "]" + msg);
    }

    public void onItemPickup(String dim, Text itemName, int amount, Text who) {
        if (itemName.getString().equalsIgnoreCase("air")) return;

        String msg = String.format("%s picked up %s %s's", who.getString(), amount, itemName.getString());
        DiscordBot.instance().sendMcLog(McLogType.ITEM_PICKUP, "[" + dim + "]" + msg);
    }

    public void onBlockPlace(String dim, Text blockName, Text who, BlockPos pos) {
        if (blockName.getString().equalsIgnoreCase("air")) return;
        Utils.blocksPlacedSinceLastBackup++;

        String msg = String.format("%s placed %s at x%s y%s z%s", who.getString(), blockName.getString(), pos.getX(), pos.getY(), pos.getZ());
        DiscordBot.instance().sendMcLog(McLogType.BLOCK_PLACE, "[" + dim + "]" + msg);
    }

    public void onBlockBreak(String dim, Text blockName, Text who, BlockPos pos) {
        if (blockName.getString().equalsIgnoreCase("air")) return;
        Utils.blocksDestroyedSinceLastBackup++;

        String msg = String.format("%s broke %s at x%s y%s z%s", who.getString(), blockName.getString(), pos.getX(), pos.getY(), pos.getZ());
        DiscordBot.instance().sendMcLog(McLogType.BLOCK_BREAK, "[" + dim + "]" + msg);
    }

    public void onItemInteraction(String dim, String msg) {
        DiscordBot.instance().sendMcLog(McLogType.ITEM_INTERACTION, "[" + dim + "]" + msg);
    }

    public void onExplosion(String dim, String msg) {
        ServerMain.griefBackup(McLogType.EXPLOSION, "[" + dim + "]" + msg);
        DiscordBot.instance().sendMcLog(McLogType.EXPLOSION, "[" + dim + "]" + msg);
    }

    public void onChatMessage(String prefix, String msg) {
        DiscordBot.instance().sendMcLog(McLogType.CHATMESSAGE, prefix + msg);
    }

    public void onCommandMessage(String msg, String command, int permLvl) {
        if (permLvl >= 2) {
            if (Arrays.stream(Utils.susCommands).anyMatch(s -> s.equalsIgnoreCase(command))) {
                ServerMain.griefBackup(McLogType.COMMANDMESSAGE__, String.format("%s executed %s", msg, command));
            }
        }
        DiscordBot.instance().sendMcLog(McLogType.COMMANDMESSAGE__, String.format("%s executed %s", msg, command));
    }
}
