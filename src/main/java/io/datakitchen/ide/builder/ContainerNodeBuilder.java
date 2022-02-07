package io.datakitchen.ide.builder;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.util.JsonUtil;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

public class ContainerNodeBuilder {
    private final Module module;
    private String imageName;
    private String command;
    private File dockerShareContent;

    public ContainerNodeBuilder(Module module){
        this.module = module;
    }

    public ContainerNodeBuilder setImageName(String imageName){
        this.imageName = imageName;
        return this;
    }

    public ContainerNodeBuilder setCommandLine(String command){
        this.command = command;
        return this;
    }

    public ContainerNodeBuilder setDockerShareContent(File dockerShareContent) {
        this.dockerShareContent = dockerShareContent;
        return this;
    }

    public void build(Consumer<VirtualFile> onFinish){
        String nodeName = imageName.replace("/","_")
                .replace(".","_")
                .replace(":","_");

        new NodeBuilder(module.getProject())
                .setModule(module)
                .setNodeName(nodeName)
                .setNodeType(NodeType.CONTAINER_NODE.getTypeName())
                .build((VirtualFile nodeFolder)->{
                    editNotebook(nodeFolder);
                    onFinish.accept(nodeFolder);
            });

    }

    private void editNotebook(VirtualFile nodeFolder) {
        VirtualFile notebookFile = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);

        String[] parsedName = parseImageName();

        try {
            Map<String, Object> notebook = JsonUtil.read(notebookFile);
            notebook.put("dockerhub-namespace", parsedName[0]);
            notebook.put("image-repo", parsedName[1]);
            notebook.put("image-tag", parsedName[2]);
            notebook.put("analytic-container", false);
            if (command != null){
                notebook.put("command-line",command);
            }
            if (dockerShareContent != null) {
                VirtualFile content = VfsUtil.findFileByIoFile(dockerShareContent, true);
                VfsUtil.copyFile(this, content, nodeFolder.findChild("docker-share"));
            }
            JsonUtil.write(notebook, notebookFile);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private String[] parseImageName(){
        String namespace;
        String name;
        String tag;

        if (imageName.contains("/")){
            String[] tokens = imageName.split("/");
            namespace = tokens[0];
            name = tokens[1];
        } else {
            namespace = "";
            name = imageName;
        }

        if (name.contains(":")){
            String[] tokens = name.split(":");
            name = tokens[0];
            tag = tokens[1];
        } else {
            tag = "latest";
        }

        return new String[]{namespace, name, tag};
    }
}
