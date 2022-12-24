package br.com.finalcraft.finalforgerestrictor.config.data;

import br.com.finalcraft.evernifecore.util.FCItemUtils;
import br.com.finalcraft.finalforgerestrictor.FinalForgeRestrictor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfiscatedInventory {

	private Player player;
	private ItemStack[] confiscatedItems;
	private boolean hasBeenRestored;

	public ConfiscatedInventory(Player player) {
		this.player = player;
		this.confiscatedItems = player.getInventory().getContents().clone();
		player.getInventory().clear();
		player.setItemInHand(new ItemStack(Material.AIR));
	}

	public void restoreInventory() {
		if (!this.hasBeenRestored) {
			if (this.player.isOnline()) {
				this.player.getInventory().setContents(this.confiscatedItems);
				this.hasBeenRestored = true;
			} else {
				FinalForgeRestrictor.warning("Couldn't restore " + this.player.getName() + "'s confiscated inventory, content: " + this.inventoryContent());
			}
		}
	}

	private String inventoryContent() {
		StringBuilder stringBuilder = new StringBuilder();
		for (ItemStack itemStack : this.confiscatedItems) {
			if (itemStack != null && itemStack.getType() != Material.AIR) {
				stringBuilder.append("\n  - " + FCItemUtils.getMinecraftIdentifier(itemStack));
			}
		}
		return stringBuilder.toString();
	}

}
