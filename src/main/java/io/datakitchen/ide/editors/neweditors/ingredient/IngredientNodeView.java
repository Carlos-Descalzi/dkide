package io.datakitchen.ide.editors.neweditors.ingredient;

import com.intellij.openapi.editor.Editor;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.editors.neweditors.NodeTestsView;
import io.datakitchen.ide.editors.neweditors.TestEditorDialog;
import io.datakitchen.ide.model.IngredientNodeModel;
import io.datakitchen.ide.model.NodeModelListener;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class IngredientNodeView extends JPanel implements NodeModelListener {
    private final JEditorPane description = new JEditorPane();

    private final IngredientNodeModel model;

    private final EntryField ingredientKitchen;
    private final EntryField ingredientRecipe;
    private final EntryField ingredientName;
    private final RegExValidatedField timeout = new RegExValidatedField(RegExValidatedField.NUMBER);
    private final RegExValidatedField pollInterval = new RegExValidatedField(RegExValidatedField.NUMBER);
    private final Editor inputParameters;

    public IngredientNodeView(IngredientNodeModel model){
        this.model = model;
        setLayout(new BorderLayout());

        JScrollPane scroll = new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(100,100));
        JPanel descriptionArea = new JPanel(new BorderLayout());
        descriptionArea.add(scroll, BorderLayout.CENTER);
        descriptionArea.setPreferredSize(new Dimension(600,100));

        NodeTestsView tests = new NodeTestsView(model, this::onAddTest);

        JPanel content = new JPanel(new BorderLayout());
        content.add(descriptionArea, BorderLayout.NORTH);

        inputParameters = EditorUtil.createJsonEditor(model.getModule().getProject());
        ingredientKitchen = new EntryField(model.getModule());
        ingredientRecipe = new EntryField(model.getModule());
        ingredientName = new EntryField(model.getModule());

        FormPanel configuration = new FormPanel();
        configuration.addField("Ingredient Kitchen", ingredientKitchen);
        configuration.addField("Ingredient Recipe", ingredientRecipe);
        configuration.addField("Ingredient Name", ingredientName);
        configuration.addField("Timeout", timeout);
        configuration.addField("Poll interval", pollInterval);
        configuration.addField("Input parameters", inputParameters.getComponent(), new Dimension(300,100));
        content.add(configuration, BorderLayout.CENTER);

        content.add(tests, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.add(content, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        descriptionArea.setBorder(new TitledBorder(LineBorder.top(), "Description"));
        configuration.setBorder(new TitledBorder(LineBorder.top(), "Configuration"));
        tests.setBorder(new TitledBorder(LineBorder.top(),"Node Tests"));

        description.setText(StringUtils.defaultString(model.getDescription(),""));
        description.getDocument().putProperty(PlainDocument.tabSizeAttribute,4);
        model.addNodeModelListener(this);

        description.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                model.setDescription(description.getText());
            }
        });
        loadModel();
    }

    private void loadModel() {
        ingredientKitchen.setText(model.getIngredientKitchen());
        ingredientRecipe.setText(model.getIngredientRecipe());
        ingredientName.setText(model.getIngredientName());
        timeout.setText(model.getTimeout() == null ? "" : model.getTimeout().toString());
        pollInterval.setText(model.getPollInterval() == null ? "" : model.getPollInterval().toString());
        EditorUtil.setText(inputParameters, JsonUtil.toJsonString(model.getInputParameters()));
    }

    private void onAddTest() {
        TestEditorDialog dialog = new TestEditorDialog(model.getModule(), model.getNodeName());
        if (dialog.showAndGet()){
            model.addTest(dialog.createTest());
        }
    }

}
