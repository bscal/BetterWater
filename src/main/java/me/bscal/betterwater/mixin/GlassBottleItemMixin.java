package me.bscal.betterwater.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

// TODO broken
@Mixin(GlassBottleItem.class)
public abstract class GlassBottleItemMixin extends Item
{
    public GlassBottleItemMixin(Settings settings)
    {
        super(settings);
    }

    @Inject(method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V",
                    ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, List list, ItemStack itemStack, HitResult hitResult, BlockPos blockPos)
    {
        GlassBottleItem item = (GlassBottleItem) (Object) this;
        var fluidState = world.getFluidState(blockPos);
        int level = fluidState.getLevel();
        if (level < 1) return;
        int newLevel = (level >= 8) ? 12 : 0;
        var state = fluidState.getBlockState();

        if (state.contains(FluidBlock.LEVEL))
            world.setBlockState(blockPos, state.with(FluidBlock.LEVEL, newLevel), Block.NOTIFY_ALL);
        else
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
    }
}
