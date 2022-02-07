package io.datakitchen.ide.hooks;

import io.datakitchen.ide.tools.LogTarget;

public abstract class Action {

    private LogTarget logTarget;

    public void setLogTarget(LogTarget logTarget) {
        this.logTarget = logTarget;
    }

    public void println(String message){
        logTarget.println(message);
    }

    public abstract String getName();
    public abstract String getDescription();
    public abstract boolean enabled(String recipe, String selectedFile);
    public abstract void run(String recipe, String selectedFile);
}
