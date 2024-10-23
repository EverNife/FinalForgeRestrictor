package br.com.finalcraft.finalforgerestrictor.config.settings;

import br.com.finalcraft.evernifecore.config.yaml.section.ConfigSection;
import br.com.finalcraft.finalforgerestrictor.FinalForgeRestrictor;
import br.com.finalcraft.finalforgerestrictor.config.ConfigManager;
import br.com.finalcraft.finalforgerestrictor.config.restricteditem.RestrictedItem;
import br.com.finalcraft.finalforgerestrictor.config.restricteditem.RestrictionType;
import br.com.finalcraft.finalforgerestrictor.logging.FFRDebugModule;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class FFResSettings {

	public static Multimap<Material, RestrictedItem> RESTRICTED_ITEMS = HashMultimap.create();
	private static HashSet<String> ENABLED_WORLDS = new HashSet<>();

	public static int confiscateTicks;
	public static boolean ignoreFakePlayers = true;
	public static boolean ignoreVanillaToVanillaInteractions = true;
	private static boolean allWorlds = true;

	public static void initialize() {
		RESTRICTED_ITEMS.clear();

		confiscateTicks = ConfigManager.getMainConfig().getOrSetDefaultValue(
				"Settings.confiscateTicks",
				3,
				"How much time will the player's inventory be confiscated!"
		);

		ignoreFakePlayers = ConfigManager.getMainConfig().getOrSetDefaultValue(
				"Settings.ignoreFakePlayers",
				true,
				"Should FakePlayers be ignored on the cancel-event system?"
		);

		ignoreVanillaToVanillaInteractions = ConfigManager.getMainConfig().getOrSetDefaultValue(
				"Settings.ignoreVanillaToVanillaInteractions",
				true,
				"Should Vanilla to Vanilla interactions be ignored on the cancel-event system?" +
						"\nUsually normal protections plugins already handle these cases so we disable" +
						"\nthese checks to improve performance!" +
						"\n" +
						"\nBut in some cases, some servers-core might behave differently and you might" +
						"\nwant to enable this extra protection!"
		);

		ENABLED_WORLDS = new HashSet<>(
				ConfigManager.getMainConfig().getOrSetDefaultValue(
						"Settings.worlds",
						Arrays.asList("*"),
						"Worlds this plugin will protected! The '*' means all Worlds!"
				)
		);
		allWorlds = ENABLED_WORLDS.contains("*");


		loadRestrictedItems(
				RestrictionType.WHITELIST,
				"Items that this plugin will ignore!" +
						"\nIf the player is holding an item from this list, this plugin will not confiscate his inventory!"
		);

		loadRestrictedItems(
				RestrictionType.RANGED,
				"Items that cause damage from a long range!" +
						"\nItems like Explosive Crossbrows, explosive wands, etc!"
		);

		loadRestrictedItems(
				RestrictionType.AOE,
				"Items that cause damage around the player!" +
						"\nItems like Hammers, twilight Horns, etc!"
		);

//		loadRestrictedItems(
//				RestrictionType.BLOCK_INTERACT,
//				"Blocks that the player should not be able to interact!" +
//						"\nNot even with their bare hands! Like some machines or applied systems!" +
//						"\nThis will also prevent the player from interacting with the block's inventory!"
//		);

		for (RestrictionType type : RestrictionType.values()) {
			long count = RESTRICTED_ITEMS.values().stream().filter(restrictedItem -> restrictedItem.getType() == type).count();
			FinalForgeRestrictor.getLog().info(String.format("[%s] Loaded %s restricted item(s)!", type.getKey(), count));
		}

		ConfigManager.getMainConfig().saveIfNewDefaults();
	}

	private static void loadRestrictedItems(RestrictionType type, String comment){
		int line = 0;
		ConfigSection section = ConfigManager.getMainConfig().getConfigSection("RestrictedItems." + type.getKey());
		for (String serializedItemWithRange : section.getOrSetDefaultValue("", new ArrayList<String>(), comment)) {
			line++;
			try {
				RestrictedItem newRestrictedItem = RestrictedItem.deserialize(serializedItemWithRange, type);

				//Check if this was already not registered on the other list
				RestrictedItem existing = getRestrictedItem(newRestrictedItem.getItemStack());
				if (existing != null){
					FinalForgeRestrictor.getLog().warning("There Item [" + serializedItemWithRange + "] is already restricted under the list: " + existing.getType());
					FinalForgeRestrictor.getLog().warning("Ignoring the new one from the list <" + type + "> at line: " + line);
					continue;
				}

				FinalForgeRestrictor.getLog().debugModule(FFRDebugModule.ITEM_REGISTRATION, "(%s) Adding restricted item: [%s]", type.getKey(), newRestrictedItem.serialize());
				RESTRICTED_ITEMS.put(newRestrictedItem.getMaterial(), newRestrictedItem);
			}catch (Exception e){
				FinalForgeRestrictor.getLog().warning("Failed to load RestrictedItem from: " + section);
				FinalForgeRestrictor.getLog().warning("Line [" + line + "]  -->   " + serializedItemWithRange);
				e.printStackTrace();
			}
		}
	}

	public static boolean isWorldEnabled(World world){
		if (allWorlds) return true;
		return ENABLED_WORLDS.contains(world.getName());
	}

	public static void removeRestrictedItem(ItemStack itemStack){
		RESTRICTED_ITEMS.get(itemStack.getType())
				.removeIf(restrictedItem -> restrictedItem.match(itemStack));
	}

	public static @Nullable RestrictedItem getRestrictedItem(ItemStack itemStack){
		for (RestrictedItem restrictedItem : RESTRICTED_ITEMS.get(itemStack.getType())) {
			if (restrictedItem.match(itemStack)){
				return restrictedItem;
			}
		}
		return null;
	}

	public static @Nullable RestrictedItem getRestrictedItem(Block block){
		for (RestrictedItem restrictedItem : RESTRICTED_ITEMS.get(block.getType())) {
			if (restrictedItem.match(block)){
				return restrictedItem;
			}
		}
		return null;
	}

	public static void addRestrictedItem(RestrictedItem restrictedItem){
		RESTRICTED_ITEMS.put(restrictedItem.getMaterial(), restrictedItem);

		ConfigManager.getMainConfig().setValue("RestrictedItems." + restrictedItem.getType().getKey(),
				RESTRICTED_ITEMS.values().stream().map(RestrictedItem::serialize).collect(Collectors.toList())
		);

		ConfigManager.getMainConfig().saveAsync();
	}
}