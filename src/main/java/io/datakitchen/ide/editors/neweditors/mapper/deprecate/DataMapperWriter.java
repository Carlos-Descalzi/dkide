package io.datakitchen.ide.editors.neweditors.mapper.deprecate;

public class DataMapperWriter {
//    private DataMapperModelImpl model;
//
//    public DataMapperWriter(DataMapperModelImpl model){
//        this.model = model;
//    }
//
//    public void write(VirtualFile nodeFolder) throws IOException {
//
//        VirtualFile notebookFile = nodeFolder.findChild("notebook.json");
//        VirtualFile dataSourcesFolder = nodeFolder.findChild("data_sources");
//        VirtualFile dataSinksFolder = nodeFolder.findChild("data_sinks");
//
//        JsonUtil.write(buildNotebook(), notebookFile);
//
//        for (VirtualFile f:dataSourcesFolder.getChildren()){
//            f.delete(this);
//        }
//
//        for (ConnectorInstance connector:model.getDataSourceConnectors()){
//            VirtualFile dataSourceFile = dataSourcesFolder.createChildData(this,connector.getName()+".json");
//            Map<String,Object> connectorJson = connector.toJson(true);
//
//            if (connector.getConnector().getConnectorType().getNature() == ConnectorNature.SQL) {
//                adjustDumpType(connector.getName(), connectorJson);
//            }
//            JsonUtil.write(connectorJson, dataSourceFile);
//        }
//
//        for (VirtualFile f:dataSinksFolder.getChildren()){
//            f.delete(this);
//        }
//        for (ConnectorInstance connector:model.getDataSinkConnectors()){
//            VirtualFile dataSinkFile = dataSinksFolder.createChildData(this,connector.getName()+".json");
//            Map<String, Object> connectorJson = connector.toJson(false);
//
//            JsonUtil.write(connectorJson, dataSinkFile);
//        }
//    }
//
//    private void adjustDumpType(String connectorName, Map<String, Object> connectorJson) {
//        Map<String, Object> keys = (Map<String, Object>) connectorJson.get("keys");
//
//        for (Map.Entry<String, Object> entry: keys.entrySet()){
//            String key = entry.getKey();
//            Map<String, Object> keyData = (Map<String, Object>) entry.getValue();
//
//            Mapping mapping = findMapping(connectorName, key);
//
//            if (mapping != null){
//                DumpType dumpType = mapping.getDumpType();
//                if (dumpType != DumpType.COPY) {
//                    keyData.put("format", dumpType.getIdentifier());
//                }
//            }
//        }
//    }
//
//    private Mapping findMapping(String connectorName, String key) {
//        for (Mapping mapping : model.getMappings()){
//            if (mapping.getSourceName().equals(connectorName)
//                && mapping.getSourceFile().equals(key)){
//                return mapping;
//            }
//        }
//        return null;
//    }
//
//    private Map<String, Object> buildNotebook() {
//        Map<String, Object> notebook = new LinkedHashMap<>();
//        notebook.put("name","mappings");
//        notebook.put("mappings",makeMappings());
//        notebook.put("tests",makeNodeTests());
//
//        return notebook;
//    }
//
//    private Map<String, Object> makeMappings(){
//        Map<String, Object> mappingsMap = new LinkedHashMap<>();
//
//        List<Mapping> mappings = new ArrayList<>(model.getMappings());
//
//        for (int i=0;i<mappings.size();i++){
//            Mapping mapping = mappings.get(i);
//
//            mappingsMap.put("mapping_"+(i+1), mappingToMap(mapping));
//        }
//
//        return mappingsMap;
//    }
//
//    private Map<String, Object> mappingToMap(Mapping mapping) {
//        Map<String, Object> map = new LinkedHashMap<>();
//
//        map.put("source-name",mapping.getSourceName());
//        map.put("source-key",mapping.getSourceFile());
//        map.put("sink-name",mapping.getSinkName());
//        map.put("sink-key",mapping.getSinkFile());
//
//        return map;
//
//    }
//
//    private Map<String, Object> makeNodeTests() {
//        Map<String, Object> tests = new LinkedHashMap<>();
//        return tests;
//    }

}
