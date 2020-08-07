package br.com.finalcraft.forgerestrictor.config;

import br.com.finalcraft.forgerestrictor.ForgeRestrictor;
import br.com.finalcraft.forgerestrictor.protectionhandler.ProtectionPlugins;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Config {
	final static String configFilePath = "plugins" + File.separator + "ForgeRestrictor" + File.separator + "config.yml";
	private File configFile;
	public FileConfiguration config;

	public List<ListedItem> whitelist;
	public List<ListedRangedItem> ranged;
	public List<ListedRangedItem> aoe;
	public HashSet<String> enabledWorlds;
	private boolean allWorlds = true;

	public int confiscateTicks;

	public boolean confiscateLog;

	public Config() {
		this.configFile = new File(configFilePath);
		this.config = YamlConfiguration.loadConfiguration(this.configFile);
		this.load();
	}

	public void load() {
		ProtectionPlugins.GriefPreventionPlus.setEnabled(this.config.getBoolean("Protection.GriefPreventionPlus", true));
		ProtectionPlugins.WorldGuard.setEnabled(this.config.getBoolean("Protection.WorldGuard", true));
		ProtectionPlugins.PlotSquared.setEnabled(this.config.getBoolean("Protection.PlotSquared", true));

		this.whitelist=new ArrayList<ListedItem>();
		for (String serialized : this.config.getStringList("Whitelist")) {
			try {
				this.whitelist.add(new ListedItem(serialized));
			} catch (Exception e) {
				ForgeRestrictor.getInstance().getLogger().warning("Invalid Whitelist element in config: "+serialized);
			}
		}
		
		this.ranged=new ArrayList<ListedRangedItem>();
		for (String serialized : this.config.getStringList("Ranged")) {
			try {
				this.ranged.add(new ListedRangedItem(serialized));
			} catch (Exception e) {
				ForgeRestrictor.getInstance().getLogger().warning("Invalid Ranged element in config: "+serialized);
			}
		}
		
		this.aoe=new ArrayList<ListedRangedItem>();
		for (String serialized : this.config.getStringList("AoE")) {
			try {
				this.aoe.add(new ListedRangedItem(serialized));
			} catch (Exception e) {
				ForgeRestrictor.getInstance().getLogger().warning("Invalid AoE element in config: "+serialized);
			}
		}

		this.enabledWorlds = new HashSet<>(this.config.getStringList("EnabledWorlds"));
		if (enabledWorlds.size() == 0){
			enabledWorlds.add("*");
		}
		this.allWorlds = enabledWorlds.contains("*");

		this.confiscateTicks = this.config.getInt("ConfiscateTicks", 3);
		this.confiscateLog = this.config.getBoolean("ConfiscateLog", true);
		
		this.save();
	}

	public void save() {
		try {
			this.config.set("Protection.GriefPreventionPlus", ProtectionPlugins.GriefPreventionPlus.isEnabled());
			this.config.set("Protection.WorldGuard", ProtectionPlugins.WorldGuard.isEnabled());
			
			this.config.set("Whitelist", serializeListedItemList(this.whitelist));
			this.config.set("Ranged", serializeListedItemList(this.ranged));
			this.config.set("AoE", serializeListedItemList(this.aoe));
			this.config.set("EnabledWorlds", new ArrayList<>(enabledWorlds));

			this.config.set("ConfiscateTicks", this.confiscateTicks);
			this.config.set("ConfiscateLog", this.confiscateLog);
			
			this.config.save(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addWhitelistItem(ListedItem item) {
		this.whitelist.add(item);
		this.save();
	}

	public void addRangedItem(ListedRangedItem item) {
		this.ranged.add(item);
		this.save();
	}

	public void addAoEItem(ListedRangedItem item) {
		this.aoe.add(item);
		this.save();
	}

	public boolean isWorldEnabled(World world){
		if (allWorlds) return true;
		return enabledWorlds.contains(world.getName());
	}

	public ListedItem getWhitelistItem(Material material, Short data, String world) {
		ForgeRestrictor.getInstance().getLogger().info("getWhitelistItem");
		return getListedItem(this.whitelist, material, data, world);
	}

	public ListedRangedItem getRangedItem(Material material, Short data, String world) {
		return (ListedRangedItem) getListedItem(this.ranged, material, data, world);
	}
	
	public ListedRangedItem getAoEItem(Material material, Short data, String world) {
		return (ListedRangedItem) getListedItem(this.aoe, material, data, world);
	}
	
	private static ListedItem getListedItem(List<? extends ListedItem> list, Material material, Short data, String world) {
		for(ListedItem item : list) {
			if (item.equals(material, data, world)) {
				return item;
			}
		}
		return null;
	}


	public ListedItem matchWhitelistItem(Material material, Short data, String world) {
		return matchListedItem(this.whitelist, material, data, world);
	}

	public ListedRangedItem matchRangedItem(Material material, Short data, String world) {
		return (ListedRangedItem) matchListedItem(this.ranged, material, data, world);
	}

	public ListedRangedItem matchAoEItem(Material material, Short data, String world) {
		return (ListedRangedItem) matchListedItem(this.aoe, material, data, world);
	}
	
	private static ListedItem matchListedItem(List<? extends ListedItem> list, Material material, Short data, String world) {
		for(ListedItem item : list) {
			if (item.match(material, data, world)) {
				return item;
			}
		}
		return null;
	}
	
	
	public boolean removeWhitelistItem(Material material, Short data, String world) {
		return removeListedItem(this.whitelist, material, data, world);
	}
	public boolean removeRangedItem(Material material, Short data, String world) {
		return removeListedItem(this.ranged, material, data, world);
	}
	public boolean removeAoEItem(Material material, Short data, String world) {
		return removeListedItem(this.aoe, material, data, world);
	}
	
	private static boolean removeListedItem(List<? extends ListedItem> list, Material material, Short data, String world) {
		Iterator<? extends ListedItem> iterator=list.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().equals(material, data, world)) {
				iterator.remove();
				return true;
			}
		}

		return false;
	}
	
	private static String[] serializeListedItemList(List<? extends ListedItem> list) {
		String[] serializedList = new String[list.size()];
		int i=0;
		for (ListedItem item : list) {
			serializedList[i]=item.serialize();
			i++;
		}
		return serializedList;
	}
}