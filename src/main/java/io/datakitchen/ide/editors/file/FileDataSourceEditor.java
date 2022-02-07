package io.datakitchen.ide.editors.file;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.util.Map;

public abstract class FileDataSourceEditor extends FileDataEditor{

    private JTextField wildcardKeyPrefix;
    private JTextField wildcardExpression;

    public FileDataSourceEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    protected JPanel buildWildcardPanel(){
        FormPanel wildcardPanel = new FormPanel();

        wildcardKeyPrefix = new JTextField();
        wildcardExpression = new JTextField();

        wildcardPanel.addField("Wildcard path prefix", wildcardKeyPrefix);
        wildcardPanel.addField("Wildcard expression", wildcardExpression);

        return wildcardPanel;
    }

    protected void loadWildcardSettings(Map<String, Object> document){
        wildcardKeyPrefix.setText((String)document.getOrDefault("wildcard-key-prefix",""));
        wildcardExpression.setText((String)document.getOrDefault("wildcard",""));
    }
    protected void saveWildcardSettings(Map<String, Object> document){
        String wildcardKeyPrefix = this.wildcardKeyPrefix.getText();
        if (StringUtils.isNotBlank(wildcardKeyPrefix)){
            document.put("wildcard-key-prefix",wildcardKeyPrefix);
        } else {
            document.remove("wildcard-key-prefix");
        }
        String wildcardExpression = this.wildcardExpression.getText();
        if (StringUtils.isNotBlank(wildcardExpression)){
            document.put("wildcard",wildcardExpression);
        } else {
            document.remove("wildcard");
        }
    }


}
