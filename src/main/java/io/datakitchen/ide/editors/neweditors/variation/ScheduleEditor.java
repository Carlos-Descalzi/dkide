package io.datakitchen.ide.editors.neweditors.variation;

import com.intellij.openapi.ui.ComboBox;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.RegExValidatedField;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScheduleEditor extends JPanel implements DocumentEditor {
    private VariationInfo currentVariation;

    private final EventSupport<DocumentChangeListener> listeners = EventSupport.of(DocumentChangeListener.class);

    private final JCheckBox scheduleEnabled = new JCheckBox("Set schedule and runtime options");
    private final JTextField schedule = new JTextField();
    private final ComboBox<String> timeZone = new ComboBox<>();
    private final RegExValidatedField ram = new RegExValidatedField(RegExValidatedField.NUMBER);
    private final RegExValidatedField disk = new RegExValidatedField(RegExValidatedField.NUMBER);

    private final FieldListener listener = new FieldListener(this::saveSchedule);

    public ScheduleEditor(){
        setLayout(new BorderLayout());
        add(scheduleEnabled, BorderLayout.NORTH);
        listener.listen(scheduleEnabled);

        List<String> timezones = new ArrayList<>(ZoneId.getAvailableZoneIds());
        timezones.sort(Comparator.naturalOrder());
        timezones.add(0, null);
        timeZone.setModel(new DefaultComboBoxModel<>(timezones.toArray(String[]::new)));

        FormPanel contents = new FormPanel();
        contents.addField("Schedule", schedule);
        contents.addField("Time zone", timeZone);
        contents.addField("Max. RAM", ram);
        contents.addField("Max. disk", disk);
        listener.listen(schedule);
        listener.listen(timeZone);
        listener.listen(ram);
        listener.listen(disk);
        add(contents, BorderLayout.CENTER);
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

    public VariationInfo getCurrentVariation() {
        return currentVariation;
    }

    public void setCurrentVariation(VariationInfo currentVariation) {
        listener.noListen(()->{
            this.currentVariation = currentVariation;

            if (this.currentVariation != null) {
                VariationInfo.Schedule schedule = currentVariation.getSchedule();
                if (schedule == null) {
                    clearForm();
                } else {
                    scheduleEnabled.setSelected(true);
                    this.schedule.setText(schedule.getSchedule());
                    this.timeZone.setItem(schedule.getTimeZone());
                    this.ram.setText(schedule.getRam() != null ? schedule.getRam().toString() : "");
                    this.disk.setText(schedule.getDisk() != null ? schedule.getDisk().toString() : "");
                }
            } else {
                clearForm();
            }
            updateState();
        });
    }

    private void clearForm(){
        scheduleEnabled.setSelected(false);
        this.schedule.setText("");
        this.timeZone.setItem(null);
        this.ram.setText("");
        this.disk.setText("");
    }

    private void updateState() {
        scheduleEnabled.setEnabled(currentVariation != null);
        this.schedule.setEnabled(currentVariation != null && scheduleEnabled.isSelected());
        this.timeZone.setEnabled(currentVariation != null && scheduleEnabled.isSelected());
        this.ram.setEnabled(currentVariation != null && scheduleEnabled.isSelected());
        this.disk.setEnabled(currentVariation != null && scheduleEnabled.isSelected());
    }

    private void saveSchedule() {
        if (scheduleEnabled.isSelected()){
            String ram = this.ram.getText();
            String disk = this.disk.getText();
            currentVariation.setSchedule(new VariationInfo.Schedule(
                    this.schedule.getText(),
                    this.timeZone.getItem(),
                    StringUtils.isBlank(ram) ? null : Integer.valueOf(ram),
                    StringUtils.isBlank(disk) ? null : Integer.valueOf(disk)
            ));
        } else {
            currentVariation.setSchedule(null);
        }
        listeners.getProxy().documentChanged(new DocumentChangeEvent(this));
        updateState();
    }

}
