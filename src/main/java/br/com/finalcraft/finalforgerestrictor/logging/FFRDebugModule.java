package br.com.finalcraft.finalforgerestrictor.logging;

import br.com.finalcraft.evernifecore.logger.debug.IDebugModule;

public enum FFRDebugModule implements IDebugModule {
    ITEM_REGISTRATION("Log each item Registration!"),
    ;

    private String comment;
    private boolean enabled = true;

    FFRDebugModule(String comment) {
        this.comment = comment;
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
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getComment() {
        return comment;
    }
}
