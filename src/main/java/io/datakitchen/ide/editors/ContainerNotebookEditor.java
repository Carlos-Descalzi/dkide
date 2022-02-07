package io.datakitchen.ide.editors;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.EntryField;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class ContainerNotebookEditor extends BaseContainerNotebookEditor{

    private EntryField imageNamespace;
    private EntryField imageName;
    private EntryField imageTag;
    private EntryField registry;
    private EntryField username;
    private EntryField password;
    private JCheckBox analyticContainer;
    private JCheckBox deleteContainer;
    private JTextArea commandLine;

    private FieldListener listener;
    public ContainerNotebookEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    @Override
    protected JPanel buildMainPanel() {
        Module module = ModuleUtil.findModuleForFile(this.file, this.project);

        listener = new FieldListener(this::saveDocument);

        imageNamespace = new EntryField(module);
        imageName = new EntryField(module);
        imageTag = new EntryField(module);
        registry = new EntryField(module);
        username = new EntryField(module);
        password = new EntryField(module);
        analyticContainer = new JCheckBox();
        deleteContainer = new JCheckBox();
        commandLine = new JTextArea();
        FormPanel panel = new FormPanel(new Dimension(300,28));

        for (JComponent c:new JComponent[]{imageNamespace, imageName, imageTag,registry,username,password}){
            c.setPreferredSize(new Dimension(200,28));
        }
        panel.addField("Namespace",imageNamespace);
        panel.addField("Image name",imageName);
        panel.addField("Image tag",imageTag);
        panel.addField("Alternative registry",registry);
        panel.addField("Username",username);
        panel.addField("Password",password);
        panel.addField("Data kitchen image",analyticContainer);
        panel.addField("Delete container on finish",deleteContainer);

        JScrollPane scroll = new JBScrollPane(commandLine, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        commandLine.setLineWrap(true);
        commandLine.setFont(new Font("Monospaced",Font.PLAIN,13));
        scroll.setPreferredSize(new Dimension(400,100));
        panel.addField("Command line", scroll, new Dimension(400,100));

        listener.listen(imageName);
        listener.listen(imageTag);
        listener.listen(imageNamespace);
        listener.listen(registry);
        listener.listen(username);
        listener.listen(password);
        listener.listen(deleteContainer);
        listener.listen(analyticContainer);
        listener.listen(commandLine);

        analyticContainer.addChangeListener(e ->{
            commandLine.setEnabled(!analyticContainer.isSelected());
        });

        panel.setBorder(new EmptyBorder(10,10,10,10));
        return panel;
    }

    @Override
    protected void doSaveDocument(Map<String, Object> document) {
        super.doSaveDocument(document);
        document.put("image-repo", imageName.getText());
        document.put("image-tag",imageTag.getText());
        document.put("dockerhub-namespace",imageNamespace.getText());

        String username = this.username.getText();
        if (StringUtils.isNotBlank(username)) {
            document.put("dockerhub-username", username);
        }
        String password = this.password.getText();
        if (StringUtils.isNotBlank(password)){
            document.put("dockerhub-password", password);
        }
        String registry = this.registry.getText();
        if (StringUtils.isNotBlank(registry)){
            document.put("dockerhub-url", registry);
        }

        document.put("analytic-container", analyticContainer.isSelected());
        document.put("delete-container-when-complete", deleteContainer.isSelected());
        if (!analyticContainer.isSelected()){
            String commandLine = this.commandLine.getText();
            if (StringUtils.isNotBlank(commandLine)){
                document.put("command-line", commandLine);
            }
        }

    }

    @Override
    protected void doLoadDocument(Map<String, Object> document) {
        super.doLoadDocument(document);

        String imageName = (String)document.get("image-repo");
        String imageTag = (String)document.get("image-tag");
        String namespace = (String)document.get("dockerhub-namespace");
        String username = (String)document.get("dockerhub-username");
        String password = (String)document.get("dockerhub-password");
        String registry = (String)document.get("dockerhub-registry");
        Boolean analyticContainer = (Boolean)document.get("analytic-container");
        Boolean deleteContainer = (Boolean)document.get("delete-container-when-complete");
        String commandLine = (String)document.get("command-line");

        this.imageName.setText(StringUtils.defaultString(imageName, ""));
        this.imageTag.setText(StringUtils.defaultString(imageTag, ""));
        this.imageNamespace.setText(StringUtils.defaultString(namespace, ""));
        this.registry.setText(StringUtils.defaultString(registry, ""));
        this.username.setText(StringUtils.defaultString(username, ""));
        this.password.setText(StringUtils.defaultString(password, ""));

        this.deleteContainer.setSelected(deleteContainer == null ? true : deleteContainer);
        this.analyticContainer.setSelected(analyticContainer == null ? true : analyticContainer);
        this.commandLine.setText(StringUtils.defaultString(commandLine,""));
    }

    @Override
    protected void disableEvents() {
        listener.setEnabled(false);
    }

    @Override
    protected void enableEvents() {
        listener.setEnabled(true);
    }
}
