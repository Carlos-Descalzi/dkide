package io.datakitchen.ide.editors.neweditors.mapper.deprecate;

public class DataMapperReader {
//    private final Project project;
//    private final DataMapperModelImpl model;
//    public DataMapperReader(Project project, DataMapperModelImpl model){
//        this.project = project;
//        this.model = model;
//    }
//
//    public void read(VirtualFile nodeFolder) throws IOException, ParseException {
//
//        List<Connector> connectors = Connector.fromVariables(
//                RecipeUtil.loadAllVariables(
//                        ModuleUtil.findModuleForFile(nodeFolder, project)
//                )
//        );
//        Map<String, Connector> connectorMap = connectors
//                .stream()
//                .collect(Collectors.toMap(Connector::getName, (Connector c)->c));
//
//        VirtualFile notebookFile = nodeFolder.findChild("notebook.json");
//        VirtualFile dataSourcesFolder = nodeFolder.findChild("data_sources");
//        VirtualFile dataSinksFolder = nodeFolder.findChild("data_sinks");
//        readDataSources(dataSourcesFolder, connectorMap);
//        readDataSinks(dataSinksFolder, connectorMap);
//
//        readNotebook(notebookFile);
//
//        model.sortConnections();
//    }
//
//    private void readDataSources(VirtualFile dataSourcesFolder, Map<String, Connector> connectors) throws IOException, ParseException {
//        for (VirtualFile file: dataSourcesFolder.getChildren()){
//            model.getDataSourceConnectors().add(ConnectorInstance.fromFile(file, connectors));
//        }
//    }
//
//    private void readDataSinks(VirtualFile dataSinksFolder, Map<String, Connector> connectors) throws IOException, ParseException {
//        for (VirtualFile file: dataSinksFolder.getChildren()){
//            model.getDataSinkConnectors().add(ConnectorInstance.fromFile(file, connectors));
//        }
//    }
//
//    private void readNotebook(VirtualFile notebookFile) throws IOException, ParseException{
//        Map<String, Object> notebookJson = JsonUtil.read(notebookFile);
//
//        Map<String, Object> mappings = (Map<String, Object>) notebookJson.get("mappings");
//
//        for (Map.Entry<String, Object> entry: mappings.entrySet()){
//            Map<String, Object> mapping = (Map<String, Object>) entry.getValue();
//
//            String sourceName = (String)mapping.get("source-name");
//            String sinkName = (String)mapping.get("sink-name");
//            String sourceKey = (String)mapping.get("source-key");
//            String sinkKey = (String)mapping.get("sink-key");
//
//            ConnectorInstance source = model.getDataSourceConnector(sourceName);
//            ConnectorInstance sink = model.getDataSinkConnector(sinkName);
//
//            ConnectorKey sourceConnKey = source.getKey(sourceKey);
//
//            DumpType dumpType;
//
//            if (sourceConnKey instanceof FileConnectorKey){
//                dumpType = DumpType.COPY;
//            } else {
//                dumpType = DumpType.forType(((SQLConnectorKey)sourceConnKey).getFormat());
//                if (dumpType == null){
//                    dumpType = DumpType.CSV;
//                }
//            }
//
//            model.addMapping(source.getConnector(), sourceKey, sink.getConnector(), sinkKey, dumpType);
//        }
//    }

}
