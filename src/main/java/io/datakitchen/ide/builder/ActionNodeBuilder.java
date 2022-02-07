package io.datakitchen.ide.builder;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.model.Connector;
import io.datakitchen.ide.model.DataSourceType;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ActionNodeBuilder {

    private final Module module;
    private String nodeName;
    private DataSourceType dsType;
    private Connector connector;
    private File sqlFile;

    public ActionNodeBuilder(Module module){
        this.module = module;
    }

    public ActionNodeBuilder setNodeName(String nodeName){
        this.nodeName = nodeName;
        return this;
    }

    public ActionNodeBuilder setDsType(DataSourceType dsType){
        this.dsType = dsType;
        return this;
    }

    public ActionNodeBuilder setSqlFile(File sqlFile){
        this.sqlFile = sqlFile;
        return this;
    }

    public ActionNodeBuilder setConnector(Connector connector) {
        this.connector = connector;
        return this;
    }

    public void build(Runnable onFinish){
        new NodeBuilder(module.getProject())
                .setNodeType(NodeType.ACTION_NODE.getTypeName())
                .setModule(module)
                .setNodeName(nodeName)
                .build((VirtualFile nodeFolder)->{
                    buildDataSource(nodeFolder, onFinish);
                });
    }

    private void buildDataSource(VirtualFile nodeFolder, Runnable onFinish){
        try {
            VirtualFile targetFolder;

            String folderName = RecipeUtil.getDataSourcesFolderNameForNode(nodeFolder);
            assert folderName != null;
            targetFolder = nodeFolder.findChild(folderName);
            if (targetFolder == null){
                targetFolder = nodeFolder.createChildDirectory(this, folderName);
            }

            if (dsType == null){
                dsType = DataSourceType.forConnectorType(connector.getConnectorType());
            }

            Map<String,Object> template = JsonUtil.read(
                    Objects.requireNonNull(
                            getClass().getResource("/templates/datasources/" + dsType.getTypeName() + ".json")));

            VirtualFile dsFile = targetFolder.createChildData(this,"actions.json");

            template.put("name","actions");

            if (connector != null){
                template.remove("config");
                template.put("config-ref",connector.getName());
            }

            Map<String, Object> keys = new LinkedHashMap<>();
            template.put("keys",keys);

            Map<String,Object> key1 = new LinkedHashMap<>();
            keys.put("run-"+sqlFile.getName().toLowerCase().replace(".","-"), key1);
            key1.put("sql-file", sqlFile.getName());
            key1.put("query-type", "execute_non_query");
            key1.remove("format");

            JsonUtil.write(template, dsFile);
            targetFolder.refresh(true,true);

            VirtualFile resourcesFolder = RecipeUtil.getResourcesFolder(module);

            VirtualFile vFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(sqlFile.toURI()));
            if (vFile != null) {
                VfsUtil.copyFile(this, vFile, resourcesFolder);
            }

            nodeFolder.refresh(true,true,onFinish);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
