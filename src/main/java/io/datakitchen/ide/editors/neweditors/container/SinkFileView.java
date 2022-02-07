package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.editors.neweditors.KeyView;

import java.awt.*;

public class SinkFileView extends KeyView {


    public SinkFileView(DataSinkKey key, ActionSupplier actionSupplier) {
        super(key, actionSupplier);
        setToolTipText("<html><pre>"+key.toString()+"</pre></html>");
    }

    @Override
    public Point getHookPoint() {
        return new Point(getWidth()+5,getHeight()/2);
    }


}
