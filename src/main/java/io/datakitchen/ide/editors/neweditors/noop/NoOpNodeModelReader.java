package io.datakitchen.ide.editors.neweditors.noop;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.model.Test;
import io.datakitchen.ide.util.JsonUtil;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class NoOpNodeModelReader {

    private final NoOpNodeModelImpl model;
    private final VirtualFile nodeFolder;

    public NoOpNodeModelReader(NoOpNodeModelImpl model, VirtualFile nodeFolder){
        this.model = model;
        this.nodeFolder = nodeFolder;
    }

    public void read() throws IOException, ParseException {
        readDescription();
        readNotebook();
    }

    private void readNotebook() throws IOException, ParseException {
        Map<String, Object> notebookJson = JsonUtil.read(Objects.requireNonNull(nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON)));

        Map<String, Object> tests = (Map<String, Object>) notebookJson.get("tests");

        if (tests != null) {
            for (Map.Entry<String, Object> entry : tests.entrySet()) {
                Map<String, Object> testJson = (Map<String, Object>) entry.getValue();
                Test test = Test.fromJson(testJson, new LinkedHashMap<>());
                model.addTest(test);
            }
        }
    }

    private void readDescription() throws IOException, ParseException {
        Map<String, Object> descriptionJson = JsonUtil.read(Objects.requireNonNull(nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON)));

        model.setDescription((String)descriptionJson.get("description"));
    }
}
