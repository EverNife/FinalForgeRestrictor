package br.com.finalcraft.finalforgerestrictor.protectionhandler;

import br.com.finalcraft.evernifecore.locale.FCLocaleManager;
import br.com.finalcraft.evernifecore.util.FCReflectionUtil;
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
	public static ProtectionHandler PlotSquared;
	public static IridiumSkyBlockHandler IridiumSkyBlock;
	public static SuperiorSkyBlockHandler SuperiorSkyBlock;
	public static FactionsHandler Factions;
	private static final List<ProtectionHandler> ALL_ENABLED_HANDLERS = new ArrayList<>();

	public static void initialize(){
		ALL_ENABLED_HANDLERS.clear();

		FinalForgeRestrictor.getLog().info("Initializing ProtectionHandlers...");

		GriefPreventionPlus = addProtectionHandler("GriefPreventionPlus", () -> new GriefPreventionPlusHandler());
		GriefPrevention 	= addProtectionHandler("GriefPrevention", () -> new GriefPreventionHandler());
		WorldGuard 			= addProtectionHandler("WorldGuard", () -> new WorldGuardHandler());
		PlotSquared 		= addProtectionHandler("PlotSquared", () -> (ProtectionHandler) FCReflectionUtil.getConstructor("br.com.finalcraft.finalforgerestrictor.protectionhandler.integration.PlotSquaredHandler").invoke());
		IridiumSkyBlock 	= addProtectionHandler("IridiumSkyBlock", () -> new IridiumSkyBlockHandler());
		SuperiorSkyBlock 	= addProtectionHandler("SuperiorSkyBlock", () -> new SuperiorSkyBlockHandler());
		Factions 			= addProtectionHandler("Factions", () -> new FactionsHandler());

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

				FinalForgeRestrictor.getLog().info(" - ProtectionHandler loaded: " + pluginName);

				return handler;
			}catch (Throwable t){
				FinalForgeRestrictor.getLog().severe("Failed to load ProtectionHandler: " + pluginName);
				t.printStackTrace();
			}
		}
		return null;
	}


	public static List<ProtectionHandler> getHandlers() {
		return ALL_ENABLED_HANDLERS;
	}

}
