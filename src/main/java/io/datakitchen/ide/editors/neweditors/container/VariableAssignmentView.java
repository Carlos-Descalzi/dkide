package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.editors.neweditors.EditorIcons;
import io.datakitchen.ide.model.Assignment;
import io.datakitchen.ide.ui.LabelWithActions;

public class VariableAssignmentView extends LabelWithActions {

    private final Assignment assignment;

    public VariableAssignmentView(Assignment assignment, ActionSupplier actionSupplier){
        super("", EditorIcons.VARIABLE, actionSupplier);

        this.assignment = assignment;
        label.setText("docker-share/"+assignment.getFile()+" -> "+assignment.getVariable());
    }

    public Assignment getAssignment() {
        return assignment;
    }
}
