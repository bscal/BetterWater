package me.bscal.betterwater;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;

public class Config
{


    public CommentedConfigurationNode RootNode;
    private final YamlConfigurationLoader Loader;

    public Config(String path)
    {
        File saveFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), path);
        Loader = YamlConfigurationLoader.builder().file(saveFile).indent(4).build();
        try
        {
            var configSettings = ConfigurationOptions.defaults().shouldCopyDefaults(true);
            RootNode = Loader.load(configSettings);
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
            Loader.save(RootNode);
        } catch (IOException e)
        {
            System.err.println("Config could not load: " + e.getMessage());
            if (e.getCause() != null)
                e.getCause().printStackTrace();
        }
    }


}
