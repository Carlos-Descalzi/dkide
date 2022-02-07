package io.datakitchen.ide.editors.script;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.BaseNodeEditorProvider;
import io.datakitchen.ide.editors.neweditors.ScriptNodeEditor;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ScriptNotebookEditorProvider extends BaseNodeEditorProvider {

    @Override
    protected String getNodeTypeName() {
        return "DKNode_Container";
    }

    protected boolean acceptNode(VirtualFile notebookFile, Map<String, Object> description){
        return super.acceptNode(notebookFile, description)
                && RecipeUtil.isScriptNode(notebookFile.getParent());
    }

    @Override
    protected FileEditor createRegularEditor(Project project, VirtualFile file) {
        return new ScriptNotebookEditor(project, file);
    }

    @Override
    protected FileEditor createSimplifiedEditor(Project project, VirtualFile file) {
        Module module = ModuleUtil.findModuleForFile(file, project);
        return new ScriptNodeEditor(module, file);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return NodeType.SCRIPT_NODE.getTypeName();
    }

}
