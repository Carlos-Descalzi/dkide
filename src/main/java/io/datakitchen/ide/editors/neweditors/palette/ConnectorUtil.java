package io.datakitchen.ide.editors.neweditors.palette;

import io.datakitchen.ide.model.Connector;
import io.datakitchen.ide.model.ConnectorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectorUtil {

    public static List<Connector> getConnectors(ComponentSource componentSource){
        List<Connector> connectors = new ArrayList<>();
        Map<String, Object> variables = componentSource.getAllVariables();

        for (Map.Entry<String, Object> entry:variables.entrySet()){
            Object content = entry.getValue();
            if (validVariable(content)){
                connectors.add(new Connector(entry.getKey(),(Map<String, Object>)entry.getValue()));
            }
        }
        return connectors;
    }

    private static boolean validVariable(Object value){
        if (!(value instanceof Map)){
            return false;
        }
        Map<String, Object> dictVariable = (Map<String, Object>)value;
        String schema = (String)dictVariable.get("_schema");
        return schema != null && ConnectorType.SCHEMAS.contains(schema);
    }
}
