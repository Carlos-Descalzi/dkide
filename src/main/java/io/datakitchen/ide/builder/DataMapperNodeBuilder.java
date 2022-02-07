package io.datakitchen.ide.builder;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import net.minidev.json.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataMapperNodeBuilder {
    private final Module module;
    private String nodeName;
    private DataSourceType dsType;
    private Connector connector;
    private Connector sinkConnector;
    private DataSinkType sinkType;
    private File sqlFile;

    public DataMapperNodeBuilder(Module module){
        this.module = module;
    }

    public DataMapperNodeBuilder setNodeName(String nodeName){
        this.nodeName = nodeName;
        return this;
    }

    public DataMapperNodeBuilder setDsType(DataSourceType dsType){
        this.dsType = dsType;
        return this;
    }

    public DataMapperNodeBuilder setSqlFile(File sqlFile){
        this.sqlFile = sqlFile;
        return this;
    }

    public DataMapperNodeBuilder setConnector(Connector connector) {
        this.connector = connector;
        return this;
    }

    public DataMapperNodeBuilder setSinkConnector(Connector sinkConnector) {
        this.sinkConnector = sinkConnector;
        return this;
    }

    public DataMapperNodeBuilder setSinkType(DataSinkType sinkType) {
        this.sinkType = sinkType;
        return this;
    }

    public void build(Runnable onFinish){
        new NodeBuilder(module.getProject())
            .setNodeType(NodeType.DATA_MAPPER_NODE.getTypeName())
            .setModule(module)
            .setNodeName(nodeName)
            .build((VirtualFile nodeFolder)->{
                try {
                    KeyReference sourceKey = buildDataSource(nodeFolder);
                    KeyReference sinkKey = buildDataSink(nodeFolder);
                    updateNotebook(nodeFolder, sourceKey, sinkKey);
                    if (onFinish != null) {
                        onFinish.run();
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            });
    }

    private void updateNotebook(VirtualFile nodeFolder, KeyReference sourceKey, KeyReference sinkKey) throws IOException, ParseException {
        VirtualFile notebookJson = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);
        Map<String, Object> notebook = JsonUtil.read(notebookJson);

        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("source-name",sourceKey.connector);
        mapping.put("source-key", sourceKey.key);
        mapping.put("sink-name",sinkKey.connector);
        mapping.put("sink-key",sinkKey.key);

        Map<String, Object> mappings = new LinkedHashMap<>();
        mappings.put("mapping-1", mapping);

        notebook.put("mappings", mappings);
        JsonUtil.write(notebook, notebookJson);
    }

    private KeyReference buildDataSource(VirtualFile nodeFolder) throws IOException, ParseException {
        VirtualFile targetFolder;

        String folderName = RecipeUtil.getDataSourcesFolderNameForNode(nodeFolder);
        targetFolder = nodeFolder.findChild(folderName);
        if (targetFolder == null){
            targetFolder = nodeFolder.createChildDirectory(this, folderName);
        }

        if (dsType == null){
            dsType = DataSourceType.forConnectorType(connector.getConnectorType());
        }

        Map<String,Object> template = JsonUtil.read(getClass().getResource("/templates/datasources/" + dsType.getTypeName() + ".json"));

        VirtualFile dsFile = targetFolder.createChildData(this,"source.json");

        template.put("name","source");

        if (connector != null){
            template.remove("config");
            template.put("config-ref",connector.getName());
        }

        Map<String, Object> keys = new LinkedHashMap<>();
        template.put("keys",keys);

        Map<String,Object> key1 = new LinkedHashMap<>();
        String keyName = "run-"+sqlFile.getName().toLowerCase().replace(".","-");
        keys.put(keyName, key1);
        key1.put("sql-file", sqlFile.getName());
        key1.put("query-type", "execute_query");
        key1.put("format", "csv");

        JsonUtil.write(template, dsFile);
        targetFolder.refresh(true,true);

        VirtualFile resourcesFolder = RecipeUtil.getResourcesFolder(module);

        VirtualFile vFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(sqlFile.toURI()));
        VfsUtil.copyFile(this, vFile,resourcesFolder);

        return new KeyReference("source", keyName);
    }

    private KeyReference buildDataSink(VirtualFile nodeFolder) throws IOException, ParseException {
        VirtualFile targetFolder;

        String folderName = RecipeUtil.getDataSinksFolderNameForNode(nodeFolder);
        targetFolder = nodeFolder.findChild(folderName);
        if (targetFolder == null){
            targetFolder = nodeFolder.createChildDirectory(this, folderName);
        }

        if (sinkType == null){
            sinkType = DataSinkType.forConnectorType(sinkConnector.getConnectorType());
        }

        Map<String,Object> template = JsonUtil.read(getClass().getResource("/templates/datasinks/" + sinkType.getTypeName() + ".json"));

        VirtualFile dsFile = targetFolder.createChildData(this,"sink.json");

        template.put("name","sink");

        if (sinkConnector != null){
            template.remove("config");
            template.put("config-ref",sinkConnector.getName());
        }

        Map<String, Object> keys = new LinkedHashMap<>();
        template.put("keys",keys);

        Map<String,Object> key1 = new LinkedHashMap<>();

        String keyName = "run-"+sqlFile.getName().toLowerCase().replace(".","-");

        keys.put(keyName, key1);

        if (sinkType.getConnectorType().getNature() == ConnectorNature.SQL) {
            key1.put("table-name", "[CHANGE ME]");
            key1.put("query-type", "bulk_insert");
            key1.put("format", "csv");
        } else {
            String sinkFile = sqlFile.getName();
            sinkFile = sinkFile.substring(0, sinkFile.indexOf('.'))+ ".csv";
            key1.put("file-key", sinkFile);
            key1.put("use-only-file-key", true);
        }
        JsonUtil.write(template, dsFile);
        targetFolder.refresh(true,true);

        VirtualFile resourcesFolder = RecipeUtil.getResourcesFolder(module);

        VirtualFile vFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(sqlFile.toURI()));
        VfsUtil.copyFile(this, vFile,resourcesFolder);

        return new KeyReference("sink", keyName);
    }

    private static class KeyReference {
        private final String connector;
        private final String key;
        public KeyReference(String connector, String key){
            this.connector = connector;
            this.key = key;
        }
    }
}
