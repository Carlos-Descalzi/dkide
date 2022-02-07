package io.datakitchen.ide.editors.neweditors.mapper;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.neweditors.ModelWriterUtil;
import io.datakitchen.ide.model.Connection;
import io.datakitchen.ide.model.Key;
import io.datakitchen.ide.model.Mapping;
import io.datakitchen.ide.util.JsonUtil;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DataMapperModelWriter {
    private final Project project;
    private final DataMapperModelImpl model;

    public DataMapperModelWriter(Project project, DataMapperModelImpl model){
        this.project = project;
        this.model = model;
    }

    public void write(VirtualFile nodeFolder) throws IOException {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                writeDescription(nodeFolder);
                writeNotebook(nodeFolder);
                writeDataSources(nodeFolder);
                writeDataSinks(nodeFolder);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    private void writeDescription(VirtualFile nodeFolder) throws IOException, ParseException {
        VirtualFile descriptionFile = nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);
        assert descriptionFile != null;
        Map<String, Object> descriptionJson = JsonUtil.read(descriptionFile);
        descriptionJson.put("description", model.getDescription());
        JsonUtil.write(descriptionJson, descriptionFile);
    }

    private void writeDataSinks(VirtualFile nodeFolder) throws IOException{
        VirtualFile dataSinksFolder = nodeFolder.findChild("data_sinks");
        assert dataSinksFolder != null;
        for (VirtualFile file: dataSinksFolder.getChildren()){
            file.delete(this);
        }
        for (Connection connection : model.getDataSinks().getConnections()){
            ModelWriterUtil.writeDataSink(dataSinksFolder, connection);
        }
    }

    private void writeDataSources(VirtualFile nodeFolder) throws IOException{
        VirtualFile dataSourcesFolder = nodeFolder.findChild("data_sources");
        assert dataSourcesFolder != null;
        for (VirtualFile file: dataSourcesFolder.getChildren()){
            file.delete(this);
        }
        for (Connection connection : model.getDataSources().getConnections()){
            ModelWriterUtil.writeDataSource(dataSourcesFolder, connection);
        }
    }

    private void writeNotebook(VirtualFile nodeFolder) throws IOException{
        VirtualFile notebookFile = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);

        Map<String, Object> notebookJson = new LinkedHashMap<>();
        notebookJson.put("name", "mappings");

        Map<String, Object> mappings = new LinkedHashMap<>();
        int i=1;

        Set<Mapping> mappingSet = model.getMappings().stream().filter(m -> !isWildcard(m)).collect(Collectors.toSet());

        for (Mapping mapping: mappingSet){
            if (!isWildcard(mapping)) {
                mappings.put("mapping-" + (i++), makeMapping(mapping));
            }
        }

        notebookJson.put("mappings", mappings);

        Set<Mapping> wildcardMappings = model.getMappings()
            .stream().filter(this::isWildcard)
            .collect(Collectors.toSet());

        if (!wildcardMappings.isEmpty()){
            List<Map<String,String>> wildcardMapping = new ArrayList<>();
            for (Mapping mapping: wildcardMappings) {
                Map<String, String> mappingJson = new LinkedHashMap<>();
                mappingJson.put("data-source", mapping.getSourceKey().getConnection().getName());
                mappingJson.put("data-sink", mapping.getSinkKey().getConnection().getName());
                wildcardMapping.add(mappingJson);
            }
            notebookJson.put("wildcard-will-automatically-create-mappings", wildcardMapping);
        }

        JsonUtil.write(notebookJson, notebookFile);
    }

    private boolean isWildcard(Mapping mapping) {
        Key sourceKey = mapping.getSourceKey();
        return sourceKey instanceof DataSourceFileKey
                && ((DataSourceFileKey)sourceKey).isWildcard();
    }

    private Map<String, Object> makeMapping(Mapping mapping) {
        Map<String, Object> mappingJson = new LinkedHashMap<>();
        mappingJson.put("source-name", mapping.getSourceKey().getConnection().getName());
        mappingJson.put("source-key", mapping.getSourceKey().getName());
        mappingJson.put("sink-name", mapping.getSinkKey().getConnection().getName());
        mappingJson.put("sink-key", mapping.getSinkKey().getName());
        return mappingJson;
    }
}
