package io.datakitchen.ide.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.ObjectUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public class RecipeDescriptionEditor implements FileEditor, DumbAware {

    private final Map<Object,Object> userData = new HashMap<>();
    private final Project project;
    private final VirtualFile file;
    private final FormPanel panel = new FormPanel();
    private final JEditorPane description = new JEditorPane();

    public RecipeDescriptionEditor(Project project, VirtualFile file){
        this.project = project;
        this.file = file;
        panel.setBorder(JBUI.Borders.empty(10));
        JScrollPane scroll = new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new LineBorder(panel.getBackground().brighter()));
        p.add(scroll, BorderLayout.CENTER);
        panel.addField("Recipe Description", p, new Dimension(800,500));
    }
    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Description Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return ObjectUtil.cast(userData.get(key));
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        userData.put(key, value);
    }

    public @NotNull VirtualFile getFile(){
        return file;
    }

}
