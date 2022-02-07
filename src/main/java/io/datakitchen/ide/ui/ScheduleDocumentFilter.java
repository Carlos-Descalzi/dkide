package io.datakitchen.ide.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScheduleDocumentFilter extends DocumentFilter {
    private static final Map<String, Set<String>> VALID = new HashMap<>();
    static {
        VALID.put("*", Set.of(" ", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
        VALID.put(" ", Set.of("*", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
        VALID.put("/", Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
        VALID.put("number", Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", " "));
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        System.out.println("insert: "+offset+","+string);
        super.insertString(fb, offset, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (offset == 0 || validateReplace(fb, offset, text)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
    private boolean validateReplace(DocumentFilter.FilterBypass fb, int offset, String text) throws BadLocationException {
        String prevChar = fb.getDocument().getText(offset -1,1);
        Set<String> validInserts;
        if (Character.isDigit(prevChar.charAt(0))){
            validInserts = VALID.get("number");
        } else {
            validInserts = VALID.get(prevChar);
        }

        return validInserts.contains(text);
    }
}
