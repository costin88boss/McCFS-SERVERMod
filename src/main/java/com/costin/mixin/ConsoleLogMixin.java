package com.costin.mixin;

import com.costin.main.Utils;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MinecraftServer.class)
public class ConsoleLogMixin {
    @Inject(method = "sendMessage", at = @At("HEAD"))
    void sendMessage(Text message, CallbackInfo ci) {
        //ServerMain.getEventManager().onCommandMessage(message);
    }

    @Inject(method = "logChatMessage", at = @At("HEAD"))
    void logChatMessage(Text message, MessageType.Parameters params, String prefix, CallbackInfo ci) {
        String string = params.applyChatDecoration(message).getString();
        Utils.getEventManager().onChatMessage(Objects.requireNonNullElse(prefix, ""), string);
    }
}
