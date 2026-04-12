package br.com.finalcraft.finalforgerestrictor.confiscation;

import br.com.finalcraft.evernifecore.util.FCBukkitUtil;
import br.com.finalcraft.finalforgerestrictor.FinalForgeRestrictor;
import br.com.finalcraft.finalforgerestrictor.config.data.ConfiscatedInventory;
import br.com.finalcraft.finalforgerestrictor.config.settings.FFResSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class ConfiscationManager {

    private static HashMap<UUID, ConfiscatedInventory> confiscated = new HashMap<>();

    public static void confiscateInventory(Player player) {
        confiscateInventory(player, FFResSettings.confiscateTicks);
    }

    public static void confiscateInventory(Player player, int ticks) {
        if (ticks < 1) {
            return;
        }

        if (FCBukkitUtil.isFakePlayer(player)){
            return;
        }

        if (isPlayerInventoryEmpty(player)){
            return;
        }

        if (confiscated.containsKey(player.getUniqueId())){
            return; //This player is already confiscated
        }

        confiscated.put(player.getUniqueId(), new ConfiscatedInventory(player));

        Bukkit.getScheduler().runTaskLater(FinalForgeRestrictor.instance, () -> {
            restoreInventoryIfNeeded(player);
        }, ticks);
    }

    public static void restoreInventoryIfNeeded(Player player){
        ConfiscatedInventory confiscatedInventory = confiscated.remove(player.getUniqueId());
        if (confiscatedInventory != null){//Can be null, maybe the player died or quit before the confiscation ended
            confiscatedInventory.restoreInventory();
        }
    }

    private static boolean isPlayerInventoryEmpty(Player player) {
        for(ItemStack itemStack : player.getInventory()) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

}
