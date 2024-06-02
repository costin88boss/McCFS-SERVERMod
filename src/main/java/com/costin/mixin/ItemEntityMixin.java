package com.costin.mixin;

import com.costin.main.Utils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    private int pickupDelay;
    @Shadow
    private @Nullable UUID owner;

    @Shadow
    public abstract ItemStack getStack();

    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    void itemPickup(PlayerEntity player, CallbackInfo ci) {
        ItemStack itemStack = this.getStack();
        if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUuid()))) {
            Utils.getEventManager().onItemPickup(player.getEntityWorld().getDimensionEntry().getIdAsString().split(":")[1], itemStack.getName(), itemStack.getCount(), player.getName());
        }
    }

}
