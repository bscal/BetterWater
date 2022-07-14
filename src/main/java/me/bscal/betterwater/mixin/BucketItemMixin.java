package me.bscal.betterwater.mixin;

import me.bscal.betterwater.FluidPhysics;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends ItemMixin implements FluidModificationItem
{
    @Shadow
    @Final
    private Fluid fluid;

    @Override
    public void appendTooltipMixin(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci)
    {
        if (IsWater(this.fluid) && stack.hasNbt())
        {
            var nbt = stack.getNbt();
            if (!nbt.contains(FluidPhysics.LEVEL_KEY)) return;
            var level = nbt.getByte(FluidPhysics.LEVEL_KEY);
            tooltip.add(Text.of(String.format("Fill Level: %d/8", level)));
        }
    }

    @Override
    public void appendStacksMixin(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci)
    {
        if (this.isIn(group) && IsWater(this.fluid))
        {
            var stack = new ItemStack(this);
            FluidPhysics.SetItemWaterLevelNbt(stack, (byte) 8);
            stacks.add(stack);
        }
    }

    @Inject(method = "placeFluid", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
            cancellable = true)
    public void placeFluid(PlayerEntity player, World world, BlockPos pos, BlockHitResult hitResult, CallbackInfoReturnable<Boolean> cir)
    {
        if (player.isSneaking() && IsWater(this.fluid))
        {
            // placeFluid() does not contain Hand variable, we have to get main hand item.
            var bucket = player.getMainHandStack();
            var level = FluidPhysics.GetItemWaterLevelNbt(bucket);
            if (level > 1)
            {
                var fluidState = world.getFluidState(pos);
                var blockState = world.getBlockState(pos);
                FluidPhysics.SetLevel(world, pos, FluidPhysics.GetLevel(fluidState) + 1, blockState);
                ((BucketItemAccessor) this).invokePlayEmptyingSound(player, world, pos);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "use", at = @At(value = "RETURN", ordinal = 4), cancellable = true)
    public void useEmptyBucket(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir)
    {
        if (user.isSneaking() && IsWater(this.fluid))
        {
            var stack = user.getStackInHand(hand);
            var level = FluidPhysics.GetItemWaterLevelNbt(stack);
            if (level > 1)
            {
                FluidPhysics.SetItemWaterLevelNbt(stack, --level);
                cir.setReturnValue(TypedActionResult.success(stack));
            }
        }
    }

    // Captures locals because we need to check if fluid is water, _ means unused
    @Inject(method = "use", at = @At(value = "RETURN", ordinal = 2), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void useFillBucket(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir,
                              ItemStack _0, BlockHitResult _1, BlockPos _2, Direction _3, BlockPos _4, BlockState _5,
                              FluidDrainable _6, ItemStack filledBucketStack)
    {
        if (filledBucketStack.getItem() instanceof BucketItemAccessor bucketItem && IsWater(bucketItem.getFluid()))
        {
            FluidPhysics.SetItemWaterLevelNbt(filledBucketStack, (byte) 8);
            cir.setReturnValue(TypedActionResult.success(filledBucketStack));
        }
    }

    private boolean IsWater(Fluid fluid)
    {
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }
}
