package br.com.finalcraft.forgerestrictor.command;

import br.com.finalcraft.forgerestrictor.ForgeRestrictor;
import br.com.finalcraft.forgerestrictor.config.ListedItem;
import br.com.finalcraft.forgerestrictor.config.ListedRangedItem;
import br.com.finalcraft.forgerestrictor.protectionhandler.ProtectionPlugins;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandExec implements CommandExecutor {
	private ForgeRestrictor instance;
	
	public CommandExec(ForgeRestrictor instance) {
		this.instance = instance;
	}

	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player=null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if (cmd.getName().equals("forgerestrictor")) {
			if (!sender.hasPermission("forgerestrictor.manage")) {
				sender.sendMessage("You don't have permission to use this command");
				return false;
			}
			
			if (args.length==0) {
				sender.sendMessage("Usage:\n/"+label+" (add|remove|list|enable|disable|reload)");
				return false;
			}
			
			String usage;
			switch(args[0].toLowerCase()) {
			case "add":
				usage="Usage:\n/"+label+" add (whitelist) ('hand'|itemid|itemname)[:(metadata)] [world]\n"
						+ "/"+label+" add (ranged|aoe) ('hand'|itemid|itemname)[:(metadata)] [range] [world]";
				if (args.length<3) {
					sender.sendMessage(usage);
					return false;
				}
				
				try {
					ListedItem listedItem=this.parseListedItemFromArgs(player, args);
					switch(args[1].toLowerCase()) {
					case "whitelist":
						if (this.instance.config.getWhitelistItem(listedItem.material, listedItem.data, listedItem.world)!=null) {
							throw new IllegalArgumentException("This item already exists.");
						}
						this.instance.config.addWhitelistItem(listedItem);
						break;
					case "ranged":
						if (this.instance.config.getRangedItem(listedItem.material, listedItem.data, listedItem.world)!=null) {
							throw new IllegalArgumentException("This item already exists.");
						}
						this.instance.config.addRangedItem((ListedRangedItem) listedItem);
						break;
					case "aoe":
						if (this.instance.config.getAoEItem(listedItem.material, listedItem.data, listedItem.world)!=null) {
							throw new IllegalArgumentException("This item already exists.");
						}
						this.instance.config.addAoEItem((ListedRangedItem) listedItem);
						break;
					default:
						throw new IllegalArgumentException(usage);
					}
					sender.sendMessage("Added '"+listedItem.toString()+"' to "+args[1]+" list");
					if (player!=null) {
						this.instance.getLogger().info(player.getName()+" added "+listedItem.toString()+"' to "+args[1]+" list");
					}
					return true;
				} catch (IllegalArgumentException e) {
					sender.sendMessage("Error: "+e.getMessage());
					return false;
				}
			case "remove":
				usage="Usage:\n/"+label+" remove (whitelist|ranged|aoe) ('hand'|itemid|itemname)[:(metadata)] [world]";
				if (args.length<3) {
					sender.sendMessage(usage);
					return false;
				}
				
				try {
					ListedItem listedItem=this.parseListedItemFromArgs(player, args);
					switch(args[1].toLowerCase()) {
					case "whitelist":
						if (!this.instance.config.removeWhitelistItem(listedItem.material, listedItem.data, listedItem.world)) {
							throw new IllegalArgumentException("Item not found");
						}
						break;
					case "ranged":
						if (!this.instance.config.removeRangedItem(listedItem.material, listedItem.data, listedItem.world)) {
							throw new IllegalArgumentException("Item not found");
						}
						break;
					case "aoe":
						if (!this.instance.config.removeAoEItem(listedItem.material, listedItem.data, listedItem.world)) {
							throw new IllegalArgumentException("Item not found");
						}
						break;
					default:
						throw new IllegalArgumentException("Wrong command");
					}
					sender.sendMessage("Item '"+listedItem.toString()+"' removed from "+args[1]+" list");
					if (player!=null) {
						this.instance.getLogger().info(player.getName()+" removed '"+listedItem.toString()+"' from "+args[1]+" list");
					}
					return true;
				} catch (IllegalArgumentException e) {
					sender.sendMessage(e.getMessage());
					return false;
				}
			case "list":
				usage="Usage:\n/"+label+" list (whitelist|ranged|aoe)";
				if (args.length<2) {
					sender.sendMessage(usage);
					return false;
				}
				
				try {
					switch(args[1].toLowerCase()) {
					case "whitelist":
						if (this.instance.config.whitelist.size()==0) {
							throw new IllegalArgumentException("Whitelist is empty");
						}
						sender.sendMessage("§2ForgeRestrictor Whitelist\n(ItemName:Data [World])");
						for (ListedItem listedItem : this.instance.config.whitelist) {
							sender.sendMessage(listedItem.toString());
						}
						return true;
					case "ranged":
						if (this.instance.config.ranged.size()==0) {
							throw new IllegalArgumentException("Ranged item list is empty");
						}
						sender.sendMessage("§2ForgeRestrictor Ranged item list\n(ItemName:Data (Range) [World])");
						for (ListedItem listedItem : this.instance.config.ranged) {
							sender.sendMessage(listedItem.toString());
						}
						return true;
					case "aoe":
						if (this.instance.config.aoe.size()==0) {
							throw new IllegalArgumentException("AoE item list is empty");
						}
						sender.sendMessage("§2ForgeRestrictor AoE item list\n(ItemName:Data (Range) [World])");
						for (ListedItem listedItem : this.instance.config.aoe) {
							sender.sendMessage(listedItem.toString());
						}
						return true;
					default:
						throw new IllegalArgumentException(usage);
					}
				} catch (IllegalArgumentException e) {
					sender.sendMessage(e.getMessage());
					return false;
				}
			case "enable":
				usage="Usage:\n/"+label+" enable ("+StringUtils.join(ProtectionPlugins.getNameList(), "|")+"|all)";
				if (args.length<2) {
					sender.sendMessage(usage);
					return false;
				}
				
				if (args[1].equalsIgnoreCase("all")) {
					for(ProtectionPlugins pp : ProtectionPlugins.values()) {
						pp.setEnabled(true);
						sender.sendMessage(pp.toString()+" integration is now temporary enabled");
						if (player!=null) {
							this.instance.getLogger().info(player.getName()+" temporary enabled integration for "+pp.toString());
						}
					}
				} else {
					ProtectionPlugins pp = ProtectionPlugins.valueOf(args[1]);
					if (pp==null) {
						sender.sendMessage(usage);
						return false;
					}
					sender.sendMessage(pp.toString()+" integration is temporary now enabled");
					if (player!=null) {
						this.instance.getLogger().info(player.getName()+" temporary enabled integration for "+pp.toString());
					}
					pp.setEnabled(true);
				}
				return true;
			case "disable":
				usage="Usage:\n/"+label+" disable ("+StringUtils.join(ProtectionPlugins.getNameList(), "|")+"|all)";
				if (args.length<2) {
					sender.sendMessage(usage);
					return false;
				}
				
				if (args[1].equalsIgnoreCase("all")) {
					for(ProtectionPlugins pp : ProtectionPlugins.values()) {
						pp.setEnabled(false);
						sender.sendMessage(pp.toString()+" integration is now temporary disabled");
						if (player!=null) {
							this.instance.getLogger().info(player.getName()+" temporary disabled integration for "+pp.toString());
						}
					}
				} else {
					ProtectionPlugins pp = ProtectionPlugins.valueOf(args[1]);
					if (pp==null) {
						sender.sendMessage(usage);
						return false;
					}
					sender.sendMessage(pp.toString()+" integration is now temporary disabled");
					if (player!=null) {
						this.instance.getLogger().info(player.getName()+" temporary disabled integration for "+pp.toString());
					}
					pp.setEnabled(false);
				}
				return true;
			case "reload":
				sender.sendMessage("Reloading ForgeRestrictor...");
				if (player!=null) {
					this.instance.getLogger().info(player.getName()+" reloaded ForgeRestrictor ");
				}
				this.instance.onEnable();
				sender.sendMessage("Forge Restrictor reloaded.");
				return true;
			}
		}
		sender.sendMessage("Wrong command.");
		return false;
	}
	
	
	@SuppressWarnings("deprecation")
	ListedItem parseListedItemFromArgs(Player player, String[] args) throws IllegalArgumentException {
		Material material=null;
		Short data=null;
		String world=null;
		int range=100;
		
		String[] arg2=args[2].split(":");
		
		if (arg2[0].equalsIgnoreCase("hand")) {
			if (player==null) {
				throw new IllegalArgumentException("Console can't use 'hand'");
			}
			
			ItemStack itemInHand=player.getItemInHand();
			if (itemInHand.getType()==Material.AIR) {
				throw new IllegalArgumentException("Your hand is empty");
			}
			material=itemInHand.getType();
			
			if (arg2.length>1 && !arg2[1].equals("*")) {
				data=itemInHand.getDurability();
			}
		} else {
			try {
				int id=Integer.valueOf(arg2[0]);
				material=Material.getMaterial(id);
			} catch (NumberFormatException e) {
				material=Material.matchMaterial(arg2[0]);
			}
			
			if (arg2.length>1 && !arg2[1].equals("*")) {
				try {
					data=Short.valueOf(arg2[1]);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid metadata");
				}
			}
		}
		
		if (material==null) {
			throw new IllegalArgumentException("Unknown item");
		}
		
		switch(args[1].toLowerCase()) {
		case "whitelist":
			if (args.length>3) {
				if (this.instance.getServer().getWorld(args[3])==null) {
					throw new IllegalArgumentException("Unknown world");
				}
				world=args[3];
			}
			
			return new ListedItem(material, data, world);
		case "ranged":
		case "aoe":
			if (args.length>3) {
				try {
					range=Integer.valueOf(args[3]);
					if (range>140) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid range (max 140 blocks)");
				}
				if (args.length>4) {
					if (this.instance.getServer().getWorld(args[4])==null) {
						throw new IllegalArgumentException("Unknown world");
					}
					world=args[4];
				}
			}
			
			return new ListedRangedItem(material, data, world, range);
		}
		throw new IllegalArgumentException("Wrong list");
	}
}
