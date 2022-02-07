package io.datakitchen.ide.dialogs;

import com.intellij.openapi.module.Module;

public class NewDataSourceDialog extends AbstractDataDialog {

    public NewDataSourceDialog(Module module) {
        super(module);
        setTitle("New Data Source");
    }

    protected String getTemplatesFolder(){
        return "/templates/datasources";
    }

    @Override
    protected String getPrefix() {
        return "DKDataSource";
    }

}
