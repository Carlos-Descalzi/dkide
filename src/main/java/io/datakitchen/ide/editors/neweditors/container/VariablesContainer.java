package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.Assignment;
import io.datakitchen.ide.model.ContainerModel;
import io.datakitchen.ide.model.ContainerModelEvent;
import io.datakitchen.ide.model.ContainerModelListener;
import io.datakitchen.ide.ui.LabelWithActions;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.ui.VerticalStackLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VariablesContainer extends JPanel implements ContainerModelListener, LabelWithActions.ActionSupplier {

    private final JPanel contents = new JPanel(new VerticalStackLayout());
    private final ContainerModel model;

    public VariablesContainer(ContainerModel model){
        this.model = model;
        setLayout(new BorderLayout());
        add(new JLabel("Variables"), BorderLayout.NORTH);
        add(contents, BorderLayout.CENTER);
        setMinimumSize(new Dimension(200,200));
        JButton addVariableButton = new JButton(new SimpleAction("+ Add variable", this::addOutputVariable));
        Font font = getFont();
        addVariableButton.setFont(font.deriveFont(font.getSize()-2f));
        addVariableButton.setContentAreaFilled(false);
        addVariableButton.setBorderPainted(false);
        addVariableButton.setHorizontalAlignment(JButton.LEFT);
        contents.add(addVariableButton);

        this.model.addContainerModelListener(this);
        loadFiles();
    }

    @Override
    public Action[] getActions(LabelWithActions requester) {
        return new Action[]{
            new SimpleAction("Remove", e -> removeAssignment(((VariableAssignmentView)requester).getAssignment()))
        };
    }

    private void removeAssignment(Assignment assignment) {
        model.removeAssignment(assignment);
    }

    private void addOutputVariable(ActionEvent event) {
        NewVariableDialog dialog = new NewVariableDialog();
        if (dialog.showAndGet()){
            model.addAssignment(dialog.getAssignment());
        }
    }

    private void loadFiles(){
        for (Assignment assignment: model.getAssignments()){
            addAssignment(assignment);
        }
        validate();
    }

    private void addAssignment(Assignment assignment) {
        contents.add(new VariableAssignmentView(assignment, this), contents.getComponentCount()-1);
    }

    private List<VariableAssignmentView> getViews(){
        return Arrays.stream(contents.getComponents())
            .filter(c -> c instanceof VariableAssignmentView)
            .map(c -> (VariableAssignmentView)c)
            .collect(Collectors.toList());
    }

    @Override
    public void variableAssignmentAdded(ContainerModelEvent event) {
        addAssignment(event.getAssignment());
        validate();
    }

    @Override
    public void variableAssignmentRemoved(ContainerModelEvent event) {
        for (VariableAssignmentView view:getViews()){
            if (view.getAssignment().equals(event.getAssignment())){
                contents.remove(view);
                validate();
                break;
            }
        }
    }

}
