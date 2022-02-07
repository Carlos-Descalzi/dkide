package io.datakitchen.ide.tools;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DataSourceResultsView extends BaseResultsView {

    private final Map<String, String> fileFormats = new HashMap<>();

    public DataSourceResultsView(Project project, VirtualFile folder) {
        super(project, folder);
    }
    protected void onRefresh(){
        loadDataFileTypes();
    }

    private void loadDataFileTypes() {
        fileFormats.clear();
        File compiledDsFile = new File(folder.getPath(),"compiled.json");
        if (compiledDsFile.exists()){
            try (InputStream input = new FileInputStream(compiledDsFile)){
                Map<String,Object> ds = JsonUtil.read(input);
                Map<String,Object> manifest = ObjectUtil.cast(ds.get("file_manifest_dict"));
                Map<String,Object> keys = ObjectUtil.cast (manifest.get("keys"));

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
                    String name = (String) manifest.get("name");
                    if (format != null) {
                        fileFormats.put(name + "." + key + ".output", format);
                    }
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    protected String getFileFormat(File file){
        return fileFormats.get(file.getName());
    }

}
