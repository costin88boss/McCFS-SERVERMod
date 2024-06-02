package com.costin.mixin;

import com.costin.main.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow
    public abstract RegistryEntry<DimensionType> getDimensionEntry();

    @Inject(method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;ZLnet/minecraft/particle/ParticleEffect;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/registry/entry/RegistryEntry;)Lnet/minecraft/world/explosion/Explosion;", at = @At("TAIL"))
    void explosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, boolean particles, ParticleEffect particle, ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent, CallbackInfoReturnable<Explosion> cir) {
        String attacker = "(unknown)", _entity = "unknown", dmgSource = "unknown";

        if (damageSource != null) {
            if (damageSource.getAttacker() != null) {
                attacker = damageSource.getAttacker().getName().getString();
            }
            dmgSource = damageSource.getName();
        }
        if (entity != null) {
            _entity = entity.getName().getString();
        }

        Utils.getEventManager().onExplosion(this.getDimensionEntry().getIdAsString().split(":")[1], "%s explosion at x%s y%s z%s caused by %s, damage type %s".formatted(_entity, x, y, z, attacker, dmgSource));

    }
}
