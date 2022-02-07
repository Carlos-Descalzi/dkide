package io.datakitchen.ide.editors.sql;

import com.intellij.openapi.module.Module;
import io.datakitchen.ide.ui.EntryField;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.util.Map;

public class BigQueryConfigEditor extends FormPanel {

    private EntryField serviceAccount;
    private EntryField privateKey;
    private EntryField projectId;

    public BigQueryConfigEditor(Module module){
        super(new Dimension(300,28));

        serviceAccount = new EntryField(module);
        privateKey = new EntryField(module);
        projectId = new EntryField(module);

        addField("Service account",serviceAccount);
        addField("Private key",privateKey);
        addField("Project ID",projectId);
    }

    public void setEnabled(boolean enabled){
        serviceAccount.setEnabled(enabled);
        privateKey.setEnabled(enabled);
        projectId.setEnabled(enabled);
    }

    public void loadConfiguration(Map<String, Object> config) {
        serviceAccount.setText(StringUtils.defaultString((String)config.get("service-account"),""));
        privateKey.setText(StringUtils.defaultString((String)config.get("private-key"),""));
        projectId.setText(StringUtils.defaultString((String)config.get("project-id"),""));
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("service-account",serviceAccount.getText());
        config.put("private-key",privateKey.getText());
        config.put("project-id",projectId.getText());
    }
}
