package me.bscal.betterwater;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.bscal.betterwater.common.Config;
import me.bscal.betterwater.common.FluidTicker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class BetterWater implements ModInitializer
{

    public static final String MOD_ID = "betterwater";
    public static Random MCRandom;
    public static Config Config;

    @Override
    public void onInitialize()
    {
        MCRandom = Random.create();
        Config = new Config(MOD_ID);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            FluidTicker.TickServer(server);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
        {
            dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("reload_betterwater")
                    .requires((src) -> src.hasPermissionLevel(2))
                    .executes(context ->
                    {
                        Config = new Config(MOD_ID);
                        return 0;
                    }));
        });
    }

    public static Config.Settings Settings()
    {
        assert Config != null: "Config must not be null";
        assert Config.Settings != null: "Config.Settings is null, how did this happen?";
        return Config.Settings;
    }


}
