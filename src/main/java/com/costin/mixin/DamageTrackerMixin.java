package com.costin.mixin;

import com.costin.main.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTracker.class)
public abstract class DamageTrackerMixin {
    @Shadow
    @Final
    private LivingEntity entity;

    @Shadow
    public abstract Text getDeathMessage();

    @Inject(method = "onDamage", at = @At("TAIL"))
    void damage(DamageSource damageSource, float damage, CallbackInfo ci) {
        if (damageSource.getAttacker() != null) {
            if (!damageSource.getAttacker().isPlayer() && !this.entity.isPlayer()) {
                return;
            }
            Utils.getEventManager().onDamage(damageSource.getAttacker().getEntityWorld().getDimensionEntry().getIdAsString().split(":")[1], damageSource.getAttacker().getName().getString() + " damaged " + entity.getName().getString() + " by " + damage);
        } else {
            if (!this.entity.isPlayer()) return;
            Utils.getEventManager().onDamage(entity.getEntityWorld().getDimensionEntry().getIdAsString().split(":")[1], entity.getName().getString() + " got damaged through " + damageSource.getType().msgId() + " by " + damage);
        }

        if (this.entity.getHealth() - damage <= 0) {
            Utils.getEventManager().onDeath(this.entity.getEntityWorld().getDimensionEntry().getIdAsString().split(":")[1], this.getDeathMessage().getString());
        }
    }
}
