package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.ContainerModelListener;

import javax.swing.*;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

public abstract class ContainerView extends JPanel implements DropTargetListener, ContainerModelListener {

    protected ContainerNodeView nodeView;

    public ContainerView(ContainerNodeView nodeView){
        this.nodeView = nodeView;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {}

    @Override
    public void dragOver(DropTargetDragEvent dtde) {}

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    @Override
    public void dragExit(DropTargetEvent dte) {}

}
