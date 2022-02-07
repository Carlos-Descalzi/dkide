package io.datakitchen.ide.ui;

import javax.swing.*;
import javax.swing.text.PlainDocument;

public class RegExValidatedField extends JTextField {

    public static final String IDENTIFIER = "[^a-zA-Z0-9\\-_]+";
    public static final String NUMBER = "[^0-9']+";

    public RegExValidatedField(final String regExp){

        ((PlainDocument)getDocument()).setDocumentFilter(new RegExDocumentFilter(regExp));
    }

}
