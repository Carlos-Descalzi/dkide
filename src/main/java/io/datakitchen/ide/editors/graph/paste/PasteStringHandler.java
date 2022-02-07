package io.datakitchen.ide.editors.graph.paste;

import com.intellij.openapi.module.Module;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.builder.ContainerNodeBuilder;
import io.datakitchen.ide.editors.graph.GraphEditor;
import io.datakitchen.ide.editors.graph.GraphModel;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URL;

public class PasteStringHandler implements PasteHandler{

    private final GraphEditor graphEditor;
    private final Module module;

    public PasteStringHandler(GraphEditor graphEditor, Module module){
        this.graphEditor = graphEditor;
        this.module = module;
    }
    @Override
    public boolean isTransferableSupported(Transferable transferable, Target target) {
        return transferable.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public void accept(Transferable transferable, JComponent targetComponent) {
        try {
            String text = (String) transferable.getTransferData(DataFlavor.stringFlavor);
            if (StringUtils.isNotBlank(text) && text.startsWith("docker pull ")) {
                createContainerNode(text.replace("docker pull ", ""));
            } else if (isDockerhubUrl(text)) {
                createContainerNodeFromUrl(text);
            } else {
                System.out.println("clipboard contents:" + text);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void createContainerNodeFromUrl(String text) {
        try {
            String path = new URL(text)
                    .getPath()
                    .replace("/_/","")
                    .replace("/r/","");
            createContainerNode(path);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void createContainerNode(String imageName) {

        new ContainerNodeBuilder(module)
                .setImageName(imageName)
                .build( nodeFolder ->
                        graphEditor.getGraphView().getModel().addNode(GraphModel.Node.process(nodeFolder.getName()))
                );
    }

    private boolean isDockerhubUrl(String text) {
        return text.startsWith(Constants.DOCKERHUB_IMAGE_URL_PREFIX);
    }

}
