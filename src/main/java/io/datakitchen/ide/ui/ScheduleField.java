package io.datakitchen.ide.ui;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DocumentFilter;

public class ScheduleField extends JFormattedTextField {
    private static final ScheduleDocumentFilter FILTER = new ScheduleDocumentFilter();

    public ScheduleField(){
        super(new DefaultFormatter(){
            @Override
            protected DocumentFilter getDocumentFilter() {
                return FILTER;
            }
        });
    }

}
