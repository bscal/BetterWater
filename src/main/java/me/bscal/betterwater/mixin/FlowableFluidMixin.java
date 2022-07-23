package me.bscal.betterwater.mixin;

import me.bscal.betterwater.BetterWater;
import me.bscal.betterwater.common.FluidPhysics;
import me.bscal.betterwater.common.FluidTicker;
import me.bscal.betterwater.common.Vec2i;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.apache.logging.log4j.core.jmx.Server;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin extends Fluid
{
    private static final Direction[] DIRECTIONS = new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    @Inject(method = "onScheduledTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/fluid/FlowableFluid;tryFlow(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/FluidState;)V"),
            cancellable = true)
    protected void OnScheduledTick(World world, BlockPos pos, FluidState state, CallbackInfo ci)
    {
        Fluid fluid = state.getFluid();
        if (fluid instanceof WaterFluid.Flowing || fluid instanceof WaterFluid.Still)
        {
            ci.cancel();
            BlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof FluidFillable) return;

            int level = FluidPhysics.GetLevel(state);
            if (level <= FluidPhysics.EMPTY)
            {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                return;
            }

/*            if (!world.isClient && level == 1)
            {
                FluidTicker.GetOrCreate((ServerWorld) world).Add(pos);
                return;
            }*/

            if (level == 1)
            {
                if (!world.hasRain(pos) && BetterWater.MCRandom.nextFloat() <= BetterWater.Settings().EvaporationChance)
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                else
                    world.createAndScheduleFluidTick(pos, state.getFluid(), 10);
                return;
            }

            BlockPos downPos = pos.down();
            BlockState downState = world.getBlockState(downPos);
            FluidState downFluidState = downState.getFluidState();
            int downLevel = FluidPhysics.GetLevel(downFluidState);

            if (CanFlowDown(state, downState, downFluidState, downLevel))
            {
                var levels = GetLevelDiffs(level, downLevel);
                FluidPhysics.SetLevel(world, pos, levels.x(), blockState);
                FluidPhysics.SetLevel(world, downPos, levels.y(), downState);
            }
            else
            {
                int currentLevel = level;
                Shuffle(DIRECTIONS);
                for (Direction direction : DIRECTIONS)
                {
                    if (currentLevel <= FluidPhysics.MIN_LEVEL) break;
                    BlockPos dirPos = pos.offset(direction);
                    BlockState dirState = world.getBlockState(dirPos);
                    FluidState dirFluidState = dirState.getFluidState();
                    int dirLevel = FluidPhysics.GetLevel(dirFluidState);

                    if (CanFlowSideways(state, currentLevel, dirState, dirFluidState, dirLevel))
                    {
                        var levels = GetLevelDiffsFlow1(currentLevel, dirLevel);
                        currentLevel = levels.x();
                        FluidPhysics.SetLevel(world, dirPos, levels.y(), dirState);
                    }
                }
                FluidPhysics.SetLevel(world, pos, currentLevel, blockState);
            }
        }
    }

    public boolean CanFlowDown(FluidState srcFluid, BlockState dstState, FluidState dstFluid, int dstLevel)
    {
        return dstLevel < FluidPhysics.MAX_LEVEL && CanMoveTo(srcFluid, dstState, dstFluid);
    }

    public boolean CanFlowSideways(FluidState srcFluid, int srcLevel, BlockState dstState, FluidState dstFluid, int dstLevel)
    {
        return srcLevel > dstLevel && dstLevel < FluidPhysics.MAX_LEVEL && CanMoveTo(srcFluid, dstState, dstFluid);
    }

    private boolean CanMoveTo(FluidState srcFluid, BlockState dstState, FluidState dstFluid)
    {
        return dstState.isAir() || dstFluid.isOf(srcFluid.getFluid()) || dstState.getMaterial().isReplaceable() || !dstState.getMaterial().isSolid();
    }

    public Vec2i GetLevelDiffs(int srcLevel, int dstLevel)
    {
        int total = dstLevel + srcLevel;
        if (total > FluidPhysics.MAX_LEVEL)
        {
            srcLevel = total - FluidPhysics.MAX_LEVEL;
            dstLevel = FluidPhysics.MAX_LEVEL;
        }
        else
        {
            srcLevel = FluidPhysics.EMPTY;
            dstLevel = total;
        }
        return new Vec2i(srcLevel, dstLevel);
    }

    public Vec2i GetLevelDiffsFlow1(int srcLevel, int dstLevel)
    {
        return new Vec2i(srcLevel - 1, dstLevel + 1);
    }

    @Inject(at = @At("HEAD"), method = "canFlowThrough", cancellable = true)
    private void CanFlowThrough(BlockView world, Fluid fluid, BlockPos pos, BlockState state,
                                Direction face, BlockPos fromPos, BlockState fromState,
                                FluidState fluidState, CallbackInfoReturnable<Boolean> cir)
    {
        if (fluid instanceof WaterFluid)
            cir.setReturnValue(false);
    }

    @Inject(at = @At("HEAD"), method = "canFlow", cancellable = true)
    private void CanFlow(BlockView world, BlockPos fluidPos, BlockState fluidBlockState,
                         Direction flowDirection, BlockPos flowTo, BlockState flowToBlockState,
                         FluidState fluidState, Fluid fluid, CallbackInfoReturnable<Boolean> cir)
    {
        if (fluid instanceof WaterFluid)
            cir.setReturnValue(false);
    }

    @Inject(at = @At("HEAD"), method = "getUpdatedState", cancellable = true)
    private void GetUpdatedState(WorldView world, BlockPos pos, BlockState state, CallbackInfoReturnable<FluidState> cir)
    {
        if (state.getFluidState().getFluid() instanceof WaterFluid.Flowing)
            cir.setReturnValue(Fluids.FLOWING_WATER.getFlowing(state.getFluidState().getLevel(), false));
    }

    private static void Shuffle(Direction[] array)
    {
        for(int i = array.length - 1; i > 0; --i) // fisher yates shuffle
        {
            int rand = BetterWater.MCRandom.nextInt();
            int pos = ((rand < 0) ? -rand : rand) % (i + 1);
            Direction t = array[pos];
            array[pos] = array[i];
            array[i] = t;
        }
    }
}
