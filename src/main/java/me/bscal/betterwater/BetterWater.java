package me.bscal.betterwater;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

public class BetterWater implements ModInitializer
{

    public static final String MOD_ID = "betterwater";
    private static Config Config;

    @Override
    public void onInitialize()
    {
        Config = new Config(MOD_ID);

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
