package io.datakitchen.ide.model;

import java.util.List;

public class NodeType {

    public static final String NOOP_NODE_TYPE_NAME = "DKNode_NoOp";
    public static final String ACTION_NODE_TYPE_NAME = "DKNode_Action";
    public static final String CONTAINER_NODE_TYPE_NAME = "DKNode_Container";
    public static final String DATA_MAPPER_NODE_TYPE_NAME = "DKNode_DataMapper";
    public static final String INGREDIENT_NODE_TYPE_NAME = "DKNode_Ingredient";

    public static final NodeType NOOP_NODE = new NodeType(NOOP_NODE_TYPE_NAME,"Synchronization node");
    public static final NodeType ACTION_NODE = new NodeType(ACTION_NODE_TYPE_NAME,null, "Action node","actions",null);
    public static final NodeType CONTAINER_NODE = new NodeType(CONTAINER_NODE_TYPE_NAME,null, "Container node", "data_sources","data_sinks", "docker-share");
    public static final NodeType DATA_MAPPER_NODE = new NodeType(DATA_MAPPER_NODE_TYPE_NAME,null, "Data mapper node","data_sources","data_sinks");
    public static final NodeType INGREDIENT_NODE = new NodeType(INGREDIENT_NODE_TYPE_NAME,"Ingredient node");
    public static final NodeType SCRIPT_NODE = new NodeType(CONTAINER_NODE_TYPE_NAME,"script","Script node", "data_sources","data_sinks", "docker-share","test-files");

    public static final List<NodeType> ALL_TYPES = List.of(
           NOOP_NODE,
           ACTION_NODE,
           DATA_MAPPER_NODE,
           INGREDIENT_NODE,
           CONTAINER_NODE,
           SCRIPT_NODE
        );

    public static NodeType getByTypeName(String typeName){
        return ALL_TYPES.stream()
            .filter(t -> t.getTypeName().equals(typeName))
                .findFirst().orElse(null);
    }

    private final String name;
    private final String subtype;
    private final String displayName;
    private final String dataSourcesFolder;
    private final String dataSinksFolder;
    private final String[] additionalFolders;

    private NodeType(String name, String displayName) {
        this(name, null, displayName, null, null);
    }

    private NodeType(String name, String subtype, String displayName, String dataSourcesFolder, String dataSinksFolder, String ... additionalFolders) {
        this.name = name;
        this.subtype = subtype;
        this.displayName = displayName;
        this.dataSourcesFolder = dataSourcesFolder;
        this.dataSinksFolder = dataSinksFolder;
        this.additionalFolders = additionalFolders;
    }

    public boolean equals(Object other){
        return other instanceof NodeType
                && getTypeName().equals(((NodeType)other).getTypeName());
    }

    public int hashCode(){
        return getTypeName().hashCode();
    }

    public String getName(){
        return name;
    }

    public String getSubtype(){
        return subtype;
    }

    public String toString() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDataSourcesFolder() {
        return dataSourcesFolder;
    }

    public String getDataSinksFolder() {
        return dataSinksFolder;
    }

    public String[] getAdditionalFolders(){
        return this.additionalFolders;
    }

    public String getTypeName() {
        return name
            + (subtype != null ? "." + subtype : "")
            ;
    }
}
