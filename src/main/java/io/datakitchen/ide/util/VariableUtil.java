package io.datakitchen.ide.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.json.CustomJsonParser;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class VariableUtil {
    public static Set<String> getAllDeclaredVariablesUpToNode(
            Module module,
            String nodeName,
            boolean includeOverrides
    ) throws IOException, ParseException {
        Set<String> result = new LinkedHashSet<>();

        String variationName = RecipeUtil.getActiveVariation(module);

        List<List<String>> graph = readGraph(module, variationName);

        Set<String> nodes = Set.of(nodeName);

        while(!nodes.isEmpty()){
            System.out.println("Collecting variables for nodes "+ nodes);
            for (String node: nodes){
                System.out.println("Collecting for node "+node);
                result.addAll(getDeclaredVariables(module, node));
            }
            Set<String> newNodes = new LinkedHashSet<>();
            for (String node: nodes){
                newNodes.addAll(getPreviousNodes(graph, node));
            }
            nodes = newNodes;

        }
        if (includeOverrides){
            result.addAll(readOverrides(module, variationName));
        }

        result = result.stream().filter(s -> !s.startsWith("testvar_")).collect(Collectors.toSet());

        return result;
    }

    private static Set<String> readOverrides(Module module, String variationName) throws IOException, ParseException {
        Set<String> result = new LinkedHashSet<>();
        VirtualFile variablesFile = RecipeUtil.recipeFolder(module).findChild("variables.json");
        if (variablesFile != null) {
            Map<String, Object> variablesJson = JsonUtil.read(variablesFile);
            Map<String, Object> variables = ObjectUtil.cast(variablesJson.get("variable-list"));

            result.addAll(variables.keySet());
        }
        return result;
    }

    private static Set<String> getPreviousNodes(List<List<String>> graph, String node) {
        Set<String> result = new LinkedHashSet<>();

        for (List<String> edge: graph){
            if (edge.size() >= 2 && edge.get(1).equals(node)){
                result.add(edge.get(0));
            }
        }

        return result;
    }

    private static Set<String> getDeclaredVariables(Module module, String node) throws IOException, ParseException {
        Set<String> result = new LinkedHashSet<>();

        VirtualFile nodeFolder = RecipeUtil.recipeFolder(module).findChild(node);

        if (nodeFolder != null) {
            String dataSourcesFolderName = RecipeUtil.getDataSourcesFolderNameForNode(nodeFolder);
            String dataSinksFolderName = RecipeUtil.getDataSinksFolderNameForNode(nodeFolder);

            VirtualFile notebookFile = nodeFolder.findChild("notebook.json");

            result.addAll(readRuntimeVariables(notebookFile));

            if (dataSourcesFolderName != null) {
                VirtualFile dsFolder = nodeFolder.findChild(dataSourcesFolderName);
                if (dsFolder != null) {
                    for (VirtualFile dsFile : dsFolder.getChildren()) {
                        result.addAll(readRuntimeVariables(dsFile));
                    }
                }
            }

            if (dataSinksFolderName != null) {
                VirtualFile dsFolder = nodeFolder.findChild(dataSinksFolderName);
                if (dsFolder != null) {
                    for (VirtualFile dsFile : dsFolder.getChildren()) {
                        result.addAll(readRuntimeVariables(dsFile));
                    }
                }
            }

            VirtualFile dockerShareFolder = nodeFolder.findChild("docker-share");

            if (dockerShareFolder != null){
                VirtualFile configJsonFile = dockerShareFolder.findChild("config.json");
                if (configJsonFile != null) {
                    try {
                        Map<String, Object> configJson = CustomJsonParser.parse(configJsonFile);

                        Map<String, Map<String, Object>> keys = ObjectUtil.cast(configJson.get("keys"));

                        if (keys != null) {
                            for (Map<String, Object> keyContent : keys.values()) {
                                List<String> exports = ObjectUtil.cast(keyContent.get("export"));
                                if (exports != null) {
                                    result.addAll(exports);
                                }
                            }
                        }
                    } catch (io.datakitchen.ide.json.ParseException ignored) {

                    }
                }
            }

        }
        return result;
    }

    private static Set<String> readRuntimeVariables(VirtualFile file) throws IOException{
        try {
            Set<String> result = new LinkedHashSet<>();
            Map<String, Object> json = JsonUtil.read(file);
            Map<String, String> runtimeVars = ObjectUtil.cast(json.get("set-runtime-vars"));
            if (runtimeVars != null){
                result.addAll(runtimeVars.values());
            }
            Map<String, Object> keys = ObjectUtil.cast(json.get("keys"));
            if (keys != null){
                for (Map.Entry<String, Object> entry: keys.entrySet()){
                    Map<String, Object> keyBody = ObjectUtil.cast(entry.getValue());
                    runtimeVars = ObjectUtil.cast(keyBody.get("set-runtime-vars"));
                    if (runtimeVars != null){
                        result.addAll(runtimeVars.values());
                    }
                }
            }
            List<Map<String, String>> assignments = ObjectUtil.cast(json.get("assign-variables"));

            if (assignments != null){
                for (Map<String, String> assignment: assignments){
                    result.add(assignment.get("name"));
                }
            }

            return result;
        }catch (ParseException ex){
            return Set.of();
        }
    }

    private static List<List<String>> readGraph(Module module, String variationName) throws IOException, ParseException {

        VirtualFile variationsFile = RecipeUtil.recipeFolder(module).findChild("variations.json");

        if (variationsFile != null) {

            Map<String, Object> variationsJson = JsonUtil.read(variationsFile);
            Map<String, Object> variationList = ObjectUtil.cast(variationsJson.get("variation-list"));
            Map<String, Object> variation = ObjectUtil.cast(variationList.get(variationName));

            if (variation != null) {

                String graphName = (String) variation.get("graph-setting");

                Map<String, Object> graphs = ObjectUtil.cast(variationsJson.get("graph-setting-list"));

                if (graphs.containsKey(graphName)) {
                    return ObjectUtil.cast(graphs.get(graphName));
                }
            }
        }
        return List.of();
    }
}
