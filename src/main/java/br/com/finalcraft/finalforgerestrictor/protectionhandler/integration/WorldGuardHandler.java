package br.com.finalcraft.finalforgerestrictor.protectionhandler.integration;

import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import br.com.finalcraft.evernifecore.minecraft.vector.BlockPos;
import br.com.finalcraft.evernifecore.protection.worldguard.FCWorldGuardRegion;
import br.com.finalcraft.evernifecore.protection.worldguard.WGFlags;
import br.com.finalcraft.evernifecore.protection.worldguard.WGPlatform;
import br.com.finalcraft.evernifecore.protection.worldguard.adapter.FCRegionResultSet;
import br.com.finalcraft.finalforgerestrictor.integration.worldguard.flags.ForgeResIgnoreFlag;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionHandler;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class WorldGuardHandler implements ProtectionHandler {

	@FCLocale(lang = LocaleType.EN_US, text = "§e§l ▶ §cVocê não tem permissão nessa Região!")
	@FCLocale(lang = LocaleType.PT_BR, text = "§e§l ▶ §cYou do not have permission on this Region!")
	private static LocaleMessage YOU_DO_NOT_HAVE_PERMISSION_ON_THIS_AREA;

	@FCLocale(lang = LocaleType.EN_US, text = "§e§l ▶ §cVocê está muito perto de uma Região Protegida para fazer isso!")
	@FCLocale(lang = LocaleType.PT_BR, text = "§e§l ▶ §cYou are to close to a protected region to do that!!")
	private static LocaleMessage YOU_ARE_TOO_CLOSE_TO_A_REGION;

	@Override
	public boolean canBuild(Player player, Location location) {
		return this.check(player, location, WGFlags.BUILD);
	}

	@Override
	public boolean canAccess(Player player, Location location) {
		return this.check(player, location, WGFlags.USE);
	}

	@Override
	public boolean canUse(Player player, Location location) {
		return this.check(player, location, WGFlags.BUILD);
	}

	@Override
	public boolean canOpenContainer(Player player, Block block) {
		return this.check(player, block.getLocation(), WGFlags.CHEST_ACCESS);
	}

	@Override
	public boolean canInteract(Player player, Location location) {
		return this.check(player, location, WGFlags.INTERACT);
	}

	@Override
	public boolean canAttack(Player player, Entity entity) {
		if (entity instanceof Player) {
			return this.check(player, entity.getLocation(), WGFlags.PVP);
		}
		if (entity instanceof Animals || entity instanceof Villager) {
			return this.check(player, entity.getLocation(), WGFlags.DAMAGE_ANIMALS);
		}
		
		return true;
	}

	@Override
	public boolean canProjectileHit(Player player, Location location) {
		return this.check(player, location, WGFlags.BUILD);
	}
	
	@Override
	public boolean canUseAoE(Player player, Location location, int range) {
		FCRegionResultSet regions = this.getRegions(location, range);
		LocalPlayer localPlayer = WGPlatform.getInstance().wrapPlayer(player);

		if (regions.queryState(localPlayer, ForgeResIgnoreFlag.instance) == State.ALLOW){
			//Check first if this regionSet has a flag to ignore the FinalForgeRestrictor
			return true;
		}

		if (regions.queryState(localPlayer, WGFlags.BUILD) != State.ALLOW){
			YOU_ARE_TOO_CLOSE_TO_A_REGION.send(player);
			return false;
		}

		return true;
	}
	
	protected boolean check(Player player, Location location, StateFlag flag) {
		FCRegionResultSet regions = this.getRegions(location);
		LocalPlayer localPlayer = WGPlatform.getInstance().wrapPlayer(player);

		if (regions.queryState(localPlayer, ForgeResIgnoreFlag.instance) == State.ALLOW){
			//Check first if this regionSet has a flag to ignore the FinalForgeRestrictor
			return true;
		}

		if (regions.queryState(localPlayer, flag) != State.ALLOW){
			YOU_DO_NOT_HAVE_PERMISSION_ON_THIS_AREA.send(player);
			return false;
		}

		return true;
	}
	
	protected FCRegionResultSet getRegions(Location location) {
		return WGPlatform.getInstance().getRegionManager(location.getWorld()).getApplicableRegions(location);
	}
	
	protected FCRegionResultSet getRegions(Location location, int range) {
		FCWorldGuardRegion region = FCWorldGuardRegion.of("ForgeRestrictorWGAoETest",
				BlockPos.from(location).add(-range, location.getBlockY(), -range),
				BlockPos.from(location).add(range, 255 - location.getBlockY(), range)
		);
		return WGPlatform.getInstance().getRegionManager(location.getWorld()).getApplicableRegions(region);
	}
	
	@Override
	public String getName() {
		return "WorldGuard";
	}
}
