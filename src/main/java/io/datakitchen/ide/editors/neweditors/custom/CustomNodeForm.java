package io.datakitchen.ide.editors.neweditors.custom;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonPatch;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.ui.EntryField;
import io.datakitchen.ide.ui.FocusLostListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.UIUtil;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomNodeForm extends FormPanel implements Disposable {

    private final Module module;
    private final VirtualFile nodeFolder;
    private final Map<Field, EntryField> fields = new LinkedHashMap<>();

    public CustomNodeForm(Module module, VirtualFile nodeFolder, Field[] fields){
        setLabelDimension(new Dimension(150, 28));
        setDefaultFieldDimension(new Dimension(300,28));
        this.module = module;
        this.nodeFolder = nodeFolder;
        setBorder(UIUtil.EMPTY_BORDER_5x5);

        buildForm(fields);
    }

    private void buildForm(Field[] fields) {
        for (Field field: fields){
            EntryField entryField = new EntryField(module);
            entryField.addFocusListener(new FocusLostListener(e -> applyChanges()));
            if (StringUtils.isNotBlank(field.getDescription())){
                entryField.setToolTipText(field.getDescription());
            }
            addField(field.getDisplayName(), entryField);
            this.fields.put(field, entryField);
        }
    }

    public void loadForm(){
        for (Map.Entry<Field, EntryField> entry:fields.entrySet()) {
            Field field = entry.getKey();
            EntryField entryField = entry.getValue();
            Change ref = field.getChanges()[0];
            loadField(nodeFolder.findFileByRelativePath(ref.getNodeFile()), ref.getJsonPointer(), entryField);
        }
    }

    private void applyChanges(){
        for (Map.Entry<Field, EntryField> entry:fields.entrySet()){
            Field fd = entry.getKey();
            EntryField field = entry.getValue();
            String value = field.getText();

            for (Change change: fd.getChanges()) {
                VirtualFile file = nodeFolder.findFileByRelativePath(change.getNodeFile());

                if (file != null) {
                    try {
                        applyChange(file, change.getJsonPointer(), change.getOperation(), value);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void loadField(VirtualFile fileByRelativePath, String jsonPointer, EntryField entryField) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode doc;
            try (Reader reader = new InputStreamReader(fileByRelativePath.getInputStream())) {
                doc = mapper.readTree(reader);
                JsonNode node = doc.at(JsonPointer.compile(jsonPointer));
                if (node.isTextual()){
                    entryField.setText(node.asText());
                }
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void applyChange(VirtualFile file, String jsonPointer, String operation, String value) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        JsonNode patch = buildPatch(jsonPointer, operation, value);
        JsonNode doc;
        try (Reader reader = new InputStreamReader(file.getInputStream())){
            doc = mapper.readTree(reader);
        }
        doc = JsonPatch.apply(patch, doc);
        write(file,doc);
    }

    private void write(VirtualFile file, JsonNode doc) {
        ApplicationManager.getApplication().runWriteAction(()->{
            try (Writer writer = new OutputStreamWriter(file.getOutputStream(this))){
                writer.write(doc.toPrettyString());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    private JsonNode buildPatch(String jsonPointer, String operation, String value) {
        JsonNodeFactory f = JsonNodeFactory.instance;
        ObjectNode node = new ObjectNode(f);
        node.put("op", operation);
        node.put("path", jsonPointer);
        node.put("value", value);
        ArrayNode patch = new ArrayNode(f);
        patch.add(node);
        return patch;
    }

    @Override
    public void dispose() {

    }
}
