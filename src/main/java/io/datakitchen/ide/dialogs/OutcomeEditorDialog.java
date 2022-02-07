package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.model.Condition;
import io.datakitchen.ide.editors.graph.ConditionOutcome;
import io.datakitchen.ide.editors.graph.ConditionalOperator;
import io.datakitchen.ide.ui.FormPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.LinkedHashSet;

public class OutcomeEditorDialog extends DialogWrapper {

    private final JLabel conditionLabel= new JLabel();
    private final ComboBox<ConditionalOperator> comparator= new ComboBox<>(new DefaultComboBoxModel<>(ConditionalOperator.values()));
    private final JTextField metric= new JTextField();
    private final Condition condition;

    public OutcomeEditorDialog(Condition condition) {
        super(true);
        this.condition = condition;
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel();
        panel.addField("Condition for", conditionLabel);
        panel.addField("Comparator", comparator);
        panel.addField("Metric", metric);
        return panel;
    }

    public void load(ConditionOutcome outcome) {
        conditionLabel.setText(outcome.getCondition().toString());
        comparator.setSelectedItem(outcome.getOperator());
        metric.setText(outcome.getMetric());
    }

    public ConditionOutcome getOutcome(){
        ConditionalOperator operator = (ConditionalOperator)this.comparator.getSelectedItem();
        String metric = this.metric.getText();

        return condition.getOutcomes()
                .stream()
                .filter((ConditionOutcome o)->o.sameCondition(operator,metric))
                .findFirst().orElseGet(()->{
                    ConditionOutcome outcome = new ConditionOutcome(operator, metric, new LinkedHashSet<>(), null);
                    condition.addOutcome(outcome);
                    return outcome;
                });
    }
}
