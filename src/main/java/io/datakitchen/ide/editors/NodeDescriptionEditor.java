package io.datakitchen.ide.editors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.ui.FocusLostListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class NodeDescriptionEditor extends AbstractFileEditor {

    private static final Logger LOGGER = Logger.getInstance(NodeDescriptionEditor.class);

    private final FormPanel panel = new FormPanel();
    private final VirtualFile file;
    private final JTextField type = new JTextField();
    private final JEditorPane description = new JEditorPane();

    public NodeDescriptionEditor(Project project, VirtualFile file){
        this.file = file;
        panel.setBorder(JBUI.Borders.empty(10));

        panel.addField("Type",type,new Dimension(200,28));
        type.setEnabled(false);

        JScrollPane scroll = new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new LineBorder(panel.getBackground().brighter()));
        p.add(scroll, BorderLayout.CENTER);
        panel.addField("Description",p,new Dimension(800,500));
        description.addFocusListener(new FocusLostListener(e -> saveDocument()));

        loadDocument();
    }

    private void loadDocument() {
        try {
            Map<String,Object> descriptionJson = JsonUtil.read(this.file);
            type.setText((String)descriptionJson.get("type"));
            description.setText((String)descriptionJson.get("description"));
        } catch (Exception ex){
            // document may still be being edited and is not yet in good shape.
        }
    }

    @Override
    public void selectNotify() {
        loadDocument();
    }

    @Override
    public void deselectNotify() {
        saveDocument();
    }

    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return description;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Description Editor";
    }

    private void saveDocument(){
        try {
            Map<String,Object> descriptionJson = JsonUtil.read(this.file);
            String descriptionText = description.getText();

            if (StringUtils.isBlank(descriptionText)){
                descriptionJson.remove("description");
            } else {
                descriptionJson.put("description",descriptionText);
            }

            ThrowableComputable<Void, IOException> action = () ->{
                JsonUtil.write(descriptionJson,file);
                return null;
            };

            ApplicationManager.getApplication().runWriteAction(action);
        }catch(Exception ex){
            LOGGER.error(ex);
        }
    }

    public @NotNull VirtualFile getFile(){
        return file;
    }

}
