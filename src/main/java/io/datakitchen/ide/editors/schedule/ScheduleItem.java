package io.datakitchen.ide.editors.schedule;

import io.datakitchen.ide.ui.NamedObject;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScheduleItem implements NamedObject, Transferable {

    public static final DataFlavor FLAVOR = new DataFlavor(ScheduleItem.class, "Schedule");

    private String name;
    private Map<String,Object> value;

    public ScheduleItem() {
        name = null;
        value = new LinkedHashMap<>();
    }

    public ScheduleItem(String key, Map<String,Object> jsonObject) {
        this.name = key;
        this.value = jsonObject;
    }

    public ScheduleItem(String name) {
        this.name = name;
        this.value = new LinkedHashMap<>();
    }

    public static ScheduleItem fromEntry(Map.Entry<String, Object> entry) {
        return new ScheduleItem(entry.getKey(), (Map<String, Object>) entry.getValue());
    }

    public String toString() {
        return name == null || name.equals("") ? "(no name)" : name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String,Object> getValue() {
        return value;
    }

    public void setValue(Map<String,Object> value) {
        this.value = value;
    }

    private static final DataFlavor[] FLAVORS = { FLAVOR };

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOR.equals(flavor);
    }

    @NotNull
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (FLAVOR.equals(flavor)){
            return this;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
