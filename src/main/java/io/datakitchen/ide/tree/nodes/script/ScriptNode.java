package io.datakitchen.ide.tree.nodes.script;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import io.datakitchen.ide.tree.nodes.AbstractContainerNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScriptNode extends AbstractContainerNode {

    private ScriptsFolderNode scriptsFolderNode;
    private TestFilesFolderNode testFilesFolderNode;

    public ScriptNode(Project project, VirtualFile item, ViewSettings viewSettings) {
        super(project, item,viewSettings);

        try {
            PsiManager mgr = PsiManager.getInstance(project);

            VirtualFile dockerShareFolder = item.findChild("docker-share");

            if (dockerShareFolder != null) {
                this.scriptsFolderNode = new ScriptsFolderNode(project, mgr.findDirectory(dockerShareFolder), viewSettings);
            }
            VirtualFile testFilesFolder = item.findChild("test-files");
            if (testFilesFolder != null) {
                this.testFilesFolderNode = new TestFilesFolderNode(project, mgr.findDirectory(testFilesFolder), viewSettings);
            }
        } catch (ProcessCanceledException ex){
            throw ex;
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

//    protected String getIconName(){
//        return NodeType.CONTAINER_NODE_TYPE_NAME;
//    }

    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<AbstractTreeNode<?>> items = new ArrayList<>();

        if (scriptsFolderNode != null) {
            items.add(scriptsFolderNode);
        }

        if (testFilesFolderNode != null) {
            items.add(testFilesFolderNode);
        }

        items.addAll(super.getChildren());

        return items;
    }
}
