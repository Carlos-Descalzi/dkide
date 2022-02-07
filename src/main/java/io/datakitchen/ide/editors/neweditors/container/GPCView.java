package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.ui.RoundedLineBorder;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.model.ScriptNodeKey;
import io.datakitchen.ide.model.ScriptNodeModel;
import io.datakitchen.ide.model.ScriptNodeModelEvent;
import io.datakitchen.ide.model.ScriptNodeModelListener;
import io.datakitchen.ide.ui.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;

public class GPCView extends ContainerView implements ScriptNodeModelListener {
    private final JPanel contents = new JPanel(new VerticalStackLayout());

    public GPCView(ContainerNodeView nodeView) {
        super(nodeView);

        JPanel containerView = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
        add(containerView, BorderLayout.CENTER);
        ((ScriptNodeModel)nodeView.getModel()).addScriptNodeModelListener(this);
        LabelWithActions imageLabel = new LabelWithActions("GPC", JLabel.CENTER,null, this::getImageActions);
        imageLabel.setHighlightOnHover(false);
        imageLabel.setBorder(LineBorder.bottom());
        containerView.setBorder(new CompoundBorder(new RoundedLineBorder(getForeground(),5,1), JBUI.Borders.empty(1, 1, 15, 1)));
        containerView.add(imageLabel, BorderLayout.NORTH);
        contents.setBorder(UIUtil.EMPTY_BORDER_5x5);
        containerView.add(contents, BorderLayout.CENTER);
        loadView();
    }

    private ScriptNodeModel getModel(){
        return (ScriptNodeModel) nodeView.getModel();
    }

    private Action[] getImageActions(LabelWithActions labelWithActions) {
        return new Action[]{
                new SimpleAction("Details", this::editDetails)
        };
    }

    private void editDetails(ActionEvent event) {
        GpcDetailsDialog dialog = new GpcDetailsDialog();

        ScriptNodeModel model = (ScriptNodeModel)nodeView.getModel();

        dialog.setAptPackage(model.getAptDependencies());
        dialog.setPipPackages(model.getPipDependencies());
        if (dialog.showAndGet()){
            model.setAptDependencies(dialog.getAptPackages());
            model.setPipDependencies(dialog.getPipPackages());
        }
    }

    private void loadView() {
        for (ScriptNodeKey key: getModel().getKeys()){
            addKey(key);
        }
    }

    private void addKey(ScriptNodeKey key) {
        ScriptNodeKeyView view = new ScriptNodeKeyView(nodeView, key, l -> new Action[]{
                new SimpleAction("Details", e -> editKeyDetails(key)),
                new SimpleAction("Remove", e -> getModel().removeKey(key))
        });
        contents.add(view);
        revalidate();
        repaint();
    }

    private void editKeyDetails(ScriptNodeKey key) {

        GpcKeyDetailsDialog dialog = new GpcKeyDetailsDialog(nodeView.getProject());
        dialog.setParameters(key.getParameters());
        dialog.setEnvironment(key.getEnvironment());
        dialog.setExports(key.getExports());
        if (dialog.showAndGet()){
            key.setParameters(dialog.getParameters());
            key.setEnvironment(dialog.getEnvironment());
            key.setExports(dialog.getExports());
        }
    }


    @Override
    public void drop(DropTargetDropEvent dtde) {

    }

    @Override
    public void keyAdded(ScriptNodeModelEvent event) {
        addKey(event.getKey());
    }

    @Override
    public void keyRemoved(ScriptNodeModelEvent event) {
        for (Component c: contents.getComponents()){
            if (((ScriptNodeKeyView)c).getKey() == event.getKey()){
                contents.remove(c);
                contents.revalidate();
                repaint();
            }
        }
    }

    @Override
    public void keyChanged(ScriptNodeModelEvent event) {
        for (Component c: contents.getComponents()){
            if (((ScriptNodeKeyView)c).getKey() == event.getKey()){
                c.revalidate();
                c.repaint();
            }
        }
    }

    @Override
    public void propertyChanged(ScriptNodeModelEvent scriptNodeModelEvent) {

    }


}
