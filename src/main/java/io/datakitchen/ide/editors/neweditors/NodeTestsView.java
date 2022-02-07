package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.NodeModel;
import io.datakitchen.ide.model.NodeModelEvent;
import io.datakitchen.ide.model.NodeModelListener;
import io.datakitchen.ide.model.Test;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.ui.VerticalStackLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NodeTestsView extends JPanel implements NodeModelListener {

    private final NodeModel nodeModel;
    private final Runnable onAddTest;

    public NodeTestsView(NodeModel nodeModel, Runnable onAddTest){
        super(new VerticalStackLayout());
        this.nodeModel = nodeModel;
        this.nodeModel.addNodeModelListener(this);
        this.onAddTest = onAddTest;
        JButton addTestButton = new JButton(new SimpleAction("+ Add test",this::addTest));
        addTestButton.setContentAreaFilled(false);
        addTestButton.setBorderPainted(false);
        addTestButton.setHorizontalAlignment(SwingConstants.LEFT);
        add(addTestButton);

        for (Test test: nodeModel.getTests()){
            doAddTest(test);
        }
    }

    private void doAddTest(Test test) {
        add(new TestView(test, this::getTestActions), getComponentCount()-1);
    }

    private void addTest(ActionEvent event) {
        this.onAddTest.run();
    }

    @Override
    public void testAdded(NodeModelEvent event) {
        doAddTest(event.getTest());
    }

    private Action[] getTestActions(TestView testView) {
        return new Action[]{
                new SimpleAction("Remove", e -> nodeModel.removeTest(testView.getTest()))
        };
    }

    @Override
    public void testRemoved(NodeModelEvent event) {
        for (Component c: getComponents()){
            if (c instanceof TestView && ((TestView)c).getTest() == event.getTest()){
                remove(c);
                validate();
                repaint();
                break;
            }
        }
    }

}
