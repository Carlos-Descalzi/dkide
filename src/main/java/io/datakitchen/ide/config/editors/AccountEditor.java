package io.datakitchen.ide.config.editors;

import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.UIUtil;

import javax.swing.*;

public class AccountEditor extends FormPanel {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JCheckBox useForIngredients = new JCheckBox();

    public AccountEditor(){
        setBorder(UIUtil.EMPTY_BORDER_10x10);

        addField("Username",username);
        addField("Password",password);
        addField("Use for Ingredients", useForIngredients);

        FieldListener fieldListener = new FieldListener(this::updateModel);
        fieldListener.listen(username);
        fieldListener.listen(password);
        fieldListener.listen(useForIngredients);
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }

    private void updateModel() {
        this.eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    public void setAccount(Account item) {
        if (item != null){
            username.setText(item.getUsername());
            password.setText(item.getPassword());
            useForIngredients.setSelected(item.isIngredientAccount());
            username.setEnabled(true);
            password.setEnabled(true);
            useForIngredients.setEnabled(true);
        } else {
            username.setText("");
            password.setText("");
            useForIngredients.setSelected(false);
            username.setEnabled(false);
            password.setEnabled(false);
            useForIngredients.setEnabled(false);
        }
    }

    public void saveAccount(Account account) {
        account.setUsername(username.getText());
        account.setPassword(String.valueOf(password.getPassword()));
        account.setIngredientAccount(useForIngredients.isSelected());
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
        useForIngredients.setEnabled(enabled);
    }


}
