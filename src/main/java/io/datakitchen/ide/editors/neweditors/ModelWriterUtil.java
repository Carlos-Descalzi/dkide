package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.util.JsonUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelWriterUtil {

    public static void writeDataSource(VirtualFile dataSourcesFolder, Connection connectionImpl) throws IOException {
        VirtualFile dsFile = dataSourcesFolder.createChildData(connectionImpl, connectionImpl.getName()+".json");

        Map<String, Object> jsonData = new LinkedHashMap<>();

        jsonData.put("type", DataSourceType.forConnectorType(connectionImpl.getConnector().getConnectorType()).getTypeName());
        jsonData.put("name", connectionImpl.getName());
        jsonData.put("config-ref", connectionImpl.getConnector().getName());

        Set<Key> allKeys = connectionImpl.getKeys();

        Set<Key> nonWildcardKeys = allKeys.stream()
                .filter(key -> !(key instanceof FileKey) || !((FileKey)key).isWildcard())
                .collect(Collectors.toSet());

        Set<Key> wildcardKeys = allKeys.stream()
                .filter(key -> (key instanceof FileKey) && ((FileKey)key).isWildcard())
                .collect(Collectors.toSet());

        if (!wildcardKeys.isEmpty()){
            // 1 as much
            FileKey fileKey = (FileKey) wildcardKeys.iterator().next();
            WildcardExpression wildcard = WildcardExpression.fromString(fileKey.getFile());

            jsonData.put("wildcard-key-prefix", wildcard.getPrefix());
            jsonData.put("wildcard", wildcard.getPattern());

        }

        Map<String, Object> keys = new LinkedHashMap<>();
        jsonData.put("keys", keys);

        for (Key key: nonWildcardKeys){
            keys.put(key.getName(), key.toJson());
        }

        if (!connectionImpl.getVariables().isEmpty()){
            Map<String, Object> variablesJson = new LinkedHashMap<>();
            for (RuntimeVariable variable: connectionImpl.getVariables()){
                variablesJson.put(variable.getAttribute().getName(), variable.getVariableName());
            }
            jsonData.put("set-runtime-vars", variablesJson);

        }

        Map<String, Object> tests = new LinkedHashMap<>();
        jsonData.put("tests", tests);

        int i=1;
        for (Test test: connectionImpl.getTests()){
            tests.put("test-"+(i++),test.toJson());
        }

        JsonUtil.write(jsonData, dsFile);
    }

    public static void writeDataSink(VirtualFile dataSinksFolder, Connection connectionImpl) throws IOException{
        VirtualFile dsFile = dataSinksFolder.createChildData(connectionImpl, connectionImpl.getName()+".json");

        Map<String, Object> jsonData = new LinkedHashMap<>();

        jsonData.put("type", DataSinkType.forConnectorType(connectionImpl.getConnector().getConnectorType()).getTypeName());
        jsonData.put("name", connectionImpl.getName());
        jsonData.put("config-ref", connectionImpl.getConnector().getName());

        Set<Key> allKeys = connectionImpl.getKeys();

        Set<Key> nonWildcardKeys = allKeys.stream()
                .filter(key -> !(key instanceof FileKey) || !((FileKey)key).isWildcard())
                .collect(Collectors.toSet());

        Set<Key> wildcardKeys = allKeys.stream()
                .filter(key -> (key instanceof FileKey) && ((FileKey)key).isWildcard())
                .collect(Collectors.toSet());

        if (!wildcardKeys.isEmpty()){
            FileKey fileKey = (FileKey) wildcardKeys.iterator().next();
            WildcardExpression wildcard = WildcardExpression.fromString(fileKey.getFile());

            jsonData.put("wildcard-key-prefix", wildcard.getPrefix());
            jsonData.put("wildcard", wildcard.getPattern());
        }

        Map<String, Object> keys = new LinkedHashMap<>();
        jsonData.put("keys", keys);

        for (Key key: nonWildcardKeys){
            keys.put(key.getName(), key.toJson());
        }

        if (!connectionImpl.getVariables().isEmpty()){
            Map<String, Object> variablesJson = new LinkedHashMap<>();
            for (RuntimeVariable variable: connectionImpl.getVariables()){
                variablesJson.put(variable.getAttribute().getName(), variable.getVariableName());
            }
            jsonData.put("set-runtime-vars", variablesJson);
        }

        Map<String, Object> tests = new LinkedHashMap<>();
        jsonData.put("tests", tests);

        int i=1;
        for (Test test: connectionImpl.getTests()){
            tests.put("test-"+(i++),test.toJson());
        }

        JsonUtil.write(jsonData, dsFile);
    }

}
