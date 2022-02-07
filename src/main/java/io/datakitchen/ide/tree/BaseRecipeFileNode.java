package io.datakitchen.ide.tree;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

public class BaseRecipeFileNode extends PsiFileNode {
    public BaseRecipeFileNode(Project project, @NotNull PsiFile value, ViewSettings viewSettings) {
        super(project, value, viewSettings);
    }

    @Override
    public Collection<AbstractTreeNode<?>> getChildrenImpl() {

        return ContainerUtil.emptyList();
    }

    protected String getDisplayName(){
        PsiFile value = getValue();
        if (value != null) {
            return value.getName();
        }
        return "(null)";
    }

    protected Icon getDisplayIcon(){
        return getValue().getIcon(Iconable.ICON_FLAG_READ_STATUS);
    }
    @Override
    protected void updateImpl(@NotNull PresentationData data) {
        PsiFile value = getValue();
        if (value != null) {
            data.setPresentableText(getDisplayName());
            data.setIcon(getDisplayIcon());

            VirtualFile file = getVirtualFile();
            if (file != null && file.is(VFileProperty.SYMLINK)) {
                @NlsSafe String target = file.getCanonicalPath();
                if (target == null) {
                    data.setAttributesKey(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);
                    data.setTooltip(IdeBundle.message("node.project.view.bad.link"));
                }
                else {
                    data.setTooltip(FileUtil.toSystemDependentName(target));
                }
            }
        }
    }
}
