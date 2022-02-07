package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.dialogs.ChangeNodeTypeDialog;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import net.minidev.json.parser.ParseException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChangeNodeTypeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        ChangeNodeTypeDialog dialog = new ChangeNodeTypeDialog();
        if (dialog.showAndGet()){
            Project project = e.getProject();
            String nodeTypeName = dialog.getNodeType();
            VirtualFile nodeFolder = e.getData(LangDataKeys.VIRTUAL_FILE);
            changeNodeType(project, nodeFolder, NodeType.getByTypeName(nodeTypeName));
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        VirtualFile nodeFolder = e.getData(LangDataKeys.VIRTUAL_FILE);
        Module module = e.getData(LangDataKeys.MODULE);
        e.getPresentation().setEnabled(
                nodeFolder != null
                && module != null
                && RecipeUtil.isNodeFolder(module, nodeFolder));
    }

    private void changeNodeType(Project project, VirtualFile nodeFolder, NodeType newNodeType) {
        try {
            NodeType actualNodeType = getActualNodeType(nodeFolder);

            ApplicationManager.getApplication().invokeLater(() -> {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        rearrangeFolders(nodeFolder, actualNodeType, newNodeType);
                        changeDescriptionJson(nodeFolder, newNodeType);
                        updateNotebook(nodeFolder, newNodeType);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Messages.showMessageDialog(project, String.valueOf(ex.getMessage()), "Error", Messages.getErrorIcon());
                    }
                });
            });
        }catch (Exception ex){
            ex.printStackTrace();
            Messages.showMessageDialog(project, String.valueOf(ex.getMessage()), "Error", Messages.getErrorIcon());
        }
    }

    private void changeDescriptionJson(VirtualFile nodeFolder, NodeType newNodeType) throws IOException, ParseException {
        VirtualFile descriptionFile = nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);
        Map<String, Object> description = JsonUtil.read(descriptionFile);
        description.put("type", newNodeType.getName());
        JsonUtil.write(description, descriptionFile);
    }

    private void updateNotebook(VirtualFile nodeFolder, NodeType newNodeType) throws IOException{
        try {
            VirtualFile notebookFile = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);
            if (notebookFile != null) {
                Map<String, Object> notebook = JsonUtil.read(notebookFile);

                Map<String, Object> tests = (Map<String, Object>) notebook.getOrDefault("tests", new LinkedHashMap<>());
                Map<String, Object> newNotebook = new LinkedHashMap<>();
                newNotebook.put("tests", tests);
                JsonUtil.write(newNotebook, notebookFile);
            }
        }catch (ParseException ex){
            // unable to parse, it means the file contains jinja stuff.
        }
    }

    private void rearrangeFolders(VirtualFile nodeFolder, NodeType actualNodeType, NodeType newNodeType) throws IOException {

        if (actualNodeType.getDataSourcesFolder() != null){
            if (newNodeType.getDataSourcesFolder() != null){
                if (!actualNodeType.getDataSourcesFolder().equals(newNodeType.getDataSourcesFolder())){
                    // rename or create if it doesn't exist.
                    VirtualFile dataSourcesFolder = nodeFolder.findChild(actualNodeType.getDataSourcesFolder());
                    if (dataSourcesFolder != null){
                        dataSourcesFolder.rename(this, newNodeType.getDataSourcesFolder());
                    } else {
                        nodeFolder.createChildDirectory(this, newNodeType.getDataSourcesFolder());
                    }
                }
            } else {
                // remove
                VirtualFile dataSourcesFolder = nodeFolder.findChild(actualNodeType.getDataSourcesFolder());
                if (dataSourcesFolder != null){
                    dataSourcesFolder.delete(this);
                }
            }
        } else {
            if (newNodeType.getDataSourcesFolder() != null){
                // create folder
                nodeFolder.createChildDirectory(this, newNodeType.getDataSourcesFolder());
            }
        }
        if (actualNodeType.getDataSinksFolder() != null){
            if (newNodeType.getDataSinksFolder() != null){
                // pass
            } else {
                // remove
                VirtualFile dataSinksFolder = nodeFolder.findChild(actualNodeType.getDataSourcesFolder());
                if (dataSinksFolder != null){
                    dataSinksFolder.delete(this);
                }
            }
        } else {
            if (newNodeType.getDataSinksFolder() != null){
                // create
                nodeFolder.createChildDirectory(this, newNodeType.getDataSinksFolder());
            }
        }

        for (String additionalFolder: newNodeType.getAdditionalFolders()){
            nodeFolder.createChildDirectory(this, additionalFolder);
        }
    }

    private NodeType getActualNodeType(VirtualFile nodeFolder) throws IOException, ParseException{
        VirtualFile descriptionFile = nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);
        Map<String, Object> description = JsonUtil.read(descriptionFile);
        String typeName = (String)description.get("type");
        return NodeType.getByTypeName(typeName);
    }

}
