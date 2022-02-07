package io.datakitchen.ide.editors.neweditors.variation;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.ui.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

public class VariationDetailsEditor extends JPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> listeners = EventSupport.of(DocumentChangeListener.class);

    private final ScheduleEditor scheduleEditor = new ScheduleEditor();
    private final VariationOverridesEditor overridesEditor = new VariationOverridesEditor();
    private final JTextArea description = new JTextArea();
    private VariationInfo currentVariation;
    private final FieldListener listener = new FieldListener(this::saveVariation);

    public VariationDetailsEditor(){
        JPanel contents = new JPanel(new VerticalStackLayout());
        contents.setPreferredSize(new Dimension(600,600));
        setLayout(new BorderLayout());
        add(contents, BorderLayout.WEST);
        contents.add(buildDescriptionOptions());
        contents.add(buildScheduleOptions());
        contents.add(buildOverridesOptions());
        listener.listen(scheduleEditor);
        listener.listen(overridesEditor);
        updateState();
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        listeners.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener){
        listeners.removeListener(listener);
    }

    @Override
    public JComponent getEditorComponent() {
        return this;
    }

    private void saveVariation() {
        listener.noListen(()->{
            if (currentVariation != null){
                currentVariation.setDescription(description.getText());
                listeners.getProxy().documentChanged(new DocumentChangeEvent(this));
            }
            updateState();
        });
    }

    private JComponent buildScheduleOptions() {
        scheduleEditor.setBorder(makeBorder("Schedule and Runtime"));
        return scheduleEditor;
    }

    private JComponent buildOverridesOptions() {
        overridesEditor.setBorder(makeBorder("Overrides"));
        return overridesEditor;
    }

    private JComponent buildDescriptionOptions() {
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBorder(makeBorder("Variation Description"));
        JScrollPane scroll = new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(500,80));
        descriptionPanel.add(scroll, BorderLayout.CENTER);
        listener.listen(description);
        description.setBackground(getBackground());
        return descriptionPanel;
    }

    private Border makeBorder(String title){
        return new CompoundBorder(
            new TitledBorder(
                LineBorder.top(getBackground().brighter()),
                title
            ),
            JBUI.Borders.empty(5)
        );
    }

    public void setCurrentVariation(VariationInfo currentVariation) {
        listener.noListen(()->{
            this.currentVariation = currentVariation;

            if (this.currentVariation != null){
                scheduleEditor.setCurrentVariation(this.currentVariation);
                overridesEditor.setCurrentVariation(this.currentVariation);
                description.setText(this.currentVariation.getDescription());
            } else {
                scheduleEditor.setCurrentVariation(null);
                description.setText("");
            }
            updateState();
        });
    }

    private void updateState() {
        description.setEnabled(currentVariation != null);
    }

    public void setVariationsDocument(Map<String, Object> variationsDocument) {
        overridesEditor.setVariationsDocument(variationsDocument);
    }

}
