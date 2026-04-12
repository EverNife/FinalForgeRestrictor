package br.com.finalcraft.finalforgerestrictor.integration.worldguard.flags;

import com.sk89q.worldguard.protection.flags.StateFlag;

public class ForgeResIgnoreFlag extends StateFlag {

    public static final ForgeResIgnoreFlag instance = new ForgeResIgnoreFlag("fres-ignore", false);

    public ForgeResIgnoreFlag(String name, boolean def) {
        super(name, def);
    }

}
