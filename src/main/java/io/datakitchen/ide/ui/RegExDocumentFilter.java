package io.datakitchen.ide.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class RegExDocumentFilter extends DocumentFilter {

    private final String regExp;

    public RegExDocumentFilter(String regExp) {
        this.regExp = regExp;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        super.insertString(fb, offset, filter(string), attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        super.replace(fb, offset, length, filter(text), attrs);
    }

    private String filter(String string) {
        if (string != null) {
            return string.replaceAll(regExp, "");
        }
        return string;
    }
}
