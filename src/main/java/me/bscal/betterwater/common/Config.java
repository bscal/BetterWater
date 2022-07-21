package me.bscal.betterwater.common;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.io.File;
import java.io.IOException;

public class Config
{

    public static final String EXTENSION = ".conf";

    public Settings Settings;
    public CommentedConfigurationNode RootNode;
    public HoconConfigurationLoader Loader;

    public Config(String path)
    {
        File saveFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), path + EXTENSION);
        var configSettings = ConfigurationOptions.defaults().shouldCopyDefaults(true);
        Loader = HoconConfigurationLoader.builder().file(saveFile).defaultOptions(configSettings).prettyPrinting(true).build();
        try
        {
            RootNode = Loader.load();
            Settings = RootNode.get(Settings.class);
            Save();
        } catch (IOException e)
        {
            System.err.println("Config could not load: " + e.getMessage());
            if (e.getCause() != null)
                e.getCause().printStackTrace();
        }
    }

    public void Save()
    {
        try
        {
            //m_RootNode.set(Settings.class, Settings);
            Loader.save(RootNode);
        } catch (IOException e)
        {
            System.err.println("Config could not load: " + e.getMessage());
            if (e.getCause() != null)
                e.getCause().printStackTrace();
        }
    }

    @ConfigSerializable
    public static class Settings
    {
        @Setting("BottleRequiredWaterLevel")
        @Comment("Level of water required to fill a glass bottle. 0 - Default Minecraft")
        public int BottleWaterLevel = 1;

        @Setting("EvaporationChance")
        @Comment("Chance to remove level 1 water blocks. Values between 0.0 - 1.0. Ticking these are rather odd, best to keep this number " +
                " so level 1 blocks don't linger")
        public double EvaporationChance = 0.05;

        @Setting("RainfallFillChance")
        @Comment("Chance for water to be filled by rain. Values 0.0 - 1.0")
        public double RainfallFillChance = 0.05;

        @Setting("EnableBucketPickup")
        @Comment("Allows you to pickup non water source blocks.")
        public boolean EnableBucketMixin = true;

    }

}
