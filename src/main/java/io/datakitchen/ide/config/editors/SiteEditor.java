package io.datakitchen.ide.config.editors;

import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class SiteEditor extends FormPanel {

    private final EventSupport<DocumentChangeListener> listeners = EventSupport.of(DocumentChangeListener.class);
    private final JTextField url = new JTextField();

    private final FieldListener fieldListener = new FieldListener(this::updateModel);

    public SiteEditor(){
        setBorder(UIUtil.EMPTY_BORDER_10x10);
        addField("Site URL", url, new Dimension(250,28));
        fieldListener.listen(url);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener) {
        this.listeners.removeListener(listener);
    }

    public void addDocumentChangeListener(DocumentChangeListener listener) {
        this.listeners.addListener(listener);
    }

    public void saveSite(Site site) {
        site.setUrl(url.getText());
    }

    public void setSite(Site site) {
        fieldListener.setEnabled(false);
        if (site != null){
            url.setText(site.getUrl());
            url.setEnabled(true);
        } else {
            url.setText("");
            url.setEnabled(false);
        }
        fieldListener.setEnabled(true);
    }

    private void updateModel() {
        this.listeners.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

}
