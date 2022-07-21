package me.bscal.betterwater.common;

import net.minecraft.fluid.Fluid;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FluidTicker
{

    public static List<Ticker> Tickers = new ArrayList<>();

    public static void TickServer(MinecraftServer server)
    {
/*        int currentTick = server.getTicks();
        var iter = Tickers.iterator();
        while (iter.hasNext())
        {
            var ticker = iter.next();

        }

        for (Ticker ticker : iter)
        {
            if (!ticker.Cb.OnTick();
        }*/
    }

    public record Ticker(
            int Tick,
            World World,
            BlockPos Pos,
            Fluid Fluid,
            TickCallback Cb)
    {
    }

    interface TickCallback
    {
        boolean OnTick();
    }

}
