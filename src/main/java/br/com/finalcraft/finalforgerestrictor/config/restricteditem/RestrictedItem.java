package br.com.finalcraft.finalforgerestrictor.config.restricteditem;

import br.com.finalcraft.evernifecore.util.FCInputReader;
import br.com.finalcraft.evernifecore.util.FCItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class RestrictedItem {

	private final ItemStack itemStack;
	private final Material material;
	private final Short damageValue;
	private final int range;
	private final RestrictionType type;

	public RestrictedItem(ItemStack itemStack, Material material, Short damageValue, int range, RestrictionType type) {
		this.itemStack = itemStack;
		this.material = material;
		this.damageValue = damageValue;
		this.range = range;
		this.type = type;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public Material getMaterial() {
		return material;
	}

	public Short getDamageValue() {
		return damageValue;
	}

	public int getRange() {
		return range;
	}

	public RestrictionType getType() {
		return type;
	}

	public boolean match(ItemStack itemStack) {
		return itemStack.getType() == this.material && (this.damageValue == null || this.damageValue == itemStack.getDurability());
	}

	public String serialize(){
		String range = getType() == RestrictionType.WHITELIST ? "" : this.range + " | ";
		return range + material + (damageValue != null ? ":" + damageValue : "");
	}

	public static RestrictedItem deserialize(String serializedLine, RestrictionType type) {
		String[] split = serializedLine.split(Pattern.quote("|"));

		Integer range 			= split.length == 1 ? 0 : FCInputReader.parseInt(split[0].trim());
		String itemSerialized 	= split.length == 1 ? split[0].trim() : split[1].trim();

		final ItemStack itemStack;
		final Short damageValue;
		if (itemSerialized.contains(":-1") || itemSerialized.contains(":*") || !itemSerialized.contains(":")){
			itemStack = FCItemUtils.fromBukkitIdentifier(itemSerialized.split(Pattern.quote(":"))[0]);
			damageValue = null;
		}else {
			itemStack = FCItemUtils.fromBukkitIdentifier(itemSerialized);
			damageValue = itemStack.getDurability();
		}

		return new RestrictedItem(
				itemStack,
				itemStack.getType(),
				damageValue,
				range,
				type
		);
	}

}
