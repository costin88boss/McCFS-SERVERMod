package com.costin;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class EventManager {

    public EventManager() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> onBlockBreak(state.getBlock().getName(), player.getName(), pos));

        /*
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stack = player.getStackInHand(hand);
            //onItemInteractOnBlock(stack.getName(), stack.getCount(), player.getName(), hitResult.getEntity().getBlockPos(), hitResult.getBlockPos());
            if(hitResult != null)
                ServerMain.LOGGER.error("Entity: {} Amount: {} User: {} On: {} At: {}", stack.getName().getString(), stack.getCount(), player.getName().getString(), hitResult.getEntity().getName().getString(), hitResult.getEntity().getBlockPos());
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            //onItemInteractOnBlock(stack.getName(), stack.getCount(), player.getName(), hitResult.getEntity().getBlockPos(), hitResult.getBlockPos());
            //ServerMain.LOGGER.error("Item: {} Amount: {} User: {} On: {} At: {}", stack.getName().getString(), stack.getCount(), player.getName().getString(), null, null);
            return TypedActionResult.pass(stack);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack stack = player.getStackInHand(hand);
            ServerMain.LOGGER.error(hitResult.);
            if(stack.isEmpty()) return ActionResult.PASS;
            ServerMain.LOGGER.error("Block: {} Amount: {} User: {} On: {} At: {}", stack.getName().getString(), stack.getCount(), player.getName().getString(), world.getBlockState(hitResult.getBlockPos()).getBlock().getName().getString(), hitResult.getBlockPos());
            //onItemInteractOnBlock(stack.getName(), stack.getCount(), player.getName(), world.getBlockState(hitResult.getBlockPos()).getBlock().getName(), hitResult.getBlockPos());
            return ActionResult.PASS;
        });*/
    }

    //public void onDeath(Text killer, Text victim) {
    public void onDeath(String msg) {
        //String msg = String.format("%s killed %s", killer.getString(), victim.getString());
        DiscordBotMain.instance().sendMcLog(McLogType.DEATH, msg);
    }

    //public void onDamage(Text attacker, Text victim, float amount) {
    public void onDamage(String msg) {
        //String msg = String.format("%s damaged %s by %s", attacker.getString(), victim.getString(), amount);
        DiscordBotMain.instance().sendMcLog(McLogType.DAMAGE, msg);
    }

    public void onItemDrop(Text itemName, int amount, Text who) {
        if (itemName.getString().equalsIgnoreCase("air")) return;

        String msg = String.format("%s dropped %s %s's", who.getString(), amount, itemName.getString());
        DiscordBotMain.instance().sendMcLog(McLogType.ITEM_DROP, msg);
    }

    public void onItemPickup(Text itemName, int amount, Text who) {
        if (itemName.getString().equalsIgnoreCase("air")) return;

        String msg = String.format("%s picked up %s %s's", who.getString(), amount, itemName.getString());
        DiscordBotMain.instance().sendMcLog(McLogType.ITEM_PICKUP, msg);
    }

    public void onBlockPlace(Text blockName, Text who, BlockPos pos) {
        if (blockName.getString().equalsIgnoreCase("air")) return;

        String msg = String.format("%s placed %s at x%s y%s z%s", who.getString(), blockName.getString(), pos.getX(), pos.getY(), pos.getZ());
        DiscordBotMain.instance().sendMcLog(McLogType.BLOCK_PLACE, msg);
    }

    public void onBlockBreak(Text blockName, Text who, BlockPos pos) {
        if (blockName.getString().equalsIgnoreCase("air")) return;

        String msg = String.format("%s broke %s at x%s y%s z%s", who.getString(), blockName.getString(), pos.getX(), pos.getY(), pos.getZ());
        DiscordBotMain.instance().sendMcLog(McLogType.BLOCK_BREAK, msg);
    }

    /*
    public void onItemInteractOnBlock(Text itemName, int amount, Text who, MutableText onWhatBlock, BlockPos blockPos) {
        if (itemName.getString().equalsIgnoreCase("air")) return;
        if (onWhatBlock.getString().equalsIgnoreCase("air")) return;

        String msg = String.format("%s used %s on %s at x%s y%s z%s", who.getString(), itemName.getString(), onWhatBlock.getString(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
        DiscordBotMain.instance().sendMcLog(McLogType.ITEM_INTERACTION, msg);
    }*/

    public void onItemInteraction(String msg) {
        DiscordBotMain.instance().sendMcLog(McLogType.ITEM_INTERACTION, msg);
    }

    public void onChatMessage(String prefix, String msg) {
        DiscordBotMain.instance().sendMcLog(McLogType.CHATMESSAGE, prefix + msg);
    }

    // Wrong name. It will also log other stuff like "x player died" or "x joined/left", but not all console messages.
    public void onCommandMessage(String msg) {
        DiscordBotMain.instance().sendMcLog(McLogType.COMMANDMESSAGE__, msg);
    }
}
