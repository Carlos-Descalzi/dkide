package io.datakitchen.ide.editors.neweditors.ingredient;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.model.Test;
import io.datakitchen.ide.util.JsonUtil;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class IngredientNodeModelWriter {

    private final IngredientNodeModelImpl model;
    private final VirtualFile nodeFolder;

    public IngredientNodeModelWriter(IngredientNodeModelImpl model, VirtualFile nodeFolder){
        this.model = model;
        this.nodeFolder = nodeFolder;
    }

    public void write() throws IOException, ParseException {
        writeDescription();
        writeNotebook();
    }

    private void writeNotebook() throws IOException {
        Map<String, Object> notebookJson = new LinkedHashMap<>();

        Map<String, Object> tests = new LinkedHashMap<>();

        notebookJson.put("tests", tests);

        int i=1;
        for (Test test: model.getTests()){
            tests.put("test-"+(i++), test.toJson());
        }

        JsonUtil.write(notebookJson, nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON));
    }

    private void writeDescription() throws IOException, ParseException {
        VirtualFile descriptionFile = nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);

        assert descriptionFile != null;
        Map<String, Object> descriptionJson = JsonUtil.read(descriptionFile);

        descriptionJson.put("description",model.getDescription());

        JsonUtil.write(descriptionJson, descriptionFile);
    }
}
