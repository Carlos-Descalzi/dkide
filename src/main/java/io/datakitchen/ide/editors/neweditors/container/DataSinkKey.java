package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.Key;

public interface DataSinkKey extends Key {
    void setContainerFileName(String containerFileName);
    String getContainerFileName();
}
