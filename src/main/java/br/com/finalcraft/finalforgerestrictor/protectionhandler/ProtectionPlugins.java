package br.com.finalcraft.finalforgerestrictor.protectionhandler;

import br.com.finalcraft.evernifecore.locale.FCLocaleManager;
import br.com.finalcraft.finalforgerestrictor.FinalForgeRestrictor;
import br.com.finalcraft.finalforgerestrictor.config.ConfigManager;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.integration.GriefPreventionPlusHandler;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.integration.PlotSquaredHandler;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.integration.WorldGuardHandler;
import dev.triumphteam.gui.guis.BaseGui;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProtectionPlugins {

	public static GriefPreventionPlusHandler GriefPreventionPlus;
	public static WorldGuardHandler WorldGuard;
	public static PlotSquaredHandler PlotSquared;
	private static final List<ProtectionHandler> ALL_ENABLED_HANDLERS = new ArrayList<>();

	public static void initialize(){
		ALL_ENABLED_HANDLERS.clear();

		GriefPreventionPlus = addProtectionHandler("GriefPreventionPlus", GriefPreventionPlusHandler::new);
		WorldGuard 			= addProtectionHandler("WorldGuard", WorldGuardHandler::new);
		PlotSquared 		= addProtectionHandler("PlotSquared", PlotSquaredHandler::new);

		ConfigManager.getMainConfig().setComment("ProtectionIntegration", "List of plugins FinalForgeRestrictor will look up to enchance protection!");
		ConfigManager.getMainConfig().saveIfNewDefaults();

		if (ALL_ENABLED_HANDLERS.size() == 0){
			FinalForgeRestrictor.warning("There are no suported protection plugins installed/enabled! FinalForgeRestrictor will not be able to enchance protection!");
		}
	}

	public static <H extends ProtectionHandler> @Nullable H addProtectionHandler(String pluginName, Supplier<H> supplier){
		if (Bukkit.getPluginManager().isPluginEnabled(pluginName) && ConfigManager.getMainConfig().getOrSetDefaultValue("ProtectionIntegration." + pluginName, true)){
			H handler = supplier.get();
			ALL_ENABLED_HANDLERS.add(handler);

			//Load Locales
			JavaPlugin javaPlugin = JavaPlugin.getProvidingPlugin(handler.getClass()); //Maybe some third-part plugins want to add new ProtectionHandlers
			FCLocaleManager.loadLocale(javaPlugin, true, handler.getClass());

			return handler;
		}
		return null;
	}


	public static List<ProtectionHandler> getHandlers() {
		return ALL_ENABLED_HANDLERS;
	}

}