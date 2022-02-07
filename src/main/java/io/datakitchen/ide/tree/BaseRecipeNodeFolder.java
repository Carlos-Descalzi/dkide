package io.datakitchen.ide.tree;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class BaseRecipeNodeFolder extends PsiDirectoryNode {
    public BaseRecipeNodeFolder(Project project, @NotNull PsiDirectory value, ViewSettings viewSettings) {
        super(project, value, viewSettings);
    }

    protected String getDisplayName(){
        PsiDirectory value = getValue();
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
        PsiDirectory value = getValue();
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
