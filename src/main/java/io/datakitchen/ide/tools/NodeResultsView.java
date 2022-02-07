package io.datakitchen.ide.tools;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NodeResultsView extends BaseResultsView {

    private final VirtualFile outputFolder;
    private final VirtualFile nodeFolder;
    private JEditorPane textArea = new JEditorPane();
    private Map<String, String> outputTypes = new HashMap<>();

    public NodeResultsView(Project project, VirtualFile nodeFolder, VirtualFile outputFolder){
        super(project, outputFolder);
        this.outputFolder = outputFolder;
        this.nodeFolder = nodeFolder;

        contentPane.add(new JBScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        textArea.setFont(new Font("Monospaced",Font.PLAIN,12));

        refresh();
    }


    @Override
    protected void onRefresh() {
        try {
            Module module = ModuleUtil.findModuleForFile(nodeFolder, project);
            String dataSourcesFolderName = RecipeUtil.getDataSourcesFolderNameForNode(nodeFolder);
            if (dataSourcesFolderName != null) {
                VirtualFile folder = nodeFolder.findChild(dataSourcesFolderName);

                if (folder != null) {
                    for (VirtualFile dsFile : folder.getChildren()) {
                        if (dsFile.getName().endsWith(".json")) {
                            outputTypes.putAll(getOutputTypes(dsFile));
                        }
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private Map<String, String> getOutputTypes(VirtualFile dsFile) {
        Map<String, String> outputTypes = new HashMap<>();
        try {
            Map<String,Object> ds = JsonUtil.read(dsFile);
            Map<String,Object> keys = ObjectUtil.cast(ds.get("keys"));
            for (Map.Entry<String, Object> entry: keys.entrySet()){
                String key = entry.getKey();
                Map<String, Object> keyData = ObjectUtil.cast(entry.getValue());

                String fileKey = (String)keyData.get("file-key");

                String format;
                if (fileKey != null){
                    format = OutputFormats.getByExtension(fileKey);
                } else {
                    format = StringUtils.defaultString((String) keyData.get("format"), OutputFormats.BINARY);
                }

                String name = (String)ds.get("name");
                if (format != null) {
                    outputTypes.put(name + "." + key + ".output", format);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return outputTypes;
    }

    @Override
    protected String getFileFormat(File file) {
        return outputTypes.get(file.getName());
    }
}
