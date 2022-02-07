package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.Key;

public interface DataSourceKey extends Key {
//    String getInputFileName();
//    void setInputFileName(String inputFileName);

    String getContainerFileName();

    void setContainerFileName(String containerFileName);

}
