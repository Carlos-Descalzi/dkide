package io.datakitchen.ide.model;

public enum DumpType {
    CSV("csv","csv","CSV"),
    BINARY("binary","pickle", "Binary"),
    JSON("json", "json", "JSON"),
    COPY;

    private String identifier;
    private String extension;
    private String description;

    DumpType(){}

    DumpType(String identifier, String extension, String description){
        this.identifier = identifier;
        this.extension = extension;
        this.description = description;
    }

    public static DumpType[] forNature(ConnectorNature nature) {
        switch (nature){
            case SQL:
                return new DumpType[]{CSV,BINARY,JSON};
            default:
                return new DumpType[]{COPY};
        }
    }

    public static DumpType fromExtension(String extension) {
        for (DumpType dumpType:values()){
            if (extension.equals(dumpType.getExtension())){
                return dumpType;
            }
        }
        return null;
    }

    public static DumpType forType(String type) {
        for (DumpType dumpType:values()){
            if (type.equals(dumpType.getIdentifier())){
                return dumpType;
            }
        }
        return null;
    }

    public String getExtension(){
        return extension;
    }

    public String getIdentifier(){
        return identifier;
    }

    public String getDescription() {
        return description;
    }
}
