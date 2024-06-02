package com.costin.mixin;

import com.costin.main.Utils;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {

    @Unique
    private int getPermLevel(ServerCommandSource source) {
        if (source.hasPermissionLevel(4)) return 4;
        if (source.hasPermissionLevel(3)) return 3;
        if (source.hasPermissionLevel(2)) return 2;
        if (source.hasPermissionLevel(1)) return 1;
        return 0;
    }

    @Inject(method = "execute", at = @At("HEAD"))
    void test(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci) {

        Utils.getEventManager().onCommandMessage(parseResults.getContext().getSource().getName(), command, getPermLevel(parseResults.getContext().getSource()));
    }
}
