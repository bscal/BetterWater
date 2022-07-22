package me.bscal.betterwater.common;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.bscal.betterwater.BetterWater;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class FluidTicker
{
    public static final int TICK_RATE = 1200;
    public static final Queue<Ticker> Tickers = new PriorityQueue<>(Comparator.comparingInt(o -> o.Tick));
    public static final ObjectOpenHashSet<BlockPos> TickerSet = new ObjectOpenHashSet<>();

    public static void Add(ServerWorld world, BlockPos pos)
    {
        var ticker = new Ticker();
        ticker.Tick = world.getServer().getTicks() + TICK_RATE;
        ticker.World = world;
        ticker.Pos = pos;
        Tickers.add(ticker);
        TickerSet.add(pos);
    }

    public static void TickServer(MinecraftServer server)
    {
        int currentTick = server.getTicks();
        var iter = Tickers.iterator();
        while (iter.hasNext())
        {
            var ticker = iter.next();
            if (ticker.Tick > currentTick) break;
            iter.remove();
            if (++ticker.Counter < 3)
            {
                ticker.Tick = currentTick + TICK_RATE;
                Tickers.add(ticker);
            }
            else
                TickerSet.remove(ticker.Pos);

            if (ticker.World == null || !ticker.World.isChunkLoaded(ChunkSectionPos.toLong(ticker.Pos))) continue;
            FluidState state = ticker.World.getFluidState(ticker.Pos);
            if (state.getFluid() != Fluids.WATER || state.getFluid() != Fluids.FLOWING_WATER) continue;
            int level = FluidPhysics.GetLevel(state);
            if (level == 1 && ticker.World.hasRain(ticker.Pos) && BetterWater.MCRandom.nextFloat() <= BetterWater.Settings().EvaporationChance)
            {
                ticker.World.setBlockState(ticker.Pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                ticker.World.updateNeighborsAlways(ticker.Pos, ticker.World.getBlockState(ticker.Pos).getBlock());
            }
        }
    }

    public static class Ticker
    {
        public int Tick;
        public ServerWorld World;
        public BlockPos Pos;
        public int Counter;
    }
}
