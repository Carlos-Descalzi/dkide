package io.datakitchen.ide.editors.neweditors;

import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;

public class KeyReference implements Serializable {

    public static final DataFlavor FLAVOR = new DataFlavor(KeyReference.class, "Key reference");

    private String connectionName;
    private String keyName;

    public KeyReference(String connectionName, String keyName) {
        this.connectionName = connectionName;
        this.keyName = keyName;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}
