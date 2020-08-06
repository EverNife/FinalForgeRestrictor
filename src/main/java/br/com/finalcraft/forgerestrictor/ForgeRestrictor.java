package br.com.finalcraft.forgerestrictor;

import br.com.finalcraft.forgerestrictor.command.CommandExec;
import br.com.finalcraft.forgerestrictor.config.Config;
import br.com.finalcraft.forgerestrictor.listener.EventListener;
import br.com.finalcraft.forgerestrictor.protectionhandler.ProtectionPlugins;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ForgeRestrictor extends JavaPlugin {
	private static ForgeRestrictor instance;
	public Config config;
	EventListener eventListener;
	public CommandExec commandExec;

	@Override
	public void onEnable() {
		instance=this;

		this.config=new Config();
		
		this.eventListener=new EventListener(this);
		this.getServer().getPluginManager().registerEvents(this.eventListener, this);
		
		this.commandExec=new CommandExec(this);
		this.getCommand("forgerestrictor").setExecutor(this.commandExec);
		
		for (ProtectionPlugins pp : ProtectionPlugins.values()) {
			Plugin plugin = this.getServer().getPluginManager().getPlugin(pp.toString());
			if (plugin!=null && plugin.isEnabled()) {
				this.eventListener.pluginEnable(pp.toString());
			}
		}

	}

	public static ForgeRestrictor getInstance() {
		return instance;
	}

	public static void setInstance(ForgeRestrictor instance) {
		ForgeRestrictor.instance = instance;
	}
}
