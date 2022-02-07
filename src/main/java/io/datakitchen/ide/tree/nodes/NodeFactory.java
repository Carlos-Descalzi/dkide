package io.datakitchen.ide.tree.nodes;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.tree.nodes.container.ContainerNode;
import io.datakitchen.ide.tree.nodes.script.ScriptNode;
import io.datakitchen.ide.tree.simple.*;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.Map;

public class NodeFactory {

    @SuppressWarnings("unchecked")
    public static AbstractTreeNode<? extends UserDataHolder> createNodeForFile(
            Project project,
            VirtualFile file,
            ViewSettings viewSettings
    ) throws IOException, ParseException {

        VirtualFile descriptionJson = file.findChild(Constants.FILE_DESCRIPTION_JSON);
        VirtualFile variationsJson = file.getParent().findChild(Constants.FILE_VARIATIONS_JSON);

        if (descriptionJson == null || variationsJson == null){
            return null;
        }

        Map<String, Object> obj = JsonUtil.read(descriptionJson);
        Map<String, Object> variationData = JsonUtil.read(variationsJson);

        String type = (String)obj.get("type");

        boolean simplifiedView = false;
        if (ConfigurationService.getInstance(project).getGlobalConfiguration().getMiscOptions().isSimplifiedView()) {

            Map<String, Object> options = (Map<String, Object>) obj.get("options");

            if (options != null) {
                simplifiedView = (Boolean) options.getOrDefault("simplified-view", false);
            }
        }

        switch(type){
            case NodeType.NOOP_NODE_TYPE_NAME:
                if (isConditionalNode(file.getName(),variationData)){
                    return new ConditionalNode(project, file,viewSettings);
                }
                if (simplifiedView){
                    return new SimpleNoOpNode(project, file, viewSettings);
                }
                return new NoOpNode(project,file,viewSettings);
            case NodeType.ACTION_NODE_TYPE_NAME:
                if (simplifiedView){
                    return new SimpleActionNode(project, file, viewSettings);
                }
                return new ActionNode(project,file,viewSettings);
            case NodeType.DATA_MAPPER_NODE_TYPE_NAME:
                if (simplifiedView){
                    return new SimpleDataMapperNode(project, file, viewSettings);
                }
                return new DataMapperNode(project, file,viewSettings);
            case NodeType.INGREDIENT_NODE_TYPE_NAME:
                if (simplifiedView){
                    return new SimpleIngredientNode(project, file, viewSettings);
                }
                return new IngredientNode(project, file,viewSettings);
            case NodeType.CONTAINER_NODE_TYPE_NAME:
                if (simplifiedView){
                    return new SimpleContainerNode(project, file, viewSettings);
                } else {
                    if (RecipeUtil.isScriptNode(file)){
                        return new ScriptNode(project, file,viewSettings);
                    } else {
                        return new ContainerNode(project, file,viewSettings);
                    }
                }
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    private static boolean isConditionalNode(String name, Map<String, Object> variationData) {
        Map<String, Object> conditionsList = (Map<String, Object>) variationData.get("conditions-list");

        if (conditionsList != null) {
            for (Map.Entry<String, Object> entry : conditionsList.entrySet()) {
                Map<String, Object> condition = (Map<String, Object>) entry.getValue();

                if (name.equals(condition.get("node"))) {
                    return true;
                }
            }
        }
        return false;
    }

}
