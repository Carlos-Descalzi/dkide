package io.datakitchen.ide.editors.neweditors.variation;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.graph.ConditionsCollection;
import io.datakitchen.ide.editors.graph.GraphEditor;
import io.datakitchen.ide.editors.graph.VariationGraph;
import io.datakitchen.ide.editors.variables.OverridesEditor;
import io.datakitchen.ide.run.VariationRunner;
import io.datakitchen.ide.ui.ItemList;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeVariationsEditor extends JPanel implements Disposable, PropertyChangeListener {

    private static final Logger LOGGER = Logger.getInstance(RecipeVariationsEditor.class);

    private final Project project;
    private final ItemList<VariationInfo> items;
    private final GraphEditor graphEditor;
    private final VariationDetailsEditor variationDetailsEditor;
    private final IngredientEditor ingredientEditor;
    private final VirtualFile variationsFile;
    private final OverridesEditor overridesEditor;
    private final Action runAction = new SimpleAction(AllIcons.Actions.Execute,"Run Variation", "Run Variation", this::runVariation);

    public RecipeVariationsEditor(Project project, VirtualFile variationsFile){
        this.project = project;
        this.variationsFile = variationsFile;
        items = new ItemList<>(this::createVariation);
        graphEditor = new GraphEditor(project, variationsFile.getParent());
        Disposer.register(this, graphEditor);
        variationDetailsEditor = new VariationDetailsEditor();
        ingredientEditor = new IngredientEditor();
        overridesEditor = new OverridesEditor(project);
        Disposer.register(this, overridesEditor);
        JPanel variationsPanel = new JPanel(new BorderLayout());

        JPanel topBar = new JPanel(new GridLayout(1,2));
        JPanel rightTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightTopBar.add(new JButton(new SimpleAction("Switch to advanced mode", this::switchToAdvancedMode)));
        topBar.add(rightTopBar);

        variationsPanel.add(items, BorderLayout.WEST);
        JTabbedPane tabs = new JBTabbedPane();
        tabs.addTab("Graph", graphEditor);
        tabs.addTab("Details", variationDetailsEditor);
        tabs.addTab("Ingredient", ingredientEditor);

        JPanel leftTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftTopBar.add(new JButton(runAction));

        JPanel variationsCenterPanel = new JPanel(new BorderLayout());
        variationsCenterPanel.add(leftTopBar, BorderLayout.NORTH);
        variationsCenterPanel.add(tabs, BorderLayout.CENTER);

        variationsPanel.add(variationsCenterPanel, BorderLayout.CENTER);
        items.addListSelectionListener(this::showItem);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);

        JTabbedPane mainTabs = new JBTabbedPane();
        mainTabs.addTab("Variations", variationsPanel);
        mainTabs.addTab("Override Sets", overridesEditor);
        add(mainTabs, BorderLayout.CENTER);
        loadFile();
        updateActions();

        DocumentChangeListener listener = this::saveVariations;

        graphEditor.addDocumentChangeListener(listener);
        variationDetailsEditor.addDocumentChangeListener(listener);
        ingredientEditor.addDocumentChangeListener(listener);
        overridesEditor.addDocumentChangeListener(listener);

    }

    private VariationInfo createVariation(){
        return new VariationInfo("variation-"+(items.getDataSize()+1));
    }

    @Override
    public void dispose() {
    }

    private void switchToAdvancedMode(ActionEvent event) {
        VirtualFile recipeFolder = variationsFile.getParent();
        VirtualFile descriptionFile = recipeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);

        if (descriptionFile != null){
            try {
                Map<String, Object> descriptionJson = JsonUtil.read(descriptionFile);

                Map<String, Object> options = ObjectUtil.cast(descriptionJson.computeIfAbsent("options", __ -> new LinkedHashMap<>()));

                options.put("simplified-view", false);

                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        JsonUtil.write(descriptionJson, descriptionFile);

                        ApplicationManager.getApplication().invokeLater(()->{
                            FileEditorManager.getInstance(project)
                                    .closeFile(variationsFile);
                            FileEditorManager.getInstance(project)
                                    .openFile(variationsFile,true);
                        });
                    } catch (Exception ex) {
                        LOGGER.error(ex);
                    }
                });
            }catch (Exception ex){
                LOGGER.error(ex);
            }
        }
    }

    private void saveVariations(DocumentChangeEvent documentChangeEvent) {
        VariationInfo item = items.getSelected();
        graphEditor.saveGraph(item.getVariationGraph());

        Map<String, Object> variationsJson = new LinkedHashMap<>();
        Map<String, Object> variationList = new LinkedHashMap<>();
        Map<String, Object> graphs = new LinkedHashMap<>();
        Map<String, Object> schedules = new LinkedHashMap<>();
        List<Map<String, Object>> ingredients = new ArrayList<>();
        variationsJson.put("variation-list", variationList);
        variationsJson.put("graph-setting-list", graphs);
        variationsJson.put("override-setting-list", overridesEditor.getOverrides());
        variationsJson.put("schedule-setting-list", schedules);
        variationsJson.put("ingredient-definition-list", ingredients);

        for (VariationInfo info: items.getData()){

            String graphName = "graph-"+info.getName();

            Map<String, Object> variationJson = new LinkedHashMap<>();
            variationJson.put("graph-setting", graphName);
            variationJson.put("description", info.getDescription());

            Set<String> overrides = info.getOverrideSets();
            if (!overrides.isEmpty()){
                variationJson.put("override-setting", new ArrayList<>(overrides));
            }

            if (info.getIngredient() != null){
                variationJson.put("ingredient-definition",info.getIngredient().getName());
                ingredients.add(info.getIngredient().toJson());
            }

            if (info.getSchedule() != null){
                String scheduleName = "schedule-"+info.getName();
                variationJson.put("schedule-setting", scheduleName);
                schedules.put(scheduleName, info.getSchedule().toJson());
            }

            VariationGraph graph = info.getVariationGraph();
            graphs.put(graphName, graph.getGraph());
            variationList.put(info.getName(),variationJson);
        }

        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                JsonUtil.write(variationsJson, variationsFile);
            }catch (Exception ex){
                LOGGER.error(ex);
            }
        });
    }

    private void runVariation(ActionEvent event) {
        Module module = ModuleUtil.findModuleForFile(variationsFile, project);
        new VariationRunner(module)
                .setVariation(items.getSelected().getName())
                .run();
    }

    private void updateActions(){
        runAction.setEnabled(items.getSelected() != null);
    }

    private void loadFile() {
        String recipeName = variationsFile.getParent().getName();

        try {
            Map<String, Object> variationsJson = JsonUtil.read(variationsFile);

            variationDetailsEditor.setVariationsDocument(variationsJson);
            overridesEditor.setVariationDocument(variationsJson);

            Map<String, Object> variations = ObjectUtil.cast(variationsJson.get("variation-list"));
            Map<String, List<List<Object>>> graphs = ObjectUtil.cast(variationsJson.get("graph-setting-list"));
            Map<String, Object> schedules = ObjectUtil.cast(variationsJson.get("schedule-setting-list"));
            List<Map<String, Object>> ingredients = ObjectUtil.cast(variationsJson.get("ingredient-definition-list"));

            ConditionsCollection conditions = ConditionsCollection.NULL_COLLECTION;

            List<VariationInfo> data = new ArrayList<>();
            for (Map.Entry<String, Object> variationEntry : variations.entrySet()){
                Map<String, Object> variationData = ObjectUtil.cast(variationEntry.getValue());

                VariationInfo variation = new VariationInfo();
                variation.setName(variationEntry.getKey());
                variation.setDescription((String)variationData.get("description"));


                Object overrides = variationData.get("override-setting");
                if (overrides instanceof String){
                    variation.setOverrideSets(Set.of((String)overrides));
                } else if (overrides instanceof List){
                    List<String> overrideList = ObjectUtil.cast(overrides);
                    variation.setOverrideSets(new LinkedHashSet<>(overrideList));
                }

                String graphName = (String)variationData.get("graph-setting");
                List<List<Object>> graph = graphs.get(graphName);

                VariationGraph variationGraph = new VariationGraph(recipeName, graphName, graph, conditions);
                variation.setVariationGraph(variationGraph);

                String scheduleName = (String)variationData.get("schedule-setting");
                if (scheduleName == null){
                    scheduleName = (String)variationData.get("mesos-setting");
                }

                if (scheduleName != null){
                    Map<String, Object> scheduleJson = ObjectUtil.cast(schedules.get(scheduleName));
                    String scheduleString = (String) scheduleJson.get("schedule");
                    String timeZone = (String) scheduleJson.get("scheduleTimeZone");
                    Integer maxRam = (Integer) scheduleJson.get("max-ram");
                    Integer maxDisk = (Integer) scheduleJson.get("max-disk");

                    variation.setSchedule(new VariationInfo.Schedule(scheduleString, timeZone, maxRam, maxDisk));
                }

                String ingredientName = (String)variationData.get("ingredient-definition");
                if (ingredientName != null && ingredients != null){

                    ingredients.stream()
                        .filter(i -> i.get("ingredient-name").equals(ingredientName))
                        .findFirst()
                        .ifPresent(i ->
                            variation.setIngredient(new VariationInfo.Ingredient(
                                (String)i.get("ingredient-name"),
                                (String)i.get("description"),
                                (String)i.get("short-description"),
                                (String)i.get("rollback-ingredient"),
                                ((List<Map<String, Object>>)i.get("required-recipe-variables"))
                                        .stream()
                                        .map(VariationInfo.IngredientVariable::fromJson)
                                        .collect(Collectors.toList()),
                                ObjectUtil.cast(i.get("apply-runtime-recipe-variables"))
                        )));
                }
                data.add(variation);
            }

            data.forEach(v -> v.addPropertyChangeListener(this));

            items.setData(data);
            if (items.getDataSize() > 0){
                items.setSelectedIndex(0);
            }
        }catch (Exception ex){
            LOGGER.error(ex);
        }
    }

    private void showItem(ListSelectionEvent event) {
        VariationInfo item = items.getSelected();
        graphEditor.loadGraph(item.getVariationGraph());
        variationDetailsEditor.setCurrentVariation(item);
        ingredientEditor.setCurrentVariation(item);
        updateActions();
    }

    public void notifyNodeRemoved(String nodeName){
        VariationInfo item = items.getSelected();
        if (item != null){
            VariationGraph graph = item.getVariationGraph();
            graph.removeNode(nodeName);
            graphEditor.loadGraph(graph);
        }
    }


    public void notifyNodeRenamed(String oldValue, String newValue) {
        VariationInfo item = items.getSelected();
        if (item != null){
            VariationGraph graph = item.getVariationGraph();
            graph.renameNode(oldValue, newValue);
            graphEditor.loadGraph(graph);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // If a variation changes its name, check if it is the active variation
        // and update it.
        Module module = ModuleUtil.findModuleForFile(variationsFile, project);
        VariationInfo variation = (VariationInfo) evt.getSource();
        if (evt.getPropertyName().equals("name")){
            String activeVariation = RecipeUtil.getActiveVariation(module);

            if (activeVariation != null
                && activeVariation.equals(evt.getOldValue())){
                RecipeUtil.setActiveVariation(module, variation.getName());
            }
        }
    }
}
