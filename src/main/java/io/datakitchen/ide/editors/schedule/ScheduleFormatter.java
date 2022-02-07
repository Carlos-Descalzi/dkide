package io.datakitchen.ide.editors.schedule;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class ScheduleFormatter extends JFormattedTextField.AbstractFormatter {
    @Override
    public Object stringToValue(String text) {
        return text;
    }

    @Override
    public String valueToString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    @Override
    protected DocumentFilter getDocumentFilter() {
        return new DocumentFilter(){
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                string = string.replaceAll("^[ 0-9\\*/]*","");
                super.insertString(fb, offset, string, attr);
            }
        };
    }

}
