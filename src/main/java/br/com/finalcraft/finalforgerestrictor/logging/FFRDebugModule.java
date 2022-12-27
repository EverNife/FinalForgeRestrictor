package br.com.finalcraft.finalforgerestrictor.logging;

import br.com.finalcraft.evernifecore.logger.debug.IDebugModule;

public enum FFRDebugModule implements IDebugModule {
    TEST,
    ;

    private boolean enabled = true;

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

}
