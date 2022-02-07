package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.Key;

import java.awt.*;

public class SinkFileView extends KeyView {

    public SinkFileView(Key key, ActionSupplier actionSupplier) {
        super(key,actionSupplier);
    }

    @Override
    public Point getHookPoint() {
        return new Point(-5,getHeight()/2);
    }

}
