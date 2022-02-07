package io.datakitchen.ide.editors;

import javax.swing.*;

public interface DocumentEditor {
    void addDocumentChangeListener(DocumentChangeListener listener);
    void removeDocumentChangeListener(DocumentChangeListener listener);
    JComponent getEditorComponent();

}
