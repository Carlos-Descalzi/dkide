package io.datakitchen.ide.editors.graph;

import com.github.rjeschke.txtmark.Processor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.ui.UIUtil;
import io.datakitchen.ide.util.JsonUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class DescriptionAwareGraphViewModel extends DefaultGraphViewModel{

    private final Project project;
    private final VirtualFile recipeFolder;

    public DescriptionAwareGraphViewModel(Project project, VirtualFile recipeFolder){
        this.project = project;
        this.recipeFolder = recipeFolder;
    }

    @Override
    public String getDescription(GraphModel.Node node) {
        VirtualFile nodeFolder = recipeFolder.findChild(node.getName());
        if (nodeFolder != null){
            String description = doGetDescription(nodeFolder);
            if (description != null) {
                if (description.startsWith("!markdown")) {
                    return Processor.process(description.replace("!markdown", "").strip());
                } else if (description.contains("<html>")) {
                    return description;
                } else {
                    return "<html><pre>" + description + "</pre></html>";
                }
            }
        }
        return null;
    }

    private String doGetDescription(VirtualFile folder){
        VirtualFile descriptionFile = folder.findChild("description.json");
        if (descriptionFile != null){
            try {
                Map<String, Object> descriptionJson = JsonUtil.read(descriptionFile);
                return (String)descriptionJson.get("description");
            }catch(Exception ignore){}
        }
        return null;
    }

    @Override
    public Icon getIcon(GraphModel.Node node, String nodeType) {

        VirtualFile nodeFolder = recipeFolder.findChild(node.getName());

        Icon icon = UIUtil.getCustomIcon(project, nodeFolder, new Dimension(40,40));
        if (icon != null){
            return icon;
        }

        return super.getIcon(node, nodeType);
    }
}
