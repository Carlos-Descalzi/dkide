package io.datakitchen.ide.editors.neweditors.variation;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.ui.CheckBoxTableCellRenderer;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.ObjectUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VariationOverridesEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> listeners = EventSupport.of(DocumentChangeListener.class);

    private final JTable overridesTable = new JBTable();
    private final OverridesTableModel model = new OverridesTableModel();
    private final FieldListener listener = new FieldListener(this::saveVariation);
    private VariationInfo currentVariation;

    public VariationOverridesEditor(){
        JScrollPane scroll = new JBScrollPane(overridesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        addField("Variation Overrides", scroll, new Dimension(350,80));
        overridesTable.setAutoCreateColumnsFromModel(false);
        TableColumn c0 = new TableColumn(0);
        c0.setMinWidth(20);
        c0.setMaxWidth(20);
        c0.setCellRenderer(new CheckBoxTableCellRenderer());
        overridesTable.getColumnModel().addColumn(c0);
        TableColumn c1 = new TableColumn(1);
        overridesTable.getColumnModel().addColumn(c1);
        overridesTable.setModel(model);
        listener.listen(overridesTable);
        updateState();
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        listeners.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener){
        listeners.removeListener(listener);
    }

    @Override
    public JComponent getEditorComponent() {
        return this;
    }

    public VariationInfo getCurrentVariation() {
        return currentVariation;
    }

    public void setCurrentVariation(VariationInfo currentVariation) {
        listener.noListen(()->{
            this.currentVariation = currentVariation;

            if (this.currentVariation != null){
                model.setSelected(this.currentVariation.getOverrideSets());
            }
            updateState();

        });
    }

    private void updateState() {
        overridesTable.setEnabled(currentVariation != null);
    }

    private void saveVariation() {
        currentVariation.setOverrideSets(model.getSelected());
        listeners.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    public void setVariationsDocument(Map<String, Object> variationsDocument) {
        listener.noListen(()->{
            Map<String, Object> overrideSets = ObjectUtil.cast(variationsDocument.get("override-setting-list"));
            OverrideItem[] items = overrideSets.keySet().stream().map(OverrideItem::new).toArray(OverrideItem[]::new);
            model.setItems(items);
        });
    }

    private static class OverrideItem {
        private boolean selected;
        private final String name;

        public OverrideItem(String name){
            this.name = name;
        }
    }

    private static class OverridesTableModel extends AbstractTableModel {

        private OverrideItem[] items;

        public OverridesTableModel(){
            items = new OverrideItem[0];
        }

        public void setItems(OverrideItem[] items){
            Set<String> selected = getSelected();
            this.items = items;
            setSelected(selected);
        }

        public void setSelected(Set<String> selected){
            System.out.println("set selected "+selected);
            for (OverrideItem item:items){
                item.selected = selected.contains(item.name);
            }
            fireTableDataChanged();
        }

        public Set<String> getSelected(){
            return Arrays.stream(items).filter(i -> i.selected).map(i -> i.name).collect(Collectors.toSet());
        }

        @Override
        public int getRowCount() {
            return items.length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                items[rowIndex].selected = (Boolean) aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex){
                case 0:
                    return items[rowIndex].selected;
                case 1:
                    return items[rowIndex].name;
            }
            return null;
        }
    }
}
