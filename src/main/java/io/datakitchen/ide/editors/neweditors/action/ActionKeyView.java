package io.datakitchen.ide.editors.neweditors.action;

import com.intellij.ui.AncestorListenerAdapter;
import io.datakitchen.ide.editors.neweditors.ConnectionListView;
import io.datakitchen.ide.editors.neweditors.ConnectionView;
import io.datakitchen.ide.editors.neweditors.KeyView;

import javax.swing.event.AncestorEvent;
import java.awt.*;

public class ActionKeyView extends KeyView {


    public ActionKeyView(ActionKey key, ActionSupplier actionSupplier) {
        super(key, actionSupplier);
        setToolTipText("Double click to open the file");
        addAncestorListener(new AncestorListenerAdapter() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                updateView();
            }
        });
    }

    @Override
    public Point getHookPoint() {
        return new Point(getWidth()+5,getHeight()/2);
    }

    public void updateView(){
        ConnectionView connectionView = (ConnectionView)getParent().getParent();
        if (connectionView == null) {
            label.setText(getKey().toString());
        } else {
            ConnectionListView connectionListView = (ConnectionListView) connectionView.getParent();
            int i=1;
            for (Component c:connectionListView.getComponents()){
                if (c != connectionView){
                    i+= ((ConnectionView)c).getConnection().getKeys().size();
                } else {
                    break;
                }
            }
            for (KeyView c:connectionView.getKeyViews()){
                if (c == this){
                    break;
                }
                i++;
            }
            label.setText(i+" - "+getKey());
        }
    }
}
