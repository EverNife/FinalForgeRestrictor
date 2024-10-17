package br.com.finalcraft.finalforgerestrictor.protectionhandler;

import br.com.finalcraft.evernifecore.locale.FCLocaleManager;
import br.com.finalcraft.finalforgerestrictor.FinalForgeRestrictor;
import br.com.finalcraft.finalforgerestrictor.config.ConfigManager;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.integration.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ProtectionPlugins {

	public static GriefPreventionPlusHandler GriefPreventionPlus;
	public static GriefPreventionHandler GriefPrevention;
	public static WorldGuardHandler WorldGuard;
	public static PlotSquaredHandler PlotSquared;
	public static IridiumSkyBlockHandler IridiumSkyBlock;
	private static final List<ProtectionHandler> ALL_ENABLED_HANDLERS = new ArrayList<>();

	public static void initialize(){
		ALL_ENABLED_HANDLERS.clear();

		GriefPreventionPlus = addProtectionHandler("GriefPreventionPlus", GriefPreventionPlusHandler::new);
		GriefPrevention 	= addProtectionHandler("GriefPrevention", GriefPreventionHandler::new);
		WorldGuard 			= addProtectionHandler("WorldGuard", WorldGuardHandler::new);
		PlotSquared 		= addProtectionHandler("PlotSquared", PlotSquaredHandler::new);
		IridiumSkyBlock 	= addProtectionHandler("IridiumSkyBlock", IridiumSkyBlockHandler::new);

		ConfigManager.getMainConfig().setComment("ProtectionIntegration", "List of plugins FinalForgeRestrictor will look up to enchance protection!");
		ConfigManager.getMainConfig().saveIfNewDefaults();

		if (ALL_ENABLED_HANDLERS.size() == 0){
			FinalForgeRestrictor.getLog().warning("There are no suported protection plugins installed/enabled! FinalForgeRestrictor will not be able to enchance protection!");
		}
	}

	public static <H extends ProtectionHandler> @Nullable H addProtectionHandler(String pluginName, Supplier<H> supplier){
		if (Bukkit.getPluginManager().isPluginEnabled(pluginName) && ConfigManager.getMainConfig().getOrSetDefaultValue("ProtectionIntegration." + pluginName, true)){
			try {
				H handler = supplier.get();
				ALL_ENABLED_HANDLERS.add(handler);

				//Load Locales
				JavaPlugin javaPlugin = JavaPlugin.getProvidingPlugin(handler.getClass()); //Maybe some third-part plugins want to add new ProtectionHandlers
				FCLocaleManager.loadLocale(javaPlugin, true, handler.getClass());

				return handler;
			}catch (Throwable e){
				FinalForgeRestrictor.getLog().severe("Failed to load ProtectionHandler: " + pluginName);
				e.printStackTrace();
			}
		}
		return null;
	}


	public static List<ProtectionHandler> getHandlers() {
		return ALL_ENABLED_HANDLERS;
	}

}
