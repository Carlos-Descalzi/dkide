package io.datakitchen.ide.editors.neweditors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.AbstractFileEditor;
import io.datakitchen.ide.run.NodeRunner;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;

public abstract class BaseSimpleEditor extends AbstractFileEditor implements DataProvider {

    private static final Logger LOGGER = Logger.getInstance(BaseSimpleEditor.class);

    protected final Module module;
    protected final VirtualFile notebookFile;
    private final JPanel mainPanel;

    public BaseSimpleEditor(Module module, VirtualFile notebookFile){
        this.module = module;
        this.notebookFile = notebookFile;

        mainPanel = new CustomPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new GridLayout(1,2));

        JPanel topLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeftPanel.add(new JButton(new SimpleAction(AllIcons.Actions.Execute,"Run Node","Run Node", this::runNode)));
        topPanel.add(topLeftPanel);

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRightPanel.add(new JButton(new SimpleAction("Switch to advanced mode", this::switchToAdvancedMode)));
        topPanel.add(topRightPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        JComponent view = buildView();
        mainPanel.add(view, BorderLayout.CENTER);
    }

    private void runNode(ActionEvent event) {
        ApplicationManager.getApplication().invokeLater(()->{
            new NodeRunner(module, notebookFile.getParent())
                .run();
        });
    }

    private class CustomPanel extends JPanel implements DataProvider {
        public CustomPanel(BorderLayout borderLayout) {
            super(borderLayout);
        }

        public @Nullable Object getData(@NotNull @NonNls String dataId) {
            return BaseSimpleEditor.this.getData(dataId);
        }
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (dataId.equals(LangDataKeys.PROJECT.getName())){
            return module.getProject();
        } else if (dataId.equals(LangDataKeys.MODULE.getName())){
            return module;
        } else if (dataId.equals(LangDataKeys.VIRTUAL_FILE.getName())){
            return notebookFile.getParent();
        }
        return null;
    }

    protected abstract JComponent buildView();

    private void switchToAdvancedMode(ActionEvent event) {
        try {
            VirtualFile descriptionFile = notebookFile.getParent().findChild(Constants.FILE_DESCRIPTION_JSON);

            Map<String, Object> description = JsonUtil.read(descriptionFile);

            Map<String, Object> options = (Map<String, Object>)description.get("options");

            if (options != null){
                options.put("simplified-view", false);
            }

            ApplicationManager.getApplication().runWriteAction(()->{
                try {
                    JsonUtil.write(description, descriptionFile);
                }catch (Exception ex){
                    LOGGER.error(ex);
                }
            });

        }catch(Exception ex){
            LOGGER.error(ex);
        }

        FileEditorManager.getInstance(module.getProject())
                .closeFile(notebookFile);
        FileEditorManager.getInstance(module.getProject())
                .openFile(notebookFile,true);
    }

    @Override
    public final @Nullable VirtualFile getFile() {
        return notebookFile;
    }

    @Override
    public final @NotNull JComponent getComponent() {
        return this.mainPanel;
    }

    @Override
    public final @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

}
