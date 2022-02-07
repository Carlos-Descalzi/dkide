package io.datakitchen.ide.ui;

import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;

public class ObjectTransferable implements Transferable {

    private final Serializable data;
    private final DataFlavor flavor;

    public ObjectTransferable(Serializable data, DataFlavor flavor){
        this.data = data;
        this.flavor = flavor;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{flavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return this.flavor.equals(flavor);
    }

    @NotNull
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)){
            return data;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
