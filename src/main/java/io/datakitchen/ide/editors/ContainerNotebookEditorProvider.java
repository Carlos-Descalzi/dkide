package io.datakitchen.ide.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.ContainerEditor;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.util.RecipeUtil;

import java.util.Map;

public class ContainerNotebookEditorProvider extends BaseNodeEditorProvider {

    @Override
    protected String getNodeTypeName() {
        return NodeType.CONTAINER_NODE_TYPE_NAME;
    }

    protected boolean acceptNode(VirtualFile notebookFile, Map<String, Object> description){
        return super.acceptNode(notebookFile, description)
                && !RecipeUtil.isScriptNode(notebookFile.getParent());
    }

    @Override
    protected FileEditor createRegularEditor(Project project, VirtualFile file) {
        return new ContainerNotebookEditor(project, file);
    }

    @Override
    protected FileEditor createSimplifiedEditor(Project project, VirtualFile file) {
        Module module = ModuleUtil.findModuleForFile(file, project);
        return new ContainerEditor(module, file);
    }

}
