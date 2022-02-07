package io.datakitchen.ide.tree.nodes;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.NodeType;

public class NoOpNode extends AbstractNode {


    public NoOpNode(Project project, VirtualFile item, ViewSettings viewSettings) {
        super(project, item, viewSettings);
    }

}
