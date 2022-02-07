package io.datakitchen.ide.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.Secret;
import io.datakitchen.ide.util.RecipeUtil;

import javax.swing.*;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.stream.Collectors;

public class EntryField extends JPanel {
    private final EventSupport<FocusListener> eventSupport = EventSupport.of(FocusListener.class);
    private final Module module;
    private final FiltrableList list = new FiltrableList();
    private JBPopup variablesPopup;
    private JBPopup secretPopup;
    private final JTextField text = new JTextField();
    private final Action showVariablesAction = new SimpleAction("V", this::showVariables);
    private final Action showSecretsAction = new SimpleAction("S", this::showSecrets);

    public EntryField(Module module){
        this(module, null, null);
    }
    public EntryField(Module module, DocumentFilter documentFilter){
        this(module, null, documentFilter);
    }
    public EntryField(Module module, InputVerifier inputVerifier, DocumentFilter documentFilter){
        this.module = module;
        setLayout(new BorderLayout());
        add(text, BorderLayout.CENTER);
        JPanel buttonsContainer = new JPanel(new GridLayout(1, 2));
        add(buttonsContainer, BorderLayout.EAST);
        JButton variables = new JButton(showVariablesAction);
        variables.setFocusable(false);
        variables.setPreferredSize(new Dimension(20,28));
        buttonsContainer.add(variables);
        JButton secrets = new JButton(showSecretsAction);
        secrets.setFocusable(false);
        secrets.setPreferredSize(new Dimension(20,28));
        buttonsContainer.add(secrets);

        list.addActionListener(this::selectSecret);
        text.addFocusListener(new FocusLostListener(e -> EntryField.this.fireFocusLost()));
        if (inputVerifier != null){
            text.setInputVerifier(inputVerifier);
        }
        if (documentFilter != null){
            ((PlainDocument)text.getDocument()).setDocumentFilter(documentFilter);
        }
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
        if (secretPopup != null){
            this.text.setText("#{vault://"+list.getValue()+"}");
            secretPopup.cancel();
            secretPopup = null;
            eventSupport.getProxy().focusLost(new FocusEvent(this, FocusEvent.FOCUS_LOST));
        }
        if (variablesPopup != null){
            this.text.setText("{{"+list.getValue()+"}}");
            variablesPopup.cancel();
            variablesPopup = null;
            eventSupport.getProxy().focusLost(new FocusEvent(this, FocusEvent.FOCUS_LOST));
        }
    }

    public String getText(){
        return text.getText();
    }

    public void setText(String text){
        this.text.setText(text);
    }

    private void showVariables(ActionEvent e){
        if (secretPopup != null){
            secretPopup.cancel();
            secretPopup = null;
        }
        if (variablesPopup != null){
            variablesPopup.cancel();
            variablesPopup = null;
        } else {
            List<String> allVariables = RecipeUtil.getPlainVariables(module);
            list.setList(allVariables);
            list.setPreferredSize(new Dimension(300, 200));

            variablesPopup = showPopup();
        }
    }
    private void showSecrets(ActionEvent e){
        if (variablesPopup != null){
            variablesPopup.cancel();
            variablesPopup = null;
        } else if (secretPopup != null){
            secretPopup.cancel();
            secretPopup = null;
        } else {
            List<String> secrets = ConfigurationService
                    .getInstance(module.getProject())
                    .getSecrets()
                    .stream()
                    .map(Secret::toString)
                    .collect(Collectors.toList());

            list.setList(secrets);
            list.setPreferredSize(new Dimension(300, 200));

            secretPopup = showPopup();
        }
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
        showSecretsAction.setEnabled(enabled);
    }

}
