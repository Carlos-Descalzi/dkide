package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.dialogs.NewDataSinkDialog;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

public class NewDataSinkAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile folder = e.getData(LangDataKeys.VIRTUAL_FILE);
        if (project != null && folder != null) {
            Module module = ModuleUtil.findModuleForFile(folder, project);
            NewDataSinkDialog dialog = new NewDataSinkDialog(module);
            if (dialog.showAndGet()) {
                dialog.writeToFolder(module, folder, true, (vFile) -> {
                    ApplicationManager.getApplication().invokeLater(()->
                            FileEditorManager.getInstance(project).openFile(vFile, true)
                    );
                });
            }
        }
    }
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        VirtualFile currentFile = e.getData(LangDataKeys.VIRTUAL_FILE);
        Module module = e.getData(LangDataKeys.MODULE);

        e.getPresentation().setEnabled(valid(module, currentFile));
    }

    private boolean valid(Module module, VirtualFile folder){
        if (module != null && folder != null) {
            try {
                if (RecipeUtil.isNodeFolder(module, folder)) {
                    String dataSourceFolderName = RecipeUtil.getDataSinksFolderNameForNode(folder);

                    return dataSourceFolderName != null && folder.findChild(dataSourceFolderName) == null;
                } else {
                    return RecipeUtil.isDataSinkFolder(module, folder);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
