package io.datakitchen.ide.editors.graph.paste;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.datakitchen.ide.builder.ActionNodeBuilder;
import io.datakitchen.ide.builder.ContainerNodeBuilder;
import io.datakitchen.ide.builder.DataMapperNodeBuilder;
import io.datakitchen.ide.builder.ScriptNodeBuilder;
import io.datakitchen.ide.dialogs.EnterNameDialog;
import io.datakitchen.ide.dialogs.NodeFromSQLFileDialog;
import io.datakitchen.ide.editors.graph.GraphEditor;
import io.datakitchen.ide.editors.graph.GraphModel;
import io.datakitchen.ide.editors.neweditors.palette.ModuleComponentSource;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class PasteFileHandler implements PasteHandler{

    private static final Set<String> SCRIPT_EXTENSIONS = Set.of("py","sh");
    private static final Set<String> SQL_EXTENSIONS = Set.of("sql");

    private final GraphEditor graphEditor;
    private final Module module;

    public PasteFileHandler(GraphEditor graphEditor, Module module){
        this.graphEditor = graphEditor;
        this.module = module;
    }
    @Override
    public boolean isTransferableSupported(Transferable transferable, Target target) {
        return transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public void accept(Transferable transferable, JComponent targetComponent) {

        try {
            List<File> files = ObjectUtil.cast(transferable.getTransferData(DataFlavor.javaFileListFlavor));

            for (File file : files) {
                if (file.isDirectory() && RecipeUtil.isNodeFolder(file)) {
                    if (!pasteNode(file, module)) {
                        break;
                    }
                } else if (makeNodeForFile(file)) {
                    break;
                } else {
                    System.out.println("Unknown file type:" + file);
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean makeNodeForFile(File file) {
        String extension = getExtension(file);
        if (SCRIPT_EXTENSIONS.contains(extension)){
            return makeScriptNode(file, true);
        } else if (SQL_EXTENSIONS.contains(extension)){
            return makeNodeForSqlFile(file);
        } else {
            return makeScriptNode(file, false);
        }

    }

    private String getExtension(File file){
        String fileName = file.getName();
        if (fileName.contains(".")){
            return fileName.substring(fileName.indexOf('.')+1).toLowerCase();
        }
        return null;
    }

    private boolean makeScriptNode(File file, boolean gpcFirst) {

        PasteOptionsDialog dialog = new PasteOptionsDialog(module.getProject(), file, true, gpcFirst);

        if (dialog.showAndGet()) {

            if (dialog.isUseGpc()) {
                new ScriptNodeBuilder(module)
                    .setScriptFile(file)
                    .build((VirtualFile nodeFolder) ->
                        ApplicationManager.getApplication().invokeLater(() ->
                            graphEditor.getGraphView().getModel().addNode(GraphModel.Node.process(nodeFolder.getName()))
                        )
                    );
            } else{
                new ContainerNodeBuilder(module)
                    .setImageName(dialog.getSelectedImageName())
                    .setCommandLine(dialog.getCommand())
                    .setDockerShareContent(file)
                    .build((VirtualFile nodeFolder)->
                        ApplicationManager.getApplication().invokeLater(() ->
                            graphEditor.getGraphView().getModel().addNode(GraphModel.Node.process(nodeFolder.getName()))
                        )
                    );
            }
            return true;
        }
        return false;
    }

    private boolean makeNodeForSqlFile(File file) {
        NodeFromSQLFileDialog dialog = new NodeFromSQLFileDialog(file, new ModuleComponentSource(module));
        if (dialog.showAndGet()){
            String nodeName = dialog.getName();
            switch(dialog.getOption()){
                case CREATE_ACTION:
                    makeActionNode(file, dialog, nodeName);
                    break;
                case CREATE_MAPPER:
                    makeDataMapperNode(file, dialog, nodeName);
                    break;
            }
        }

        return false;
    }

    private void makeDataMapperNode(File file, NodeFromSQLFileDialog dialog, String nodeName) {
        DataMapperNodeBuilder builder = new DataMapperNodeBuilder(module)
                .setNodeName(nodeName)
                .setSqlFile(file);
        if (dialog.getDataSource() != null) {
            builder.setConnector(dialog.getDataSource());
        } else {
            builder.setDsType(dialog.getDataSourceType());
        }
        if (dialog.getDataSink() != null){
            builder.setSinkConnector(dialog.getDataSink());
        } else {
            builder.setSinkType(dialog.getDataSinkType());
        }
        builder.build(() ->
            ApplicationManager.getApplication().invokeLater(() ->
                graphEditor.getGraphView().getModel().addNode(GraphModel.Node.process(nodeName))
            )
        );
    }

    private void makeActionNode(File file, NodeFromSQLFileDialog dialog, String nodeName) {
        ActionNodeBuilder builder = new ActionNodeBuilder(module)
                .setNodeName(nodeName)
                .setSqlFile(file);

        if (dialog.getDataSource() != null) {
            builder.setConnector(dialog.getDataSource());
        } else {
            builder.setDsType(dialog.getDataSourceType());
        }
        builder.build(() ->
            ApplicationManager.getApplication().invokeLater(() ->
                graphEditor.getGraphView().getModel().addNode(GraphModel.Node.process(nodeName))
            )
        );
    }

    private boolean pasteNode(File file, Module module) {
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);

        VirtualFile nodeFolder = VirtualFileManager.getInstance().findFileByNioPath(Path.of(file.toURI()));

        if (nodeFolder != null) {
            GraphModel model = graphEditor.getGraphView().getModel();

            final String newName;

            if (recipeFolder.findChild(file.getName()) != null) {
                if (model.getNodeByName(file.getName()) == null) {
                    model.addNode(GraphModel.Node.process(nodeFolder.getName()));
                    return true;
                } else {
                    EnterNameDialog dialog = new EnterNameDialog();
                    if (dialog.showAndGet()) {
                        newName = dialog.getName();
                    } else {
                        return false;
                    }
                }
            } else {
                newName = null;
            }

            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    if (newName == null) {
                        VfsUtil.copy(this, nodeFolder, recipeFolder);
                        model.addNode(GraphModel.Node.process(nodeFolder.getName()));
                    } else {
                        VirtualFile newDir = recipeFolder.createChildDirectory(this, newName);
                        VfsUtil.copyDirectory(this, nodeFolder, newDir, null);
                        model.addNode(GraphModel.Node.process(newName));
                    }

                } catch (IOException ex) {
                    Messages.showMessageDialog(module.getProject(), String.valueOf(ex.getMessage()), "Error", Messages.getErrorIcon());
                }
            });
            return true;
        }
        return false;
    }

}
