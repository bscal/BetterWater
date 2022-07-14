package me.bscal.betterwater;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public final class FluidPhysics
{

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 8;
    public static final int EMPTY = 0;
    public static final String LEVEL_KEY = BetterWater.MOD_ID + ":level";

    private FluidPhysics()
    {
    }

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

    public static boolean TryRemoveLevels(World world, BlockPos pos, BlockState state, int levelsNeeded)
    {
        int level = GetLevel(world.getFluidState(pos));
        if (level >= levelsNeeded)
        {
            SetLevel(world, pos, level - levelsNeeded, state);
            return true;
        }
        return false;
    }

    public static void SetItemWaterLevelNbt(ItemStack outStack, byte amount)
    {
        assert !outStack.isEmpty(): "stack cannot be empty";
        var nbt = outStack.getOrCreateNbt();
        nbt.putByte(LEVEL_KEY, MathHelper.clamp(amount, (byte)0, (byte)8));
        outStack.setNbt(nbt);
    }

    public static byte GetItemWaterLevelNbt(ItemStack stack)
    {
        assert !stack.isEmpty(): "stack cannot be empty";
        if (!stack.hasNbt()) return 0;
        return stack.getNbt().getByte(LEVEL_KEY);
    }

/*    public static BlockHitResult Raycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        float f = player.getPitch();
        float g = player.getYaw();
        float toDegrees = MathHelper.PI / 180.0f;
        float gDegress = -g * toDegrees;
        float h = MathHelper.cos(gDegress - MathHelper.PI);
        float i = MathHelper.sin(gDegress - MathHelper.PI);
        float fDegress = -f * toDegrees;
        float j = -MathHelper.cos(fDegress);
        float k = MathHelper.sin(fDegress);
        Vec3d vec3d = player.getEyePos();
        Vec3d vec3d2 = vec3d.add(i * j * 5.0f, k * 5.0f, h * j * 5.0f);
        return world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));
    }*/

}
