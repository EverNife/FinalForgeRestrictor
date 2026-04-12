package br.com.finalcraft.finalforgerestrictor.config;

import br.com.finalcraft.evernifecore.config.Config;
import br.com.finalcraft.finalforgerestrictor.config.settings.FFResSettings;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionPlugins;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private static Config mainConfig;

    public static Config getMainConfig(){
        return mainConfig;
    }

    public static void initialize(JavaPlugin instance){
        mainConfig  = new Config(instance,"config.yml",false);

        FFResSettings.initialize();
        ProtectionPlugins.initialize();
    }
}
