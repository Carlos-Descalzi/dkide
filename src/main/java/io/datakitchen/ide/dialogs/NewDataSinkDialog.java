package io.datakitchen.ide.dialogs;

import com.intellij.openapi.module.Module;

public class NewDataSinkDialog extends AbstractDataDialog {

    public NewDataSinkDialog(Module module) {
        super(module);
        setTitle("New Data Sink");
    }

    protected String getTemplatesFolder(){
        return "/templates/datasinks";
    }

    @Override
    protected String getPrefix() {
        return "DKDataSink";
    }

}
