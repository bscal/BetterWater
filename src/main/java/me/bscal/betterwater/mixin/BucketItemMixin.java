package me.bscal.betterwater.mixin;

import me.bscal.betterwater.BetterWater;
import me.bscal.betterwater.FluidPhysics;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// TODO CLEAN THIS UP, ui, pickup by 1s?
@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends Item
        implements FluidModificationItem
{

    private static final String LEVEL_KEY = BetterWater.MOD_ID + ":level";

    @Shadow
    @Final
    private Fluid fluid;

    public BucketItemMixin(Settings settings)
    {
        super(settings);
    }

    @Inject(method = "placeFluid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
            cancellable = true)
    public void placeFluid(PlayerEntity player, World world, BlockPos pos, BlockHitResult hitResult, CallbackInfoReturnable<Boolean> cir)
    {
        if (player.isSneaking())
        {
            BucketItem bucketItem = (BucketItem) (Object) this;
            BucketItemAccessor bucketAccessor = (BucketItemAccessor) bucketItem;
            if (!IsWater(this.fluid)) return;
            // placeFluid() does not contain Hand variable, we have to get main hand item.
            var bucket = player.getMainHandStack();
            if (bucket.hasNbt() && bucket.getNbt().getByte(LEVEL_KEY) > 1)
            {
                var fluidState = world.getFluidState(pos);
                var blockState = world.getBlockState(pos);
                FluidPhysics.SetLevel(world, pos, FluidPhysics.GetLevel(fluidState) + 1, blockState);
                bucketAccessor.invokePlayEmptyingSound(player, world, pos);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "use", at = @At(value = "RETURN", ordinal = 4), cancellable = true)
    public void useEmptyBucket(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir)
    {
        BucketItem bucketItem = (BucketItem) (Object) this;
        if (!IsWater(this.fluid)) return;
        var stack = user.getStackInHand(hand);
        if (!stack.hasNbt()) return;
        var nbt = stack.getNbt();
        byte level = nbt.getByte(LEVEL_KEY);
        if (level > 1)
        {
            nbt.putByte(LEVEL_KEY, --level);
            stack.setNbt(nbt);
            cir.setReturnValue(TypedActionResult.success(stack));
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
            var nbt = filledBucketStack.getOrCreateNbt();
            nbt.putByte(LEVEL_KEY, (byte) 8);
            filledBucketStack.setNbt(nbt);
            cir.setReturnValue(TypedActionResult.success(filledBucketStack));
        }
    }

    private boolean IsWater(Fluid fluid)
    {
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }
}
