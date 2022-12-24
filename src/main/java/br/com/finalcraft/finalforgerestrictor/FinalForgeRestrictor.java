package br.com.finalcraft.finalforgerestrictor;

import br.com.finalcraft.evernifecore.ecplugin.annotations.ECPlugin;
import br.com.finalcraft.evernifecore.listeners.base.ECListener;
import br.com.finalcraft.finalforgerestrictor.command.CommandRegisterer;
import br.com.finalcraft.finalforgerestrictor.config.ConfigManager;
import br.com.finalcraft.finalforgerestrictor.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

@ECPlugin(
		bstatsID = "17169"
)
public class FinalForgeRestrictor extends JavaPlugin {

	public static FinalForgeRestrictor instance;

	public static void info(String msg){
		instance.getLogger().info("[Info] " + msg);
	}

	public static void warning(String msg) {
		instance.getLogger().warning("[Warning] " + msg);
	}

	@Override
	public void onEnable() {
		instance = this;

		info("Registering Commands...");
		CommandRegisterer.registerCommands(this);

		info("Loading Configuration...");
		ConfigManager.initialize(this);

		ECListener.register(this, PlayerListener.class);
	}

	@ECPlugin.Reload
	public void onReload(){
		ConfigManager.initialize(this);
	}

}
