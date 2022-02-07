package io.datakitchen.ide.ui;

import java.io.Serializable;

public interface NamedObject extends Serializable {

    String getName();
    void setName(String name);
}
