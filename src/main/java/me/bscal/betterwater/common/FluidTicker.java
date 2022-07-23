package me.bscal.betterwater.common;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.bscal.betterwater.BetterWater;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.PersistentState;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class FluidTicker extends PersistentState
{
    public static final int TICK_RATE = 1200;
    public Queue<Ticker> Tickers = new PriorityQueue<>(Comparator.comparingInt(o -> o.Tick));
    public ObjectOpenHashSet<BlockPos> TickerSet = new ObjectOpenHashSet<>();
    public ServerWorld World;

    public static FluidTicker GetOrCreate(ServerWorld world)
    {
        var fluidTicker = world.getPersistentStateManager().getOrCreate(FluidTicker::FromNbt,
                FluidTicker::new,
                BetterWater.MOD_ID + ":FluidTicker");
        if (fluidTicker.World == null)
            fluidTicker.World = world;
        return fluidTicker;
    }

    public void Add(BlockPos pos)
    {
        var ticker = new Ticker();
        ticker.Tick = World.getServer().getTicks() + TICK_RATE;
        ticker.Pos = pos;
        if (TickerSet.add(pos))
            Tickers.add(ticker);
    }

    public void TickServer()
    {
        int currentTick = World.getServer().getTicks();
        while (Tickers.size() > 0)
        {
            var entry = Tickers.peek();
            if (entry.Tick > currentTick) return;
            Tickers.remove();

            if (++entry.Counter < 3)
                Add(entry.Pos);
            else
                TickerSet.remove(entry.Pos);

            if (!World.isChunkLoaded(ChunkSectionPos.toLong(entry.Pos))) continue;
            FluidState state = World.getFluidState(entry.Pos);
            if (state.getFluid() != Fluids.WATER || state.getFluid() != Fluids.FLOWING_WATER) continue;
            int level = FluidPhysics.GetLevel(state);
            if (level == 1 && World.hasRain(entry.Pos) && BetterWater.MCRandom.nextFloat() <= BetterWater.Settings().EvaporationChance)
            {
                World.setBlockState(entry.Pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                World.updateNeighborsAlways(entry.Pos, World.getBlockState(entry.Pos).getBlock());
            }
        }
    }

    public static FluidTicker FromNbt(NbtCompound nbt)
    {
        FluidTicker fluidTicker = new FluidTicker();
        NbtList list = nbt.getList("WorldFluidTickers", NbtList.COMPOUND_TYPE);
        for (NbtElement nbtElement : list)
        {
            NbtCompound tickerNbt = (NbtCompound) nbtElement;
            Ticker ticker = new Ticker();
            ticker.Tick = tickerNbt.getInt("ticks");
            ticker.Counter = tickerNbt.getInt("counter");
            ticker.Pos = BlockPos.fromLong(tickerNbt.getLong("pos"));
            fluidTicker.Tickers.add(ticker);
            fluidTicker.TickerSet.add(ticker.Pos);
        }
        return fluidTicker;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt)
    {
        NbtList nbtList = new NbtList();
        for (var ticker : Tickers)
        {
            NbtCompound tickerNbt = new NbtCompound();
            tickerNbt.putInt("ticks", ticker.Tick);
            tickerNbt.putInt("counter", ticker.Counter);
            tickerNbt.putLong("pos", ticker.Pos.asLong());
            nbtList.add(tickerNbt);
        }
        nbt.put("WorldFluidTickers", nbtList);
        return nbt;
    }

    public static class Ticker
    {
        public BlockPos Pos;
        public int Tick;
        public int Counter;
    }
}
