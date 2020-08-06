package br.com.finalcraft.forgerestrictor.config;

import br.com.finalcraft.forgerestrictor.ForgeRestrictor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfiscatedInventory {
	private Player player;
	private ItemStack[] is;
	private boolean isReleased;

	public ConfiscatedInventory(Player player) {
		this.player = player;
		this.is = player.getInventory().getContents().clone();
		player.getInventory().clear();
		player.setItemInHand(new ItemStack(Material.AIR));
	}

	public void release() {
		if (!this.isReleased) {
			if (this.player.isOnline()) {
				this.player.getInventory().setContents(this.is);
				this.isReleased=true;
			} else {
				ForgeRestrictor.getInstance().getLogger().warning("Couldn't restore "+this.player.getName()+"'s confiscated inventory, content: "+this.inventoryContent());
			}
		}
	}

	public Player getPlayer() {
		return player;
	}
	
	boolean isPlayerInventoryEmpty() {
		for(ItemStack is : this.player.getInventory()) {
			if (is!=null && is.getType()!=Material.AIR) {
				return false;
			}
		}
		return true;
	}
	
	private String inventoryContent() {
		StringBuilder sb=new StringBuilder();
		for (ItemStack is : this.is) {
			if (is!=null) {
				sb.append(is.getType().toString()+"("+is.getAmount()+") ");
			}
		}
		return sb.toString();
	}
}
