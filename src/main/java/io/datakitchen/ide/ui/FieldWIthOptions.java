package io.datakitchen.ide.ui;

import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.function.Supplier;

public class FieldWIthOptions extends JPanel {
    private final EventSupport<FocusListener> eventSupport = EventSupport.of(FocusListener.class);
    private final FiltrableList list = new FiltrableList();
    private JBPopup popup;
    private final JTextField text = new JTextField();
    private final Action showVariablesAction = new SimpleAction("?", this::showOptions);
    private final Supplier<List<String>> optionsSupplier;

    public FieldWIthOptions(Supplier<List<String>> optionsSupplier){
        this.optionsSupplier = optionsSupplier;
        setLayout(new BorderLayout());
        add(text, BorderLayout.CENTER);
        JPanel buttonsContainer = new JPanel(new GridLayout(1, 2));
        add(buttonsContainer, BorderLayout.EAST);
        JButton variables = new JButton(showVariablesAction);
        variables.setFocusable(false);
        variables.setPreferredSize(new Dimension(20,28));
        buttonsContainer.add(variables);

        list.addActionListener(this::selectSecret);
        text.addFocusListener(new FocusLostListener(e -> FieldWIthOptions.this.fireFocusLost()));
    }

    private void fireFocusLost(){
        eventSupport.getProxy().focusLost(new FocusEvent(this,0));
    }

    public void addFocusListener(FocusListener listener){
        eventSupport.addListener(listener);
    }

    public void removeFocusListener(FocusListener listener){
        eventSupport.removeListener(listener);
    }

    private void selectSecret(ActionEvent e) {
        if (popup != null){
            this.text.setText(list.getValue());
            popup.cancel();
            popup = null;
            eventSupport.getProxy().focusLost(new FocusEvent(this, FocusEvent.FOCUS_LOST));
        }
    }

    public String getText(){
        return text.getText();
    }

    public void setText(String text){
        this.text.setText(text);
    }

    private void showOptions(ActionEvent e){
        if (popup != null){
            popup.cancel();
            popup = null;
        }

        List<String> allVariables = optionsSupplier.get();
        list.setList(allVariables);
        list.setPreferredSize(new Dimension(300, 200));

        popup = showPopup();
    }


    private JBPopup showPopup() {
        Point p = getLocationOnScreen();
        p.y+=getHeight();
        JBPopup popup = JBPopupFactory
                .getInstance()
                .createComponentPopupBuilder(list, list.getFilterField())
                .setFocusable(true)
                .setRequestFocus(true)
                .createPopup();
        popup.showInScreenCoordinates(this, p);
        return popup;
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        text.setEnabled(enabled);
        showVariablesAction.setEnabled(enabled);
    }

}
