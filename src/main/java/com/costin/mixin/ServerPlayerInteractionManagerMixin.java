package com.costin.mixin;

import com.costin.main.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.Optional;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Unique
    private static Optional<Entity> getClosestEntity(World world, BlockPos pos, double radius) {
        Vec3d searchPosition = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

        // Search for all entities within the radius
        return world.getEntitiesByClass(Entity.class, new net.minecraft.util.math.Box(pos).expand(radius), entity -> true)
                .stream()
                .filter(entity -> entity instanceof FallingBlockEntity || entity instanceof TntEntity || entity instanceof VillagerEntity)
                .min(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(searchPosition)));
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    void interactItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().isAccepted()) {
            Utils.getEventManager().onItemInteraction(world.getDimensionEntry().getIdAsString().split(":")[1], String.format("%s used %s", player.getName().getString(), stack.getName().getString()));
        }
    }

    // this will work also when blocks are placed, so BlockItem mixin is useless.
    // However, you cannot tell apart if it's an interaction like flint and steel on tnt (or any other block), or an actual block that was being placed. Keep in mind spawn eggs and bone meals too.
    @Inject(method = "interactBlock", at = @At("RETURN"))
    void interactBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().isAccepted()) {

            String blockName, entName = "";
            blockName = world.getBlockState(hitResult.getBlockPos()).getBlock().getName().getString();
            if (blockName.equals("Air")) blockName = "";
            Optional<Entity> ent = getClosestEntity(world, hitResult.getBlockPos(), 1);
            if (ent.isPresent()) {
                entName = ent.get().getName().getString();
            }
            if (entName == null) entName = "";

            String msg;
            if (!blockName.isEmpty() && !entName.isEmpty()) {
                msg = "%s used %s on either entity %s or block %s (?)";
            } else if (!blockName.isEmpty()) {
                msg = "%s used %s on block %s%s";
            } else if (!entName.isEmpty()) {
                msg = "%s used %s on entity %s%s";
            } else {
                msg = "INTERNAL ERROR: closest block AND entity are null! Most likely, an explosion (such as bed in nether) happened.";
            }
            Utils.getEventManager().onItemInteraction(world.getDimensionEntry().getIdAsString().split(":")[1], msg.formatted(player.getName().getString(), stack.getName().getString(), entName, blockName));
        }
    }
}
