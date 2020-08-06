package br.com.finalcraft.forgerestrictor.protectionhandler;

import br.com.finalcraft.forgerestrictor.protectionhandler.integration.GriefPreventionPlusHandler;
import br.com.finalcraft.forgerestrictor.protectionhandler.integration.PlotSquaredHandler;
import br.com.finalcraft.forgerestrictor.protectionhandler.integration.WorldGuardHandler;

import java.util.Arrays;

public enum ProtectionPlugins {
	GriefPreventionPlus(GriefPreventionPlusHandler.class),
	WorldGuard(WorldGuardHandler.class),
	PlotSquared(PlotSquaredHandler.class);

	private Class<? extends ProtectionHandler> clazz;
	private ProtectionHandler handler;
	private boolean enabled=true;

	private static ProtectionHandler[] handlersList={};

	ProtectionPlugins(Class<? extends ProtectionHandler> clazz) {
		this.clazz=clazz;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		generateHandlersList();
	}
	
	public void createHandler() throws InstantiationException, IllegalAccessException {
		this.handler = this.clazz.newInstance();
		generateHandlersList();
	}
	
	public void removeHandler() {
		this.handler = null;
		generateHandlersList();
	}
	
	private static void generateHandlersList() {
		ProtectionHandler[] arr = new ProtectionHandler[ProtectionPlugins.values().length];
		
		int i=0;
		for (ProtectionPlugins pp : ProtectionPlugins.values()) {
			if (pp.enabled && pp.handler!=null) {
				arr[i]=pp.handler;
				i++;
			}
		}
		handlersList=Arrays.copyOf(arr, i);
	}
	
	public static ProtectionHandler[] getHandlers() {
		return handlersList;
	}
	
	public static String[] getNameList() {
		String[] list = new String[ProtectionPlugins.values().length];
		int i=0;
		for (ProtectionPlugins pp : ProtectionPlugins.values()) {
			list[i]=pp.toString();
			i++;
		}
		return list;
	}
}
