package io.datakitchen.ide.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.DataMapperEditor;
import io.datakitchen.ide.model.NodeType;

public class DataMapperNotebookEditorProvider extends BaseNodeEditorProvider {

    @Override
    protected String getNodeTypeName() {
        return NodeType.DATA_MAPPER_NODE_TYPE_NAME;
    }

    @Override
    protected FileEditor createRegularEditor(Project project, VirtualFile file) {
        return new DataMapperNotebookEditor(project, file);
    }

    @Override
    protected FileEditor createSimplifiedEditor(Project project, VirtualFile file) {
        Module module = ModuleUtil.findModuleForFile(file, project);
        return new DataMapperEditor(module, file);
    }
}
