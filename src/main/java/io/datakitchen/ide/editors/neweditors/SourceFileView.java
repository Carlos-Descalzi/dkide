package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.Key;

import java.awt.*;

public class SourceFileView extends KeyView {


    public SourceFileView(Key key, ActionSupplier actionSupplier) {
        super(key, actionSupplier);
    }

    @Override
    public Point getHookPoint() {
        return new Point(getWidth()+5,getHeight()/2);
    }

}
