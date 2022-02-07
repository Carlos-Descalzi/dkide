package io.datakitchen.ide.editors.overrides;

import javax.swing.*;
import javax.swing.event.ChangeListener;

public interface ValueEditor {
    void addChangeListener(ChangeListener listener);
    void removeChangeListener(ChangeListener listener);
    Object getValue();
    void setValue(Object value);
    JComponent getComponent();
}
