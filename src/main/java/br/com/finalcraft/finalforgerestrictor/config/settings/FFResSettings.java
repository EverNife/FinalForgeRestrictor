package br.com.finalcraft.finalforgerestrictor.config.settings;

import br.com.finalcraft.evernifecore.config.yaml.section.ConfigSection;
import br.com.finalcraft.finalforgerestrictor.FinalForgeRestrictor;
import br.com.finalcraft.finalforgerestrictor.config.ConfigManager;
import br.com.finalcraft.finalforgerestrictor.config.restricteditem.RestrictedItem;
import br.com.finalcraft.finalforgerestrictor.config.restricteditem.RestrictionType;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionPlugins;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class FFResSettings {

	public static Multimap<Material, RestrictedItem> RESTRICTED_ITEMS = HashMultimap.create();
	private static HashSet<String> ENABLED_WORLDS = new HashSet<>();

	private static boolean allWorlds = true;
	public static int confiscateTicks;

	public static void initialize() {
		RESTRICTED_ITEMS.clear();

		confiscateTicks = ConfigManager.getMainConfig().getOrSetDefaultValue("Settings.confiscateTicks", 3, "How much time will the player's inventory be confiscated!");

		ENABLED_WORLDS = new HashSet<>(
				ConfigManager.getMainConfig().getOrSetDefaultValue("Settings.worlds", Arrays.asList("*"), "Worlds this plugin will protected! The '*' means all Worlds!")
		);
		allWorlds = ENABLED_WORLDS.contains("*");


		loadRestrictedItems(
				RestrictionType.WHITELIST,
				"Items that this plugin will ignore! " +
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

		for (RestrictionType type : RestrictionType.values()) {
			long count = RESTRICTED_ITEMS.values().stream().filter(restrictedItem -> restrictedItem.getType() == type).count();
			FinalForgeRestrictor.info(String.format("[%s] Loaded %s restricted item(s)!", type.getKey(), count));
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
					FinalForgeRestrictor.warning("There Item [" + serializedItemWithRange + "] is already restricted under the list: " + existing.getType());
					FinalForgeRestrictor.warning("Ignoring the new one from the list <" + type + "> at line: " + line);
					continue;
				}

				RESTRICTED_ITEMS.put(newRestrictedItem.getMaterial(), newRestrictedItem);
			}catch (Exception e){
				FinalForgeRestrictor.warning("Failed to load RestrictedItem from: " + section);
				FinalForgeRestrictor.warning("Line [" + line + "]  -->   " + serializedItemWithRange);
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

	public static void addRestrictedItem(RestrictedItem restrictedItem){
		RESTRICTED_ITEMS.put(restrictedItem.getMaterial(), restrictedItem);

		ConfigManager.getMainConfig().setValue("RestrictedItems." + restrictedItem.getType().getKey(),
				RESTRICTED_ITEMS.values().stream().map(RestrictedItem::serialize).collect(Collectors.toList())
		);

		ConfigManager.getMainConfig().saveAsync();
	}
}