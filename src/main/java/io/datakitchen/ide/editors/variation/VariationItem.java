package io.datakitchen.ide.editors.variation;

import io.datakitchen.ide.ui.NamedObject;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class VariationItem implements NamedObject, Transferable {

    private String name = "";
    private Map<String,Object> variation = new LinkedHashMap<>();
    private transient Function<VariationItem, VariationInfo> transferDataSupplier;

    public static VariationItem fromEntry(Map.Entry<String, Object> entry) {
        VariationItem item = new VariationItem();
        item.setName(entry.getKey());
        item.setVariation((Map<String,Object>) entry.getValue());
        return item;
    }

    public VariationItem(){}

    public VariationItem(String name) {
        this.name = name;
    }

    public void setTransferDataSupplier(Function<VariationItem, VariationInfo> transferDataSupplier) {
        this.transferDataSupplier = transferDataSupplier;
    }

    public String toString() {
        return name.equals("") ? "(no name)" : name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String,Object> getVariation() {
        return variation;
    }

    public void setVariation(Map<String,Object> variation) {
        this.variation = variation;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{VariationInfo.FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return VariationInfo.FLAVOR.equals(flavor);
    }

    @NotNull
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)){
            throw new UnsupportedFlavorException(flavor);
        }
        if (transferDataSupplier == null){
            throw new IOException("Unable to transfer, no transfer data supplier");
        }
        return transferDataSupplier.apply(this);
    }
}
