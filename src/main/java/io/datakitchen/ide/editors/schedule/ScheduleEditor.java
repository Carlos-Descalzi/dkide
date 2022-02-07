package io.datakitchen.ide.editors.schedule;

import com.intellij.openapi.ui.ComboBox;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.RegExValidatedField;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScheduleEditor extends FormPanel {
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final JTextField schedule = new JTextField();
    private final ComboBox<String> timeZone = new ComboBox<>();
    private final RegExValidatedField maxRam = new RegExValidatedField(RegExValidatedField.NUMBER);
    private final RegExValidatedField maxDisk = new RegExValidatedField(RegExValidatedField.NUMBER);

    private final FieldListener listener = new FieldListener(this::scheduleChanged);

    public ScheduleEditor(){

        addField("Schedule",schedule,new Dimension(300,28));
        addField("Timezone",timeZone,new Dimension(300,28));

        List<String> timezones = new ArrayList<>(ZoneId.getAvailableZoneIds());
        timezones.sort(Comparator.naturalOrder());
        timezones.add(0, null);
        timeZone.setModel(new DefaultComboBoxModel<>(timezones.toArray(String[]::new)));

        addField("Max. RAM",maxRam,new Dimension(100,28));
        addField("Max. Disk",maxDisk,new Dimension(100,28));

        listener.listen(schedule);
        listener.listen(timeZone);
        listener.listen(maxRam);
        listener.listen(maxDisk);
    }

    private void enableEvents(){
        listener.setEnabled(true);
    }

    private void disableEvents(){
        listener.setEnabled(false);
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }
    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }

    private void scheduleChanged(){
        validateInput();
        this.eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    private void validateInput() {

    }

    public void loadSchedule(ScheduleItem currentSchedule) {
        disableEvents();
        if (currentSchedule != null) {
            String schedule = (String) currentSchedule.getValue().get("schedule");
            if (schedule != null) {
                this.schedule.setText(schedule);
            } else {
                this.schedule.setText("");
            }
            String timezone = (String) currentSchedule.getValue().get("scheduleTimeZone");
            if (!StringUtils.isBlank((timezone))) {
                this.timeZone.setSelectedItem(timezone);
            } else {
                this.timeZone.setSelectedItem(null);
            }
            Integer maxRam = (Integer) currentSchedule.getValue().get("max-ram");
            if (maxRam != null) {
                this.maxRam.setText(String.valueOf(maxRam));
            } else {
                this.maxRam.setText("");
            }
            Integer maxDisk = (Integer) currentSchedule.getValue().get("max-disk");
            if (maxDisk != null) {
                this.maxDisk.setText(String.valueOf(maxDisk));
            } else {
                this.maxDisk.setText("");
            }
        } else {
            schedule.setText("");
            timeZone.setSelectedItem(null);
            maxRam.setText("");
            maxDisk.setText("");
        }
        enableEvents();
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        schedule.setEnabled(enabled);
        timeZone.setEnabled(enabled);
        maxRam.setEnabled(enabled);
        maxDisk.setEnabled(enabled);
    }

    public void saveSchedule(ScheduleItem currentSchedule) {
        currentSchedule.getValue().put("schedule",schedule.getText());
        String timeZone = this.timeZone.getItem();
        if (timeZone == null){
            currentSchedule.getValue().remove("scheduleTimeZone");
        } else {
            currentSchedule.getValue().put("scheduleTimeZone", timeZone);
        }
        String maxRam = this.maxRam.getText();
        String maxDisk = this.maxDisk.getText();
        if (StringUtils.isNotBlank(maxRam)){
            currentSchedule.getValue().put("max-ram", Integer.parseInt(maxRam));
        } else {
            currentSchedule.getValue().remove("max-ram");
        }
        if (StringUtils.isNotBlank(maxDisk)){
            currentSchedule.getValue().put("max-disk", Integer.parseInt(maxDisk));
        } else {
            currentSchedule.getValue().remove("max-disk");
        }
    }
}
