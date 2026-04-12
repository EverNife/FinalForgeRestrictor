package br.com.finalcraft.finalforgerestrictor.integration.worldguard;

import br.com.finalcraft.evernifecore.protection.worldguard.WGPlatform;
import br.com.finalcraft.finalforgerestrictor.FinalForgeRestrictor;
import br.com.finalcraft.finalforgerestrictor.integration.worldguard.flags.ForgeResIgnoreFlag;

public class WorldGuardIntegration {

    public static void initialize(){
        WGPlatform.getInstance().registerFlag(ForgeResIgnoreFlag.instance, FinalForgeRestrictor.instance);
    }

}
