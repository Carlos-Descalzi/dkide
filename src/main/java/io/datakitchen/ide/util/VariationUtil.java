package io.datakitchen.ide.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.graph.GraphInfo;
import io.datakitchen.ide.editors.graph.VariationGraph;
import io.datakitchen.ide.editors.variation.VariationInfo;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class VariationUtil {

    private static final Logger LOGGER = Logger.getInstance(VariationUtil.class);

    public static void copyVariation(VariationInfo variation, Module targetModule)  {

        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                copyNodes(variation.getRecipeName(), variation.getGraph(), targetModule);
                copyVariationInfo(variation, targetModule);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    public static void copyGraph(GraphInfo graph, Module targetModule) {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                copyNodes(graph.getRecipeName(), graph.getGraph(), targetModule);
                copyGraphInfo(graph, targetModule);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    public static void copyNodes(String recipeName, VariationGraph graph, Module targetModule) {
        Project project = targetModule.getProject();
        Arrays.stream(ModuleManager.getInstance(project).getModules())
            .filter(m -> m.getName().equals(recipeName))
            .findFirst()
            .ifPresentOrElse(sourceModule -> {
                VirtualFile sourceRecipeFolder = RecipeUtil.recipeFolder(sourceModule);
                VirtualFile targetRecipeFolder = RecipeUtil.recipeFolder(targetModule);
                Set<String> nodeNames = getNodeNames(graph);

                for (String nodeName: nodeNames){
                    try {
                        VirtualFile sourceNodeFolder = sourceRecipeFolder.findChild(nodeName);

                        if (sourceNodeFolder != null) {
                            if (targetRecipeFolder.findChild(nodeName) == null) {
                                VirtualFile targetNodeFolder = targetRecipeFolder.createChildDirectory(project, nodeName);

                                VfsUtil.copyDirectory(project, sourceNodeFolder, targetNodeFolder, null);
                            }
                        } else {
                            LOGGER.error("Node folder "+nodeName+" not found for recipe "+sourceModule);
                        }
                    }catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
            },()->
                LOGGER.error("Unable to find recipe "+recipeName)
            );

    }

    private static Set<String> getNodeNames(VariationGraph graph) {
        Set<String> nodeNames = new HashSet<>();
        for (List<Object> edge: graph.getGraph()){
            nodeNames.addAll(edge.stream().map(String::valueOf).collect(Collectors.toList()));
        }
        return nodeNames;
    }

    @SuppressWarnings("unchecked")
    private static void copyGraphInfo(GraphInfo graph, Module targetModule) throws IOException, ParseException {
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(targetModule);

        VirtualFile variationsFile = recipeFolder.findChild(Constants.FILE_VARIATIONS_JSON);
        if (variationsFile == null){
            LOGGER.error("File variations.json does not exist on supposed recipe folder "+recipeFolder.getName());
            return;
        }
        Map<String, Object> variationsJson = JsonUtil.read(variationsFile);

        Map<String, Object> graphSettingList = (Map<String, Object>) variationsJson.get("graph-setting-list");
        graphSettingList.put(graph.getGraph().getName(),graph.getGraph().getGraph());

        JsonUtil.write(variationsJson, variationsFile);
    }

    @SuppressWarnings("unchecked")
    private static void copyVariationInfo(VariationInfo variation, Module targetModule) throws IOException, ParseException {
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(targetModule);

        VirtualFile variationsFile = recipeFolder.findChild(Constants.FILE_VARIATIONS_JSON);
        if (variationsFile == null){
            LOGGER.error("File variations.json does not exist on supposed recipe folder "+recipeFolder.getName());
            return;
        }
        Map<String, Object> variationsJson = JsonUtil.read(variationsFile);
        Map<String, Object> variationList = (Map<String, Object>) variationsJson.get("variation-list");

        variationList.put(variation.getVariation().getName(), variation.getVariation().getVariation());

        Map<String, Object> graphSettingList = (Map<String, Object>) variationsJson.get("graph-setting-list");
        graphSettingList.put(variation.getGraph().getName(),variation.getGraph().getGraph());

        if (variation.getScheduleData() != null){
            Map<String, Object> schedules = (Map<String, Object>)variationsJson.computeIfAbsent(
                "schedule-setting-list", __ -> new LinkedHashMap<>());

            schedules.put(variation.getScheduleData().getName(), variation.getScheduleData().getValue());
        }

        Map<String, Object> overrides = (Map<String, Object>) variationsJson.get("override-setting-list");
        if (variation.getOverrides() != null){
            overrides.putAll(variation.getOverrides());
        }

        JsonUtil.write(variationsJson, variationsFile);
    }
}
