package me.bscal.betterwater.mixin;

import me.bscal.betterwater.BetterWater;
import me.bscal.betterwater.FluidPhysics;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends ItemMixin implements FluidModificationItem
{
    @Shadow
    @Final
    private Fluid fluid;

    @Override
    public void AppendTooltipMixin(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci)
    {
        if (this.fluid == Fluids.WATER && stack.hasNbt())
        {
            var nbt = stack.getNbt();
            if (!nbt.contains(FluidPhysics.LEVEL_KEY))
                return;
            var level = nbt.getByte(FluidPhysics.LEVEL_KEY);
            tooltip.add(Text.of(String.format("Fill Level: %d/8", level)));
        }
    }

    @Inject(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/BucketItem;raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;"),
            cancellable = true)
    public void Use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir)
    {
        if (!BetterWater.Settings().EnableBucketMixin)
            return;

        boolean isFilledWithWater = this.fluid == Fluids.WATER;
        boolean isEmpty = this.fluid == Fluids.EMPTY;
        if (isFilledWithWater || isEmpty)
        {
            var ray = FluidPhysics.Raycast(world, user, RaycastContext.FluidHandling.WATER);
            if (ray.getType() != HitResult.Type.BLOCK)
                return;

            var itemStack = user.getStackInHand(hand);
            BlockPos blockPos = ray.getBlockPos();
            Direction direction = ray.getSide();
            BlockPos dirBlockPos = blockPos.offset(direction);

            if (!world.canPlayerModifyAt(user, blockPos) || !user.canPlaceOn(dirBlockPos, direction, itemStack))
                return;

            var sourceFluidState = world.getFluidState(blockPos);
            if (sourceFluidState.getFluid() == Fluids.WATER) // We dont wants to override default source block impl
                return;
            BlockPos waterBlockPos = sourceFluidState.getFluid() == Fluids.FLOWING_WATER ? blockPos : dirBlockPos;

            var blockState = world.getBlockState(waterBlockPos);
            var fluidState = blockState.getFluidState();
            int waterBlockLevel = FluidPhysics.GetLevel(fluidState);

            var nbt = (itemStack.hasNbt()) ? itemStack.getNbt() : new NbtCompound();
            int currentBucketFill;
            if (nbt.contains(FluidPhysics.LEVEL_KEY))
                currentBucketFill = nbt.getByte(FluidPhysics.LEVEL_KEY);
            else if (isFilledWithWater)
                currentBucketFill = FluidPhysics.MAX_LEVEL;
            else
                currentBucketFill = FluidPhysics.EMPTY;

            // Handle bucket fill
            if (fluidState.getFluid() == Fluids.FLOWING_WATER && currentBucketFill < FluidPhysics.MAX_LEVEL  && !user.isSneaking())
            {
                int bucketSpace = FluidPhysics.MAX_LEVEL - currentBucketFill;
                FluidPhysics.SetLevel(world, waterBlockPos, waterBlockLevel - bucketSpace, blockState);
                Fluids.WATER.getBucketFillSound().ifPresent(sound -> user.playSound(sound, 1.0f, 1.0f));

                nbt.putByte(FluidPhysics.LEVEL_KEY, (byte) Math.min(currentBucketFill + waterBlockLevel, FluidPhysics.MAX_LEVEL));
                var outStack = new ItemStack(Items.WATER_BUCKET);
                outStack.setNbt(nbt);
                ItemUsage.exchangeStack(itemStack, user, outStack);
                cir.setReturnValue(TypedActionResult.success(outStack));
            }
            else if (currentBucketFill > FluidPhysics.EMPTY) // Handle bucket empty
            {
                int swapAmount = user.isSneaking() ? 1 : currentBucketFill;
                FluidPhysics.SetLevel(world, waterBlockPos, waterBlockLevel + swapAmount, blockState);
                ((BucketItemAccessor) this).invokePlayEmptyingSound(user, world, waterBlockPos);

                currentBucketFill -= swapAmount;
                if (currentBucketFill > FluidPhysics.EMPTY)
                {
                    nbt.putByte(FluidPhysics.LEVEL_KEY, (byte) currentBucketFill);
                    itemStack.setNbt(nbt);
                }
                else
                    itemStack = BucketItem.getEmptiedStack(itemStack, user);
                cir.setReturnValue(TypedActionResult.success(itemStack));
            }
        }
    }
}
