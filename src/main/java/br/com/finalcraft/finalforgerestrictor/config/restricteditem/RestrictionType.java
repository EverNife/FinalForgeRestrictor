package br.com.finalcraft.finalforgerestrictor.config.restricteditem;

public enum RestrictionType {
    WHITELIST("Whitelist", 0),
    RANGED("Ranged", 30),
    AOE("AoE", 10),
//    BLOCK_INTERACT("BlockInteract", 0),
    ;

    private final String key;
    private final int defaultRange;

    RestrictionType(String key, int defaultRange) {
        this.key = key;
        this.defaultRange = defaultRange;
    }

    public String getKey() {
        return key;
    }

    public int getDefaultRange() {
        return defaultRange;
    }
}
