package me.bscal.betterwater;

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

    private static final String EXTENSION = ".conf";

    public Settings Settings;

    private transient CommentedConfigurationNode m_RootNode;
    private transient final HoconConfigurationLoader m_Loader;

    public Config(String path)
    {
        File saveFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), path + EXTENSION);
        var configSettings = ConfigurationOptions.defaults().shouldCopyDefaults(true);
        m_Loader = HoconConfigurationLoader.builder().file(saveFile).defaultOptions(configSettings).prettyPrinting(true).build();
        try
        {
            m_RootNode = m_Loader.load();
            Settings = m_RootNode.get(Settings.class);
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
            m_Loader.save(m_RootNode);
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
        @Comment("Chance a level 1 water block will be evaporated. Values 0.0 - 1.0")
        public double EvaporationChance = 0.05;

        @Setting("EnableBucketPickup")
        @Comment("Allows you to pickup non water source blocks.")
        public boolean EnableBucketMixin = true;
    }

}
