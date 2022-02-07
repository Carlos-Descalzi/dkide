package io.datakitchen.ide.editors.tests;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.ui.EditorUtil;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestEditor extends FormPanel implements Disposable {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final JTextField testVariable = new JTextField();
    private final ComboBox<String> testAction = new ComboBox<>(new DefaultComboBoxModel<>(new String[]{"log","warning","stop-on-error"}));
    private final ComboBox<String> testType = new ComboBox<>(new DefaultComboBoxModel<>(new String[]{
            "test-contents-as-integer",
            "test-contents-as-float",
            "test-contents-as-date",
            "test-contents-as-string",
            "test-contents-as-boolean",
            "test-contents-as-default"
    }));
    private final JRadioButton testLogicJinja = new JRadioButton("Jinja expression");
    private final JRadioButton testLogicDeclarative = new JRadioButton("Declarative");
    private final JTextField testLogicString = new JTextField();
    private final ComboBox<String> comparator = new ComboBox<>(new DefaultComboBoxModel<>(new String[]{
            "greater-than",
            "less-than",
            "equal-to",
            "not-equal-to",
            "greater-than-equal-to",
            "less-than-equal-to"
    }));
    private final Editor testMetric;
    private final DocumentListener documentListener = new DocumentListener() {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            testChanged();
        }
    };
    private final FocusListener focusListener = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            testChanged();
        }
    };
    private final ItemListener itemListener = __ -> testChanged();
    private final ActionListener actionListener = __ -> testChanged();


    public TestEditor(Project project){

        addField("Test variable",testVariable, new Dimension(250,28));
        addField("Test action",testAction,new Dimension(250,28));
        addField("Test type",testType,new Dimension(250,28));
        JPanel testLogicTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addField("Test logic type",testLogicTypePanel, new Dimension(400,28));
        testLogicTypePanel.add(testLogicJinja);
        testLogicTypePanel.add(testLogicDeclarative);
        ButtonGroup group = new ButtonGroup();
        group.add(testLogicJinja);
        group.add(testLogicDeclarative);

        addField("Logic string", testLogicString,new Dimension(250,28));
        addField("Test comparator", comparator,new Dimension(250,28));
        testMetric = EditorUtil.createJsonEditor(project);
        JComponent editorComponent = testMetric.getComponent();
        addField("Test metric", editorComponent, new Dimension(250,200));
        enableEvents();
        updateActions();

    }

    public void dispose(){
        EditorFactory.getInstance().releaseEditor(testMetric);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        testVariable.setEnabled(enabled);
        testLogicString.setEnabled(enabled & testLogicJinja.isSelected());
        testMetric.getContentComponent().setEnabled(enabled & testLogicDeclarative.isSelected());
        testLogicJinja.setEnabled(enabled);
        testLogicDeclarative.setEnabled(enabled);
        testAction.setEnabled(enabled);
        testType.setEnabled(enabled);
        comparator.setEnabled(enabled);
    }

    private void updateActions(){
        testLogicString.setEnabled(testLogicJinja.isSelected());
        comparator.setEnabled(testLogicDeclarative.isSelected());
        testMetric.getContentComponent().setEnabled(isEnabled() && testLogicDeclarative.isSelected());
    }

    private void disableEvents(){
        testVariable.removeFocusListener(focusListener);
        testLogicString.removeFocusListener(focusListener);
        testMetric.getDocument().removeDocumentListener(documentListener);
        testLogicJinja.removeActionListener(actionListener);
        testLogicDeclarative.removeActionListener(actionListener);
        testAction.removeItemListener(itemListener);
        testType.removeItemListener(itemListener);
        comparator.removeItemListener(itemListener);
    }
    private void enableEvents(){
        testVariable.addFocusListener(focusListener);
        testLogicString.addFocusListener(focusListener);
        testMetric.getDocument().addDocumentListener(documentListener);
        testLogicJinja.addActionListener(actionListener);
        testLogicDeclarative.addActionListener(actionListener);
        testAction.addItemListener(itemListener);
        testType.addItemListener(itemListener);
        comparator.addItemListener(itemListener);
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }

    private void testChanged(){
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
        updateActions();
    }

    public void saveTest(Test test){
        test.getTest().put("test-variable", testVariable.getText());
        test.getTest().put("action", testAction.getSelectedItem());
        test.getTest().put("type", testType.getSelectedItem());
        if (testLogicJinja.isSelected()) {
            test.getTest().put("test-logic", testLogicString.getText());
        } else {
            Map<String, Object> testLogic = new LinkedHashMap<>();
            testLogic.put("test-compare", comparator.getSelectedItem());

            String text = testMetric.getDocument().getText();
            Object jsonObj;
            try {
                jsonObj = JsonUtil.read(text);
            }catch(Exception ex){
                jsonObj = text;
            }
            testLogic.put("test-metric", jsonObj);
            test.getTest().put("test-logic", testLogic);
        }
    }

    public void loadTest(Test test) {
        disableEvents();
        if (test != null) {
            testVariable.setText(StringUtils.defaultString((String) test.getTest().get("test-variable"), ""));
            testAction.setSelectedItem(StringUtils.defaultString((String) test.getTest().get("action"), "stop-on-error"));
            testType.setSelectedItem(StringUtils.defaultString((String) test.getTest().get("type"), "test-contents-as-default"));

            Object testLogic = test.getTest().get("test-logic");
            if (testLogic instanceof String) {
                testLogicJinja.setSelected(true);
                testLogicString.setText((String) testLogic);
            } else {
                testLogicDeclarative.setSelected(true);

                Map<String, Object> testLogicMap = ObjectUtil.cast(testLogic);

                comparator.setSelectedItem(testLogicMap.get("test-compare"));
                Object testMetricStr = testLogicMap.get("test-metric");
                try {
                    String str = JsonUtil.toJsonString(testMetricStr);
                    EditorUtil.setText(testMetric, str);
                } catch (Exception ex) {
                    EditorUtil.setText(testMetric, String.valueOf(testMetricStr));
                }

            }
        } else {
            testVariable.setText("");
            testAction.setSelectedItem(0);
            testType.setSelectedItem(0);
            testLogicString.setText("");
            comparator.setSelectedItem(0);
            testLogicJinja.setSelected(true);
            EditorUtil.setText(testMetric,"");
        }
        enableEvents();
    }

}
