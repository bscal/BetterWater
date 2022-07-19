package me.bscal.betterwater.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BucketItem.class)
public interface BucketItemAccessor
{
    @Invoker
    void invokePlayEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos);

}
