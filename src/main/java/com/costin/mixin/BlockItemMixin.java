package com.costin.mixin;

import com.costin.main.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("TAIL"))
    void place(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        if (context.getPlayer() != null) {
            Utils.getEventManager().onBlockPlace(context.getPlayer().getEntityWorld().getDimensionEntry().getIdAsString().split(":")[1], state.getBlock().getName(), context.getPlayer().getName(), context.getBlockPos());
        }
    }
}
