package br.com.finalcraft.forgerestrictor.api;

import br.com.finalcraft.forgerestrictor.ForgeRestrictor;
import org.bukkit.entity.Player;

public class FResAPI {

    public static void confiscateInventory(Player player) {
        ForgeRestrictor.getInstance().eventListener.confiscateInventory(player);
    }

    public static void confiscateInventory(Player player, int ticks) {
        ForgeRestrictor.getInstance().eventListener.confiscateInventory(player, ticks);
    }

}
