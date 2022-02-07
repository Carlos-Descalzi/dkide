package io.datakitchen.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.psi.PsiManager;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class FileWatcherService implements BulkFileListener {

    private static final Logger LOGGER = Logger.getInstance(FileWatcherService.class);

    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        Map<Module, Set<String>> nodeDeletes = new LinkedHashMap<>();
        for (VFileEvent event:events){
            if (event instanceof VFileDeleteEvent && event.getRequestor() instanceof PsiManager){
                VirtualFile file = event.getFile();

                PsiManager psiManager = (PsiManager)event.getRequestor();
                Project project = psiManager.getProject();
                assert file != null;
                Module module = ModuleUtil.findModuleForFile(file, project);
                if (RecipeUtil.isNodeFolder(module, file)){
                    nodeDeletes.computeIfAbsent(module, m->new LinkedHashSet<>()).add(file.getName());
                }

            }
        }
        for (Map.Entry<Module, Set<String>> entry: nodeDeletes.entrySet()) {
            updateNodeDeletes(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        for (VFileEvent event:events){
            if (event instanceof VFilePropertyChangeEvent && event.getRequestor() instanceof PsiManager){

                VFilePropertyChangeEvent moveEvent = (VFilePropertyChangeEvent) event;
                String oldName = (String)moveEvent.getOldValue();
                VirtualFile file = event.getFile();

                PsiManager psiManager = (PsiManager)event.getRequestor();
                Project project = psiManager.getProject();
                assert file != null;
                Module module = ModuleUtil.findModuleForFile(file, project);
                if (RecipeUtil.isNodeFolder(module, file)){
                    updateReferencesInVariation(module, oldName, file.getName());
                }

            }
        }
    }

    private void updateNodeDeletes(Module module, Set<String> nodesToDelete) {
        try {
            VirtualFile variationsFile = RecipeUtil.recipeFolder(module).findChild(Constants.FILE_VARIATIONS_JSON);

            assert variationsFile != null;
            Map<String, Object> variationsJson = JsonUtil.read(variationsFile);

            Map<String, Object> graphsObject = ObjectUtil.cast(variationsJson.get("graph-setting-list"));

            Map<String, Object> newGraphsObject = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : graphsObject.entrySet()) {
                List<List<String>> graph = ObjectUtil.cast(entry.getValue());
                newGraphsObject.put(entry.getKey(),updateGraph(graph, nodesToDelete));
            }
            variationsJson.put("graph-setting-list",newGraphsObject);

            ThrowableComputable<Void, IOException> action = ()->{
                JsonUtil.write(variationsJson, variationsFile);
                return null;
            };

            ApplicationManager.getApplication().runWriteAction(action);
        }catch(Exception ex){
            LOGGER.error(ex);
        }
    }

    private Object updateGraph(List<List<String>> graph, Set<String> nodesToDelete) {
        List<List<String>> result = new ArrayList<>();

        for (List<String> edge : graph){
            List<String> newEdge = new ArrayList<>();
            for (String node:edge){
                if (!nodesToDelete.contains(node)){
                    newEdge.add(node);
                }
            }
            if (newEdge.size() > 0) {
                result.add(newEdge);
            }
        }

        return result;
    }

    private void updateReferencesInVariation(Module module, String oldName, String newName) {
        try {
            VirtualFile variationsFile = RecipeUtil.recipeFolder(module).findChild(Constants.FILE_VARIATIONS_JSON);
            assert variationsFile != null;
            Map<String, Object> variationsJson = JsonUtil.read(variationsFile);

            Map<String, Object> graphsObject = ObjectUtil.cast(variationsJson.get("graph-setting-list"));

            Map<String, Object> newGraphsObject = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : graphsObject.entrySet()) {
                List<List<String>> graph = ObjectUtil.cast(entry.getValue());
                newGraphsObject.put(entry.getKey(),updateGraph(graph, oldName, newName));
            }
            variationsJson.put("graph-setting-list",newGraphsObject);

            ThrowableComputable<Void, IOException> action = ()->{
                JsonUtil.write(variationsJson, variationsFile);
                return null;
            };

            ApplicationManager.getApplication().runWriteAction(action);
        }catch(Exception ex){
            LOGGER.error(ex);
        }
    }

    private List<List<String>> updateGraph(List<List<String>> graph, String oldName, String newName) {
        List<List<String>> result = new ArrayList<>();

        for (List<String> edge : graph){
            List<String> newEdge = new ArrayList<>();
            for (String node:edge){
                if (node.equals(oldName)){
                    if (newName != null) {
                        newEdge.add(newName);
                    }
                } else {
                    newEdge.add(node);
                }
            }
            if (newEdge.size() > 0) {
                result.add(newEdge);
            }
        }

        return result;
    }
}
