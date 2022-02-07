package io.datakitchen.ide.psi;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.PlainTextLanguage;

public class SqlLanguage extends Language {

    public static final SqlLanguage INSTANCE = new SqlLanguage();

    private SqlLanguage() {
        super(PlainTextLanguage.INSTANCE, "SQL", "application/sql");
    }
}
