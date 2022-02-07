package io.datakitchen.ide;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import io.datakitchen.ide.editors.graph.GraphInfo;
import io.datakitchen.ide.editors.variation.VariationInfo;
import io.datakitchen.ide.service.HookService;
import io.datakitchen.ide.util.VariationUtil;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.dnd.*;

public class ApplicationListener implements AppLifecycleListener {

    @Override
    public void appWillBeClosed(boolean isRestart) {
    }

    @Override
    public void appStarted() {
        for (Project project: ProjectManager.getInstance().getOpenProjects()){
            HookService.getInstance(project); // TODO change this
            DropTarget dropTarget = new DropTarget();
            AbstractProjectViewPane pane = ProjectView.getInstance(project).getCurrentProjectViewPane();
            JTree tree = pane.getTree();
            tree.setDropTarget(dropTarget);
            try {
                dropTarget.addDropTargetListener(new DropListener(pane, tree));
            }catch(Exception ignored){}
        }
    }

    private static class DropListener implements DropTargetListener {
        private final JTree tree;
        private final AbstractProjectViewPane pane;

        public DropListener(AbstractProjectViewPane pane, JTree tree){
            this.pane = pane;
            this.tree = tree;
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {}
        @Override
        public void dragOver(DropTargetDragEvent dtde) {}
        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {}
        @Override
        public void dragExit(DropTargetEvent dte) {}

        @Override
        public void drop(DropTargetDropEvent dtde) {
            Point p = dtde.getLocation();
            TreePath path = tree.getPathForLocation(p.x, p.y);

            if (path != null && path.getPathCount() >= 2) {
                Object obj = path.getPath()[1]; // the module
                Object value = pane.getValueFromNode(obj);
                if (value instanceof Module){
                    Module module = (Module) value;
                    handleDrop(dtde, module);
                }
            }
        }

        private void handleDrop(DropTargetDropEvent dtde, Module module) {
            try {
                if (dtde.isDataFlavorSupported(VariationInfo.FLAVOR)) {
                    VariationUtil.copyVariation(
                            (VariationInfo) dtde.getTransferable().getTransferData(VariationInfo.FLAVOR),
                            module
                    );
                } else if (dtde.isDataFlavorSupported(GraphInfo.FLAVOR)){
                    VariationUtil.copyGraph(
                            (GraphInfo) dtde.getTransferable().getTransferData(GraphInfo.FLAVOR),
                            module
                    );
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
