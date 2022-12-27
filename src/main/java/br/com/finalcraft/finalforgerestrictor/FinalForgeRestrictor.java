package br.com.finalcraft.finalforgerestrictor;

import br.com.finalcraft.evernifecore.ecplugin.annotations.ECPlugin;
import br.com.finalcraft.evernifecore.listeners.base.ECListener;
import br.com.finalcraft.evernifecore.logger.ECLogger;
import br.com.finalcraft.finalforgerestrictor.command.CommandRegisterer;
import br.com.finalcraft.finalforgerestrictor.config.ConfigManager;
import br.com.finalcraft.finalforgerestrictor.integration.worldguard.WorldGuardIntegration;
import br.com.finalcraft.finalforgerestrictor.listener.PlayerListener;
import br.com.finalcraft.finalforgerestrictor.logging.FFRDebugModule;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@ECPlugin(
		bstatsID = "17169",
		debugModuleEnum = FFRDebugModule.class
)
public class FinalForgeRestrictor extends JavaPlugin {

	public static FinalForgeRestrictor instance;

	private final ECLogger ecLogger = new ECLogger(this);

	public static ECLogger<FFRDebugModule> getLog(){
		return instance.ecLogger;
	}

	@Override
	public void onEnable() {
		instance = this;

		getLog().info("Registering Commands...");
		CommandRegisterer.registerCommands(this);

		getLog().info("Loading Configuration...");
		ConfigManager.initialize(this);

		ECListener.register(this, PlayerListener.class);

		if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")){
			WorldGuardIntegration.initialize();
		}
	}

	@ECPlugin.Reload
	public void onReload(){
		ConfigManager.initialize(this);
	}

}
