package me.bscal.betterwater;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FluidPhysics
{

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 8;
    public static final int EMPTY = 0;

    private FluidPhysics() {}

    public static int GetLevel(FluidState fluidstate)
    {
        int level;
        if (fluidstate.getFluid() instanceof WaterFluid.Still)
            level = MAX_LEVEL;
        else if (fluidstate.getFluid() instanceof WaterFluid.Flowing)
            level = fluidstate.getLevel();
        else
            level = EMPTY;
        return level;
    }

    public static void SetLevel(World world, BlockPos pos, int level, BlockState state)
    {
        if (level == EMPTY)
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        else if (level == MAX_LEVEL && state.getBlock() instanceof FluidFillable)
            world.setBlockState(pos, Fluids.WATER.getDefaultState().getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        else
            world.setBlockState(pos, Fluids.FLOWING_WATER.getFlowing(level, false).getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
    }

}
