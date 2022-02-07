package io.datakitchen.ide.editors.variation;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.ui.CheckBoxTableCellRenderer;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.ObjectUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariationEditor extends FormPanel {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final JTextArea description = new JTextArea();
    private final JTable overridesList = new JBTable();
    private final ComboBox<String> graph = new ComboBox<>();
    private final ComboBox<String> schedule = new ComboBox<>();
    private final ComboBox<String> ingredient = new ComboBox<>();
    private final FieldListener listener = new FieldListener(this::variationChanged);

    public VariationEditor(){

        addField("Description",
                new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                new Dimension(400,100));
        addField("Schedule",schedule,new Dimension(200,28));
        addField("Graph",graph,new Dimension(200,28));

        overridesList.setAutoCreateColumnsFromModel(false);
        overridesList.getColumnModel().addColumn(new TableColumn(0, 30, new CheckBoxTableCellRenderer(), new DefaultCellEditor(new JCheckBox())));
        overridesList.getColumnModel().addColumn(new TableColumn(1,170,new DefaultTableCellRenderer(), new DefaultCellEditor(new JTextField())));
        JScrollPane overridesScroll = new JBScrollPane(overridesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        overridesList.setModel(new OverridesTableModel());
        addField("Overrides",overridesScroll, new Dimension(200,200));
        addField("Ingredient definition", ingredient, new Dimension(200,28));

        listener.listen(description);
        listener.listen(schedule);
        listener.listen(graph);
        listener.listen(overridesList);
        listener.listen(ingredient);

    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        description.setEnabled(enabled);
        overridesList.setEnabled(enabled);
        graph.setEnabled(enabled);
        schedule.setEnabled(enabled);
        ingredient.setEnabled(enabled);
    }

    private void enableEvents() {
        listener.setEnabled(true);
    }

    private void disableEvents(){
        listener.setEnabled(false);
    }

    private void variationChanged(){
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }
    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }


    private void updateActions(){

    }

    public void saveVariation(VariationItem currentVariation) {
        currentVariation.getVariation().put("description", description.getText());
        currentVariation.getVariation().put("graph-setting",(String)graph.getSelectedItem());

        String scheduleName = (String)schedule.getSelectedItem();

        if (scheduleName != null) {
            currentVariation.getVariation().put("schedule-setting", scheduleName);
        } else {
            currentVariation.getVariation().remove("schedule-setting");
        }

        String ingredient = (String)this.ingredient.getSelectedItem();

        if (ingredient != null) {
            currentVariation.getVariation().put("ingredient-definition", ingredient);
        } else {
            currentVariation.getVariation().remove("ingredient-definition");
        }

        List<String> overrides = ((OverridesTableModel)this.overridesList.getModel()).getSelected();

        System.out.println("Selected overrides:"+overrides);

        currentVariation.getVariation().put("override-setting", overrides);
    }

    public void loadVariation(VariationItem currentVariation) {
        disableEvents();
        if (currentVariation != null) {
            description.setText((String)currentVariation.getVariation().getOrDefault("description",""));
            graph.setSelectedItem(currentVariation.getVariation().get("graph-setting"));

            Object overridesObj = currentVariation.getVariation().get("override-setting");

            List<String> overrides;
            if (overridesObj != null) {
                 overrides = overridesObj instanceof List
                        ? ((List<String>) overridesObj)
                        : List.of((String) overridesObj);
            } else {
                overrides = new ArrayList<>();
            }

            ((OverridesTableModel) overridesList.getModel()).setSelected(overrides);

            String scheduleName = (String) currentVariation.getVariation().get("schedule-setting");

            if (scheduleName == null) {
                scheduleName = (String) currentVariation.getVariation().get("mesos-setting");
            }

            schedule.setSelectedItem(scheduleName);

            String ingredient = (String)currentVariation.getVariation().get("ingredient-definition");
            this.ingredient.setSelectedItem(ingredient);
        } else {
            ((OverridesTableModel) overridesList.getModel()).setSelected(new ArrayList<>());
        }
        enableEvents();
    }

    private Map<String, Object> variationDocument;

    public void setVariationDocument(Map<String, Object> variationDocument) {
        this.variationDocument = variationDocument;
        updateOptions();
    }

    public void updateOptions() {
        disableEvents();
        Map<String, Object> graphs = ObjectUtil.cast(variationDocument.get("graph-setting-list"));
        if (graphs != null){
            DefaultComboBoxModel<String> graphsModel = new DefaultComboBoxModel<>(new ArrayList<>(graphs.keySet()).toArray(String[]::new));
            this.graph.setModel(graphsModel);
        }
        Map<String, Object> schedules = ObjectUtil.cast(variationDocument.get("schedule-setting-list"));
        if (schedules == null){
            schedules = ObjectUtil.cast(variationDocument.get("mesos-setting-list"));
        }
        if (schedules != null){
            List<String> schedulesList = new ArrayList<>(schedules.keySet());
            schedulesList.add(0, null);
            this.schedule.setModel(new DefaultComboBoxModel<>(schedulesList.toArray(String[]::new)));
        }
        Map<String, Object> overrides = ObjectUtil.cast(variationDocument.get("override-setting-list"));
        if (overrides != null){
            overridesList.setModel(new OverridesTableModel(new ArrayList<>(overrides.keySet())));
        }
        List<Map<String,Object>> ingredientDefinitions = ObjectUtil.cast(variationDocument.get("ingredient-definition-list"));

        List<String> ingredientNames = new ArrayList<>();
        ingredientNames.add(null);
        if (ingredientDefinitions != null){
            ingredientNames.addAll(ingredientDefinitions
                    .stream()
                    .map((Map<String, Object> o)-> (String)o.get("ingredient-name"))
                    .collect(Collectors.toList()));
        }
        ingredient.setModel(new DefaultComboBoxModel<>(ingredientNames.toArray(String[]::new)));
        enableEvents();
    }

    private static final String OBJ_INGREDIENT = "ingredient";
    private static final String OBJ_GRAPH = "graph";
    private static final String OBJ_OVERRIDE_SET = "overrideSet";
    private static final String OBJ_SCHEDULE = "schedule";

    @SuppressWarnings({"rawtypes","unchecked"})
    public void notifyReferencedObjectAdded(String typeName, String itemName) {
        switch(typeName){
            case OBJ_INGREDIENT:
                ((DefaultComboBoxModel)ingredient.getModel()).addElement(itemName);
                break;
            case OBJ_GRAPH:
                ((DefaultComboBoxModel)graph.getModel()).addElement(itemName);
                break;
            case OBJ_OVERRIDE_SET:
                ((OverridesTableModel)overridesList.getModel()).addElement(itemName);
                break;
            case OBJ_SCHEDULE:
                ((DefaultComboBoxModel)schedule.getModel()).addElement(itemName);
                break;
        }
    }

    @SuppressWarnings("rawtypes")
    public void notifyReferencedObjectRemoved(String typeName, String itemName) {
        switch(typeName){
            case OBJ_INGREDIENT:
                ((DefaultComboBoxModel)ingredient.getModel()).removeElement(itemName);
                break;
            case OBJ_GRAPH:
                ((DefaultComboBoxModel)graph.getModel()).removeElement(itemName);
                break;
            case OBJ_OVERRIDE_SET:
                ((OverridesTableModel)overridesList.getModel()).removeElement(itemName);
                break;
            case OBJ_SCHEDULE:
                ((DefaultComboBoxModel)schedule.getModel()).removeElement(itemName);
                break;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void notifyReferencedObjectChanged(String typeName, String itemName, String oldItemName) {
        switch(typeName){
            case OBJ_INGREDIENT:
                replaceElement((DefaultComboBoxModel)ingredient.getModel(), oldItemName, itemName);
                break;
            case OBJ_GRAPH:
                replaceElement((DefaultComboBoxModel)graph.getModel(), oldItemName, itemName);
                break;
            case OBJ_OVERRIDE_SET:
                ((OverridesTableModel)overridesList.getModel()).changeElement(oldItemName, itemName);
                break;
            case OBJ_SCHEDULE:
                replaceElement((DefaultComboBoxModel)schedule.getModel(), oldItemName, itemName);
                break;
        }
    }

    private void replaceElement(DefaultComboBoxModel<String> model, String oldItemName, String itemName) {
        int index = model.getIndexOf(oldItemName);
        if (index != -1){
            model.removeElementAt(index);
            model.insertElementAt(itemName, index);
        }
    }
}
