package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.KeyEvent;
import io.datakitchen.ide.model.KeyListener;
import io.datakitchen.ide.ui.EventSupport;

public abstract class BaseDataSourceKey implements DataSourceKey{

    protected final EventSupport<KeyListener> listeners = EventSupport.of(KeyListener.class);

    private String containerFileName;

    public String getContainerFileName() {
        return containerFileName;
    }

    public void setContainerFileName(String containerFileName) {
        this.containerFileName = containerFileName;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

}
