package io.datakitchen.ide.tree;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.ex.ProjectEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import io.datakitchen.ide.tree.nodes.AbstractNode;
import io.datakitchen.ide.tree.nodes.script.ScriptsFolderNode;
import io.datakitchen.ide.tree.nodes.script.TestFilesFolderNode;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecipeTreeStructureProvider implements TreeStructureProvider {
    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        List<AbstractTreeNode<?>> items = new ArrayList<>();
        assert parent.getProject() != null;

        if (parent.getValue() instanceof ProjectEx){
            try {
                for (Module module : ModuleManager.getInstance(parent.getProject()).getModules()) {
                    if ("dk-recipe".equals(module.getModuleTypeName())) {
                        items.add(new RecipeNode(parent.getProject(), module, settings));
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
            try {

                VirtualFile localOverridesFile = RecipeUtil.getLocalOverridesFile(parent.getProject());

                if (localOverridesFile != null) {
                    PsiManager mgr = PsiManager.getInstance(parent.getProject());
                    items.add(new PsiFileNode(parent.getProject(), mgr.findFile(localOverridesFile), settings));
                }
            }catch (ProcessCanceledException ignored){
            } catch (Exception ex){
//                ex.printStackTrace();
            }
        } else if (parent instanceof RecipeNode){
            items.addAll(parent.getChildren());
        } else if (parent instanceof PsiFileNode){
            items.addAll(parent.getChildren());
        } else if (parent instanceof AbstractNode){
            items.addAll(parent.getChildren());
        } else if (parent instanceof ScriptsFolderNode){
            items.addAll(parent.getChildren());
        } else if (parent instanceof TestFilesFolderNode) {
            items.addAll(parent.getChildren());
        } else if (parent instanceof BaseRecipeNodeFolder) {
            items.addAll(parent.getChildren());
        } else if (parent.getValue() instanceof PsiDirectory){
            items.addAll(parent.getChildren());
        }

        return items;
    }


}
