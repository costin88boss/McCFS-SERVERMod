package com.costin.mixin;

import com.costin.ServerMain;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "execute", at = @At("HEAD"))
    void test(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci) {
        ServerMain.getEventManager().onCommandMessage(String.format("%s executed %s", parseResults.getContext().getSource().getName(), command));
    }
}
