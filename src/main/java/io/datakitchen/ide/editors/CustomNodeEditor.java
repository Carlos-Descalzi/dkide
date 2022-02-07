package io.datakitchen.ide.editors;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.neweditors.BaseSimpleEditor;
import io.datakitchen.ide.editors.neweditors.custom.CustomNodeForm;
import io.datakitchen.ide.editors.neweditors.custom.Field;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomNodeEditor extends BaseSimpleEditor {

    private CustomNodeForm form;

    public CustomNodeEditor(Module module, VirtualFile notebookFile) {
        super(module, notebookFile);
    }

    @Override
    protected JComponent buildView() {
        VirtualFile nodeFolder = notebookFile.getParent();
        try {
            Map<String, Object> obj = JsonUtil.read(
                    Objects.requireNonNull(nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON)));
            Map<String, Object> options = ObjectUtil.cast(obj.get("options"));
            Map<String, Object> customForm = ObjectUtil.cast(options.get("custom-form"));
            List<Map<String,Object>> fields = ObjectUtil.cast(customForm.get("fields"));

            Field[] fieldDescriptions = fields.stream()
                    .map(Field::fromJson).toArray(Field[]::new);

            form = new CustomNodeForm(module, nodeFolder, fieldDescriptions);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return form;
    }

    @Override
    public void selectNotify() {
        super.selectNotify();
        form.loadForm();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Node "+notebookFile.getParent().getName();
    }
}
