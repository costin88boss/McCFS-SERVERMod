package com.costin;

public enum McLogType {
    DEATH("id_kill"),
    DAMAGE("id_damage"),
    ITEM_DROP("id_item_dp"),
    ITEM_PICKUP("id_item_dp"),
    BLOCK_PLACE("id_block_pr"),
    BLOCK_BREAK("id_block_pr"),
    ITEM_INTERACTION("id_item_interaction"),
    CHATMESSAGE("id_chat"),
    COMMANDMESSAGE__("id_servercmds"),
    BACKUP_LOG("id_backuplogs"),
    EXPLOSION("id_explosions");

    public final String idProperty;

    McLogType(String botProperty) {
        idProperty = botProperty;
    }
}
