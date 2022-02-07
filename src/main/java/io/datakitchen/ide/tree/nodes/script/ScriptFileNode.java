package io.datakitchen.ide.tree.nodes.script;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiFile;
import io.datakitchen.ide.tree.BaseRecipeFileNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptFileNode extends BaseRecipeFileNode {
    private final ScriptsFolderNode parent;

    public ScriptFileNode(@Nullable Project project, ScriptsFolderNode parent, PsiFile psiFile, ViewSettings viewSettings) {
        super(project, psiFile, viewSettings);
        this.parent = parent;
    }
    @Override
    protected void updateImpl(@NotNull PresentationData data) {
        PsiFile value = getValue();
        super.updateImpl(data);

        if (value != null){
            String fileName = value.getName();
            if (parent.getMainScripts().contains(fileName)){
                data.setIcon(IconLoader.getIcon("/icons/green.svg",getClass()));
            }
        }

    }
}
