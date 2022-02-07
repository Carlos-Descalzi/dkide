package io.datakitchen.ide.editors.variables;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.*;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OverridesEditor extends JPanel implements Disposable, VariationItemEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);
    private final EventSupport<VariationEditionListener> editionListeners = EventSupport.of(VariationEditionListener.class);
    private final ItemList<OverrideSet> overrideSets = new ItemList<>(this::createOverrideSet);
    private final RegExValidatedField name = new RegExValidatedField(RegExValidatedField.IDENTIFIER);
    private final Editor overrideSetEditor;

    private final FocusListener focusListener = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            documentChanged();
        }
    };
    private final ListSelectionListener listListener = __ -> showItem();
    private final DocumentChangeListener documentChangeListener = __ ->documentChanged();
    private final ItemListListener itemListListener = new ItemListListener() {
        @Override
        public void itemAdded(ItemListEvent event) {
            notifyItemAdded(event);
        }

        @Override
        public void itemRemoved(ItemListEvent event) {
            notifyItemRemoved(event);
        }

        @Override
        public void itemChanged(ItemListEvent event) {
            notifyItemChanged(event);
        }
    };

    private OverrideSet currentOverrideSet;
    private Map<String, Object> variationDocument;

    public OverridesEditor(Project project){
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());

        overrideSetEditor = EditorUtil.createJsonEditor(project);

        centerPanel.add(overrideSetEditor.getComponent(), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FormLayout(5,5));
        topPanel.setBorder(JBUI.Borders.empty(10));
        JLabel l = new JLabel("Name");
        l.setPreferredSize(new Dimension(100,28));
        l.setLabelFor(name);
        topPanel.add(l);
        name.setPreferredSize(new Dimension(200,28));
        topPanel.add(name);
        centerPanel.add(topPanel, BorderLayout.NORTH);
        add(overrideSets, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        updateActions();
        enableEvents();
    }

    private OverrideSet createOverrideSet() {
        return new OverrideSet("overrides-"+(overrideSets.getDataSize()+1));
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(overrideSetEditor);
    }

    private void enableEvents(){
        name.addFocusListener(this.focusListener);
        overrideSetEditor.getContentComponent().addFocusListener(this.focusListener);
        overrideSets.addListSelectionListener(this.listListener);
        overrideSets.addDocumentChangeListener(this.documentChangeListener);
        overrideSets.addItemListListener(this.itemListListener);
    }

    private void disableEvents(){
        name.removeFocusListener(this.focusListener);
        overrideSetEditor.getContentComponent().removeFocusListener(this.focusListener);
        overrideSets.removeListSelectionListener(this.listListener);
        overrideSets.removeDocumentChangeListener(this.documentChangeListener);
        overrideSets.removeItemListListener(this.itemListListener);
    }

    private void documentChanged() {
        if (currentOverrideSet != null){
            saveCurrentOverrideSet();
            eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
        }
    }


    private void saveCurrentOverrideSet(){
        currentOverrideSet.setName(name.getText());
        try {
            Map<String, Object> variables = JsonUtil.read(overrideSetEditor.getDocument().getText());
            currentOverrideSet.setVariables(variables);
        }catch  (Exception ex){
            ex.printStackTrace();
        }
    }

    private void showItem(){
        if (currentOverrideSet != null){
            saveCurrentOverrideSet();
        }
        currentOverrideSet = overrideSets.getSelected();

        if (currentOverrideSet != null){
            name.setText(currentOverrideSet.getName());
            String text = JsonUtil.toJsonString(currentOverrideSet.getVariables());
            EditorUtil.setText(overrideSetEditor, text);
            name.setEnabled(true);
            overrideSetEditor.getContentComponent().setEnabled(true);
        } else {
            name.setText("");
            EditorUtil.setText(overrideSetEditor, "");
            name.setEnabled(false);
            overrideSetEditor.getContentComponent().setEnabled(false);
        }

    }

    private void updateActions(){

    }

    public void setVariationDocument(Map<String, Object> document) {
        disableEvents();
        this.variationDocument = document;
        Map<String, Object> overrides = ObjectUtil.cast(document.get("override-setting-list"));

        List<OverrideSet> overrideSetList = new ArrayList<>();

        if (overrides != null) {
            overrideSetList.addAll(
                    overrides.entrySet().stream().map(OverrideSet::fromEntry).collect(Collectors.toList())
            );

        }
        overrideSets.setData(overrideSetList);
        currentOverrideSet = null;
        showItem();

        updateActions();
        enableEvents();
        if (overrides != null && overrides.size() > 0){
            this.overrideSets.setSelectedIndex(0);
        }
    }

    public void saveDocument() {
        this.variationDocument.put("override-setting-list",getOverrides());
    }

    public Map<String, Object> getOverrides(){
        Map<String, Object> overrideSetsObj = new LinkedHashMap<>();
        for (OverrideSet overrideSet: this.overrideSets.getData()){
            overrideSetsObj.put(overrideSet.getName(),overrideSet.getVariables());
        }
        return overrideSetsObj;
    }

    public void addDocumentChangeListener(DocumentChangeListener listener) {
        eventSupport.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener) {
        eventSupport.removeListener(listener);
    }

    public Map<String, Object> getOverridesByName(List<String> overrideNames) {
        Map<String, Object> overrides = new LinkedHashMap<>();

        for (String overrideName:overrideNames){
            overrideSets.getData().stream().filter(o -> o.getName().equals(overrideName))
                    .findFirst()
                    .ifPresent(o -> overrides.put(o.getName(), o.getVariables()));
        }

        return overrides;
    }

    @Override
    public void addVariationEditionListener(VariationEditionListener listener) {
        this.editionListeners.addListener(listener);
    }

    @Override
    public void removeVariationEditionListener(VariationEditionListener listener) {
        this.editionListeners.removeListener(listener);
    }

    private static class OverrideSet implements NamedObject{
        private String name;
        private Map<String, Object> variables;

        public OverrideSet(){
            variables = new LinkedHashMap<>();
        }

        public OverrideSet(String name, Map<String, Object> variables) {
            this.name = name;
            this.variables = variables;
        }

        public OverrideSet(String name) {
            this.name = name;
            this.variables = new LinkedHashMap<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }

        public String toString(){
            return StringUtils.isBlank(name) ? "(no name)" : name;
        }

        public static OverrideSet fromEntry(Map.Entry<String, Object> entry) {
            return new OverrideSet(entry.getKey(), ObjectUtil.cast(entry.getValue()));
        }
    }
    private void notifyItemAdded(ItemListEvent event){
        editionListeners.getProxy().variationItemAdded(
                new VariationEditionEvent(this,"overrideSet",event.getItem().getName())
        );
    }
    private void notifyItemRemoved(ItemListEvent event){
        editionListeners.getProxy().variationItemRemoved(
                new VariationEditionEvent(this,"overrideSet",event.getItem().getName())
        );
    }
    private void notifyItemChanged(ItemListEvent event){
        editionListeners.getProxy().variationItemChanged(
                new VariationEditionEvent(this,"overrideSet",event.getItem().getName(), event.getOldName())
        );
    }
}
