package io.datakitchen.ide.editors;

import java.util.EventListener;

public interface DocumentChangeListener extends EventListener {
    void documentChanged(DocumentChangeEvent e);
}
