package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.util.IconLoader;
import io.datakitchen.ide.model.Key;
import io.datakitchen.ide.ui.LabelWithActions;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;

public abstract class KeyView extends LabelWithActions implements FileView{

    private final Key key;

    public KeyView(Key key, ActionSupplier actionSupplier){
        super(key.toString(), null, actionSupplier);
        this.key = key;
        label.setOpaque(false);
        label.setIcon(IconLoader.getIcon("/file16.png",getClass()));
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {}
            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }

    public String getText(){
        return label.getText();
    }

    public abstract Point getHookPoint();

    public JComponent asComponent(){
        return this;
    }

    public Key getKey() {
        return key;
    }

    public void updateView(){
        label.setText(key.toString());
    }
}
