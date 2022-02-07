package io.datakitchen.ide.editors.diff;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.impl.DiffRequestPanelImpl;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.AbstractFileEditor;
import io.datakitchen.ide.ui.SimpleAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class PlainDiffEditor extends AbstractFileEditor {

    private final Project project;
    private final VirtualFile file;
    private final DiffRequestPanelImpl diffPanel;
    private final JPanel panel;

    private DocumentContent currentContent;

    public PlainDiffEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;

        diffPanel = new DiffRequestPanelImpl(project, null);
        panel = new JPanel(new BorderLayout());
        panel.add(diffPanel.getComponent(), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Action saveAction = new SimpleAction("Save", this::save);
        buttons.add(new JButton(saveAction));
        Action reloadAction = new SimpleAction("Reload", this::reload);
        buttons.add(new JButton(reloadAction));
        panel.add(buttons, BorderLayout.SOUTH);

        loadFile();
    }

    private void loadFile() {
        try {
            DiffFileLoader loader = new DiffFileLoader(file);
            loader.load();

            DiffContentFactory factory = DiffContentFactory.getInstance();

            DocumentContent left = factory.createEditable(project,loader.getLeft(), PlainTextFileType.INSTANCE);
            DocumentContent right = factory.createEditable(project,loader.getRight(), PlainTextFileType.INSTANCE);

            SimpleDiffRequest request = new SimpleDiffRequest(
                    file.getName(),
                    left,
                    right,
                    "Local",
                    "Remote");

            diffPanel.setRequest(request);
            currentContent = left;
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return diffPanel.getPreferredFocusedComponent();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Diff";
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }

    private void save(ActionEvent event) {
        if (currentContent != null){
            ApplicationManager.getApplication().runWriteAction(()->{
                String text = currentContent.getDocument().getText();

                try (OutputStream output = file.getOutputStream(PlainDiffEditor.this)){
                    OutputStreamWriter writer = new OutputStreamWriter(output);
                    writer.write(text);
                    writer.flush();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
        }
    }
    private void reload(ActionEvent event) {
        loadFile();
    }


}
