package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.editors.neweditors.EditorIcons;
import io.datakitchen.ide.editors.neweditors.KeyView;

import java.awt.*;

public class SourceFileView extends KeyView {


    public SourceFileView(DataSourceKey key, ActionSupplier actionSupplier) {
        super(key, actionSupplier);
        setToolTipText("<html><pre>"+ key +"</pre></html>");
        if (key instanceof DataSourceFileKey){
            setIcon(EditorIcons.FILE);
        } else {
            setIcon(EditorIcons.DATABASE);
        }
    }

    @Override
    public Point getHookPoint() {
        return new Point(getWidth()+5,getHeight()/2);
    }

}
