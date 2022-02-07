package io.datakitchen.ide.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryService {
    /**
     * TODO when a new library is added it should notify UI to refresh
     */
    private final Project project;
    private final EventSupport<LibraryServiceListener> listeners = EventSupport.of(LibraryServiceListener.class);
    private Map<String, Object> configuration;

    public LibraryService(Project project){
        this.project = project;
        readConfiguration();
    }

    private void readConfiguration() {
        try {
            configuration = JsonUtil.read(Constants.LIBRARY_FILE.toURI().toURL());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void saveConfiguration(){
        try {
            JsonUtil.write(configuration, Constants.LIBRARY_FILE);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static LibraryService getInstance(Project project){
        return project.getService(LibraryService.class);
    }

    public Map<String, List<String>> getLibraryNodes(){
        Map<String, List<String>> result = new LinkedHashMap<>();

        List<String> recipes = ObjectUtil.cast(configuration.get("recipes"));

        if (recipes != null) {
            for (String recipePath : recipes) {
                File file = new File(recipePath);
                if (file.exists()) {
                    String recipeName = file.getAbsolutePath();

                    List<String> nodeNames = Arrays
                            .stream(file.listFiles(f -> RecipeUtil.isNodeFolder(f)))
                            .map(File::getName).collect(Collectors.toList());

                    result.put(recipeName, nodeNames);
                }
            }
        }
        return result;
    }

    public void addRecipe(VirtualFile recipeFolder){
        List<String> recipes = ObjectUtil.cast(configuration
                .computeIfAbsent("recipes", s -> new ArrayList<>()));

        String path = recipeFolder.getPath();

        if (!recipes.contains(path)) {
            recipes.add(path);
        }
        saveConfiguration();
        listeners.getProxy().libraryAdded(new LibraryServiceEvent(this, recipeFolder.getName()));
    }

    public void addLibraryServiceListener(LibraryServiceListener listener){
        listeners.addListener(listener);
    }

    public void removeLibraryServiceListener(LibraryServiceListener listener){
        listeners.removeListener(listener);
    }
}
