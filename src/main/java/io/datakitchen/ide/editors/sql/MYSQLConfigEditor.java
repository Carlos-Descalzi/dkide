package io.datakitchen.ide.editors.sql;

import com.intellij.openapi.module.Module;
import io.datakitchen.ide.ui.EntryField;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.util.Map;

public class MYSQLConfigEditor extends FormPanel {
    private final EntryField username;
    private final EntryField password;
    private final EntryField hostname;
    private final EntryField database;

    public MYSQLConfigEditor(Module module){
        super(new Dimension(300,28));
        username = new EntryField(module);
        password = new EntryField(module);
        hostname = new EntryField(module);
        database = new EntryField(module);

        addField("Username", username);
        addField("Password", password);
        addField("Hostname", hostname);
        addField("Database", database);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
        hostname.setEnabled(enabled);
        database.setEnabled(enabled);
    }

    public void loadConfiguration(Map<String, Object> config) {
        username.setText(StringUtils.defaultString((String)config.get("username"),""));
        password.setText(StringUtils.defaultString((String)config.get("password"),""));
        hostname.setText(StringUtils.defaultString((String)config.get("hostname"),""));
        database.setText(StringUtils.defaultString((String)config.get("database"),""));
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("username", username.getText());
        config.put("password", password.getText());
        config.put("hostname", hostname.getText());
        config.put("database", database.getText());
    }

}
