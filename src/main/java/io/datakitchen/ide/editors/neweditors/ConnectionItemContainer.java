package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.ui.LineBorder;
import io.datakitchen.ide.ui.UIUtil;
import io.datakitchen.ide.ui.VerticalStackLayout;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ConnectionItemContainer<T extends JComponent> extends JPanel {
    private final String title;

    public ConnectionItemContainer(String title){
        super(new VerticalStackLayout());
        this.title = title;
        updateView();
    }

    private void updateView(){
        if (getComponentCount() == 0){
            setBorder(UIUtil.EMPTY_BORDER_5x5);
        } else {
            setBorder(new CompoundBorder(
                UIUtil.EMPTY_BORDER_5x5,
                new TitledBorder(
                    LineBorder.top(getForeground().darker()),
                    title,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    getFont().deriveFont(11f),
                    getForeground().darker()
                ))
            );
        }
        validate();
    }

    public void addItem(T testView){
        super.add(testView);
        updateView();
    }

    public List<T> getItems(){
        return Arrays.stream(getComponents()).map(c -> (T)c).collect(Collectors.toList());
    }

    public void remove(Function<T, Boolean> filter){
        for (T item: getItems()){
            if (filter.apply(item)){
                remove(item);
                updateView();
                break;
            }
        }
    }
}
