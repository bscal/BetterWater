package me.bscal.betterwater.common;

import me.bscal.betterwater.BetterWater;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public final class FluidPhysics
{

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 8;
    public static final int EMPTY = 0;
    public static final String LEVEL_KEY = BetterWater.MOD_ID + ":level";

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
        if (level <= EMPTY)
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        else if (level >= MAX_LEVEL && state.getBlock() instanceof FluidFillable)
            world.setBlockState(pos, Fluids.WATER.getDefaultState().getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        else
            world.setBlockState(pos, Fluids.FLOWING_WATER.getFlowing(Math.min(level, MAX_LEVEL), false).getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
    }

    public static boolean TryRemoveLevels(World world, BlockPos pos, BlockState state, int levelsNeeded)
    {
        levelsNeeded = MathHelper.clamp(levelsNeeded, 0, 8);
        int level = GetLevel(world.getFluidState(pos));
        if (level >= levelsNeeded)
        {
            SetLevel(world, pos, level - levelsNeeded, state);
            return true;
        }
        return false;
    }

    public static BlockHitResult Raycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling)
    {
        final float TO_DEGREES = MathHelper.PI / 180.0f;
        float yawDegrees = -player.getYaw() * TO_DEGREES;
        float h = MathHelper.cos(yawDegrees - MathHelper.PI);
        float i = MathHelper.sin(yawDegrees - MathHelper.PI);
        float pitchDegrees = -player.getPitch() * TO_DEGREES;
        float j = -MathHelper.cos(pitchDegrees);
        float k = MathHelper.sin(pitchDegrees);
        Vec3d eyePos = player.getEyePos();
        float endJ = j * 5.0f;
        Vec3d endPos = eyePos.add(i * endJ, k * 5.0f, h * endJ);
        return world.raycast(new RaycastContext(eyePos, endPos, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));
    }

}
