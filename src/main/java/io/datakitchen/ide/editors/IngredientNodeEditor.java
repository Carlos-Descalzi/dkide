package io.datakitchen.ide.editors;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class IngredientNodeEditor extends AbstractNodeEditor {

    private EntryField ingredientKitchenName;
    private EntryField ingredientName;
    private EntryField ingredientRecipeName;
    private RegExValidatedField pollInterval;
    private RegExValidatedField timeout;
    private Editor inputVariables;
    private FieldListener listener;

    public IngredientNodeEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    @Override
    protected void enableEvents() {
        listener.setEnabled(true);
    }

    @Override
    protected void disableEvents() {
        listener.setEnabled(false);
    }

    @Override
    protected Map<String, JComponent> getTabs() {

        listener = new FieldListener(this::saveDocument);

        FormPanel generalPanel = new FormPanel();
        generalPanel.setBorder(JBUI.Borders.empty(10));
        Module module = ModuleUtil.findModuleForFile(file, project);

        ingredientKitchenName = new EntryField(module);
        generalPanel.addField("Ingredient kitchen name",ingredientKitchenName);

        ingredientRecipeName = new EntryField(module);
        generalPanel.addField("Ingredient recipe name", ingredientRecipeName);

        ingredientName = new EntryField(module);
        generalPanel.addField("Ingredient name", ingredientName);

        pollInterval = new RegExValidatedField(RegExValidatedField.NUMBER);
        generalPanel.addField("Order run poll interval", pollInterval, new Dimension(100,28));

        timeout = new RegExValidatedField(RegExValidatedField.NUMBER);
        generalPanel.addField("Order run timeout", timeout, new Dimension(100,28));

        inputVariables = EditorUtil.createJsonEditor(project);
        generalPanel.addField("Input variables JSON", inputVariables.getComponent(), new Dimension(500,400));

        listener.listen(ingredientKitchenName);
        listener.listen(ingredientName);
        listener.listen(ingredientRecipeName);
        listener.listen(pollInterval);
        listener.listen(timeout);
        listener.listen(inputVariables);

        Map<String, JComponent> tabs = new LinkedHashMap<>();
        tabs.put("General",generalPanel);

        return tabs;
    }

    @Override
    protected void doLoadDocument(Map<String,Object> document) {
        ingredientName.setText(StringUtils.defaultString((String)document.get("ingredient-name"),""));
        ingredientRecipeName.setText(StringUtils.defaultString((String)document.get("ingredient-recipe-name"),""));
        ingredientKitchenName.setText(StringUtils.defaultString((String)document.get("ingredient-kitchen-name"),""));

        Map<String, Object> requiredResults = ObjectUtil.cast(document.get("ingredient-required-orderrun-results"));

        if (requiredResults != null){
            Integer pollInterval = (Integer)requiredResults.get("orderrun-poll-interval");
            this.pollInterval.setText(pollInterval != null ? String.valueOf(pollInterval) : "");

            Integer timeout = (Integer) requiredResults.get("orderrun-timeout");
            this.timeout.setText(timeout != null ? String.valueOf(timeout): "");
        }

        Map<String, Object> inputVariables = ObjectUtil.cast(document.get("required-ingredient-variables"));
        if (inputVariables == null){
            inputVariables = new LinkedHashMap<>();
        }

        EditorUtil.setText(this.inputVariables, JsonUtil.toJsonString(inputVariables));
    }

    @Override
    protected void doSaveDocument(Map<String,Object> document) {
        document.put("ingredient-name", ingredientName.getText());
        document.put("ingredient-recipe-name", ingredientRecipeName.getText());
        document.put("ingredient-kitchen-name", ingredientKitchenName.getText());

        String inputVariables = this.inputVariables.getDocument().getText();
        if (StringUtils.isNotBlank(inputVariables)){
            try {
                Map<String, Object> inputVariablesDict = JsonUtil.read(inputVariables);
                document.put("required-ingredient-variables", inputVariablesDict);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        Map<String, Object> requiredResults = new LinkedHashMap<>();

        String pollInterval = this.pollInterval.getText();
        if (StringUtils.isNotBlank(pollInterval)) {
            requiredResults.put("orderrun-poll-interval", Integer.parseInt(pollInterval));
        }

        String timeout = this.timeout.getText();
        if (StringUtils.isNotBlank(timeout)){
            requiredResults.put("orderrun-timeout",Integer.parseInt(timeout));
        }

        if (!requiredResults.isEmpty()){
            document.put("ingredient-required-orderrun-results", requiredResults);
        }

    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Ingredient Editor";
    }

}
