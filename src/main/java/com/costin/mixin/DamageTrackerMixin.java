package com.costin.mixin;

import com.costin.ServerMain;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DamageTracker.class)
public abstract class DamageTrackerMixin {

    @Shadow @Final private List<DamageRecord> recentDamage;

    @Shadow @Final private LivingEntity entity;

    @Shadow public abstract Text getDeathMessage();

    @Inject(method = "onDamage", at = @At("TAIL"))
    void damage(DamageSource damageSource, float damage, CallbackInfo ci) {
        if(damageSource.getAttacker() != null) {
            if(!damageSource.getAttacker().isPlayer() && !this.entity.isPlayer()) {
                return;
            }
            ServerMain.getEventManager().onDamage(damageSource.getAttacker().getName().getString() + " damaged " + entity.getName().getString() + " by " + damage);
        }
        else {
            if(!this.entity.isPlayer()) return;
            ServerMain.getEventManager().onDamage(entity.getName().getString() + " got damaged through " + damageSource.getType().msgId() + " by " + damage);
        }

        if(this.entity.getHealth() - damage <= 0) {
            ServerMain.getEventManager().onDeath(this.getDeathMessage().getString());
        }
    }
}
