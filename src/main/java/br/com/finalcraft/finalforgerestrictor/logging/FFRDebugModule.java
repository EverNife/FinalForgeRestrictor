package br.com.finalcraft.finalforgerestrictor.logging;

import br.com.finalcraft.evernifecore.logger.debug.IDebugModule;

public enum FFRDebugModule implements IDebugModule {
    ITEM_REGISTRATION("Log each item Registration!", true),
    WORLD_GUARD("Debug related to WorldGuard!", true),
    ;

    private final String comment;
    private final boolean enabledByDefault;
    private boolean enabled = true;

    FFRDebugModule(String comment) {
        this.comment = comment;
        this.enabledByDefault = false;
    }

    FFRDebugModule(String comment, boolean enabledByDefault) {
        this.comment = comment;
        this.enabledByDefault = enabledByDefault;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getComment() {
        return comment;
    }
}
