package io.datakitchen.ide.editors;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.messages.MessageBusConnection;
import io.datakitchen.ide.editors.graph.VariationGraph;
import io.datakitchen.ide.editors.graph.VariationGraphsEditor;
import io.datakitchen.ide.editors.ingredient.IngredientsListEditor;
import io.datakitchen.ide.editors.schedule.ScheduleItem;
import io.datakitchen.ide.editors.schedule.VariationSchedulesEditor;
import io.datakitchen.ide.editors.variables.OverridesEditor;
import io.datakitchen.ide.editors.variation.VariationInfo;
import io.datakitchen.ide.editors.variation.VariationItem;
import io.datakitchen.ide.editors.variation.VariationListEditor;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class VariationsFileEditor extends AbstractFileEditor implements DocumentChangeListener, VariationEditionListener {

    private static final Logger LOGGER = Logger.getInstance(VariationsFileEditor.class);

    private final Project project;
    private final VirtualFile file;
    private Map<String, Object> document;

    private final JTabbedPane tabs = new JBTabbedPane();
    private final VariationListEditor variationListEditor;
    private final OverridesEditor overridesEditor;
    private final VariationSchedulesEditor variationSchedulesEditor;
    private final VariationGraphsEditor variationGraphsEditor;
    private final IngredientsListEditor ingredientListEditor;

    private final MessageBusConnection connection;

    public VariationsFileEditor(Project project, VirtualFile file){
        this.project = project;
        this.file = file;

        this.variationListEditor = new VariationListEditor();
        Disposer.register(this, variationListEditor);
        this.overridesEditor = new OverridesEditor(project);
        Disposer.register(this, overridesEditor);
        this.variationSchedulesEditor = new VariationSchedulesEditor();
        Disposer.register(this, variationSchedulesEditor);
        this.variationGraphsEditor = new VariationGraphsEditor(project, file.getParent());
        Disposer.register(this, variationGraphsEditor);
        this.ingredientListEditor = new IngredientsListEditor();
        Disposer.register(this, ingredientListEditor);


        this.variationListEditor.addDocumentChangeListener(this);
        this.overridesEditor.addDocumentChangeListener(this);
        this.variationSchedulesEditor.addDocumentChangeListener(this);
        this.variationGraphsEditor.addDocumentChangeListener(this);
        this.ingredientListEditor.addDocumentChangeListener(this);

        this.overridesEditor.addVariationEditionListener(this);
        this.variationSchedulesEditor.addVariationEditionListener(this);
        this.variationGraphsEditor.addVariationEditionListener(this);
        this.ingredientListEditor.addVariationEditionListener(this);

        tabs.addTab("Variations", variationListEditor);
        tabs.addTab("Overrides", overridesEditor);
        tabs.addTab("Graphs", variationGraphsEditor);
        tabs.addTab("Schedules", variationSchedulesEditor);
        tabs.addTab("Ingredients", ingredientListEditor);

        connection = project.getMessageBus().connect();
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
                filesUpdated(events);
            }

            @Override
            public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
                checkRemoved(events);
            }
        });

        variationListEditor.setTransferDataSupplier(this::createTransferData);

        loadDocument();
    }

    private VariationInfo createTransferData(VariationItem variationItem) {

        String graphName = (String)variationItem.getVariation().get("graph-setting");
        String scheduleName = (String)variationItem.getVariation().get("schedule-setting");
        List<String> overrideNames = ObjectUtil.cast(variationItem.getVariation().getOrDefault("override-setting",new ArrayList<>()));

        VariationGraph graph = variationGraphsEditor.getGraphByName(graphName);
        ScheduleItem scheduleItem = variationSchedulesEditor.getScheduleByName(scheduleName);
        Map<String, Object> overrides = overridesEditor.getOverridesByName(overrideNames);

        Module module = ModuleUtil.findModuleForFile(file, project);

        String recipeName = module.getName();

        return new VariationInfo(
            recipeName,
            variationItem,
            scheduleItem,
            overrides,
            graph);
    }

    @Override
    public void selectNotify() {

    }

    private void checkRemoved(List<? extends VFileEvent> events) {
        for (VFileEvent event:events){
            if (event instanceof VFileDeleteEvent){
                Module module = ModuleUtil.findModuleForFile(Objects.requireNonNull(event.getFile()),project);
                if (module != null && RecipeUtil.isNodeFolder(module,event.getFile())) {
                    variationGraphsEditor.notifyNodeRemoved(event.getFile().getName());
                }
            }
        }
    }

    private void filesUpdated(List<? extends VFileEvent> events) {
        for (VFileEvent event:events){
            if (event instanceof VFilePropertyChangeEvent){
                Module module = ModuleUtil.findModuleForFile(Objects.requireNonNull(event.getFile()),project);
                if (module != null && RecipeUtil.isNodeFolder(module,event.getFile())){
                    variationGraphsEditor.notifyNodeRenamed(
                            (String)((VFilePropertyChangeEvent)event).getOldValue(),
                            (String)((VFilePropertyChangeEvent)event).getNewValue());
                }
            }
        }

    }

    @Override
    public void deselectNotify() {
        saveDocument();
    }

    public void dispose(){
        super.dispose();
        connection.disconnect();
    }

    private void loadDocument(){
        try {
            document = JsonUtil.read(file);
        } catch (Exception ex){
            document = new LinkedHashMap<>();
            ex.printStackTrace();
        }
        variationListEditor.setVariationDocument(document);
        variationGraphsEditor.setVariationDocument(document);
        variationSchedulesEditor.setVariationDocument(document);
        overridesEditor.setVariationDocument(document);
        ingredientListEditor.setVariationDocument(document);
    }

    @Override
    public void documentChanged(DocumentChangeEvent e) {
        if (e.getSource() == variationGraphsEditor
            || e.getSource() == variationSchedulesEditor
            || e.getSource() == overridesEditor
            || e.getSource() == ingredientListEditor){
            variationListEditor.updateOptions();
        }
        saveDocument();
    }

    private void saveDocument() {
        variationListEditor.saveDocument();
        variationGraphsEditor.saveDocument();
        variationSchedulesEditor.saveDocument();
        overridesEditor.saveDocument();
        ingredientListEditor.saveDocument();
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                JsonUtil.write(this, document, file);
            }catch (Exception ex){
                LOGGER.error(ex);
            }
        });
        ProjectView.getInstance(project).refresh();
    }

    public @NotNull VirtualFile getFile(){
        return file;
    }
    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Variation Editor";
    }

    @Override
    public @NotNull JComponent getComponent() {
        return tabs;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return tabs;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public void variationItemAdded(VariationEditionEvent event) {
        variationListEditor.notifyReferencedObjectAdded(event.getTypeName(), event.getItemName());
    }

    @Override
    public void variationItemRemoved(VariationEditionEvent event) {
        variationListEditor.notifyReferencedObjectRemoved(event.getTypeName(), event.getItemName());
    }

    @Override
    public void variationItemChanged(VariationEditionEvent event) {
        variationListEditor.notifyReferencedObjectChanged(event.getTypeName(), event.getItemName(), event.getOldItemName());
    }
}
