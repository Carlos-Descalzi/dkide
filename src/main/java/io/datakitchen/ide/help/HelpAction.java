package io.datakitchen.ide.help;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class HelpAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(HelpAction.class);

    @Override
    public final void actionPerformed(@NotNull AnActionEvent e) {
        String launcher = getLauncherCommand();
        try {
            new ProcessBuilder(launcher, getUrl()).start();
        }catch(Exception ex){
            LOGGER.error(ex);
        }
    }

    @NotNull
    private String getLauncherCommand() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("windows")){
            return "start";
        }

        if (osName.contains("mac")){
            return "open";
        }

        return "xdg-open";
    }

    protected abstract String getUrl();
}
