package io.datakitchen.ide.editors.overrides;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.ui.ItemList;
import io.datakitchen.ide.util.JsonUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalOverridesEditor extends JPanel implements Disposable {
    private static final Logger LOGGER = Logger.getInstance(LocalOverridesEditor.class);
    private final ItemList<LocalOverride> overrides = new ItemList<>(this::createOverride);
    private final LocalOverrideEditor editor;
    private final VirtualFile file;
    public LocalOverridesEditor(Module module, VirtualFile file){
        this.file = file;
        editor = new LocalOverrideEditor(module);
        Disposer.register(this, editor);
        setLayout(new BorderLayout());
        add(overrides, BorderLayout.WEST);
        add(editor, BorderLayout.CENTER);
        overrides.setEditor(new OverrideEditor());
        overrides.setRenderer(new OverrideRenderer());
        load();
        overrides.addListSelectionListener(this::itemSelected);
    }

    public void load(){
        editor.clear();
        try {
            Map<String, Object> contents = JsonUtil.read(file);
            overrides.setData(contents.entrySet().stream().map(e -> new LocalOverride(e.getKey(), e.getValue())).collect(Collectors.toList()));
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private static class OverrideEditor extends DefaultCellEditor {

        public OverrideEditor(){
            super(new JTextField());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            LocalOverride currentValue = (LocalOverride)value;

            JTextField field = (JTextField) super.getTableCellEditorComponent(table, currentValue.getName(), isSelected, row, column);

            return field;
        }
    }

    private LocalOverride createOverride(){
        return new LocalOverride("var-"+(overrides.getDataSize()+1));
    }

    private void itemSelected(ListSelectionEvent event) {
        editor.setValue(overrides.getSelected());
    }

    @Override
    public void dispose() {

    }

    public void save() {
        editor.updateCurrent();
        Map<String, Object> json = new LinkedHashMap<>();
        for (LocalOverride item: this.overrides.getData()){
            json.put(item.getName(), item.getContent());
        }
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                JsonUtil.write(json, file);
            } catch (Exception ex){
                LOGGER.error(ex);
            }
        });
    }
}
