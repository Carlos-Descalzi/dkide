package io.datakitchen.ide.tools;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RecipeResultsView extends BaseResultsView {

    private final VirtualFile outputFolder;
    private final VirtualFile recipeFolder;
    private Map<String, String> outputTypes = new HashMap<>();

    public RecipeResultsView(Project project, VirtualFile recipeFolder, VirtualFile outputFolder){
        super(project, outputFolder);
        this.outputFolder = outputFolder;
        this.recipeFolder = recipeFolder;

        refresh();
    }

    protected void showItem() {
        if (itemList.getSelectedIndex() != -1) {
            File file = (File)itemList.getSelectedValue();
            contentPane.removeAll();
            if (file.getName().equals("compiled.json")){
                try {
                    contentPane.add(new RecipeView(recipeFolder, file));
                }catch (Exception ex){
                    ex.printStackTrace();
                    showAsText(file);
                }
            } else {
                showAsText(file);
            }
        }
    }

    @Override
    protected void onRefresh() {

    }


    @Override
    protected String getFileFormat(File file) {
        return null;
    }
}
