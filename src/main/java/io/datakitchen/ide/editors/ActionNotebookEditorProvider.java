package io.datakitchen.ide.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.NodeType;

public class ActionNotebookEditorProvider extends BaseNodeEditorProvider {

    @Override
    protected String getNodeTypeName() {
        return NodeType.ACTION_NODE_TYPE_NAME;
    }

    @Override
    protected FileEditor createRegularEditor(Project project, VirtualFile file) {
        return new ActionEditor(project, file);
    }

    @Override
    protected FileEditor createSimplifiedEditor(Project project, VirtualFile file) {
        Module module = ModuleUtil.findModuleForFile(file, project);
        return new io.datakitchen.ide.editors.neweditors.ActionEditor(module, file);
    }
}
