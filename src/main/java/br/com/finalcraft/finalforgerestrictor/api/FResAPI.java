package br.com.finalcraft.finalforgerestrictor.api;

import br.com.finalcraft.finalforgerestrictor.confiscation.ConfiscationManager;
import org.bukkit.entity.Player;

public class FResAPI {

    public static void confiscateInventory(Player player) {
        ConfiscationManager.confiscateInventory(player);
    }

    public static void confiscateInventory(Player player, int ticks) {
        ConfiscationManager.confiscateInventory(player, ticks);
    }

}
