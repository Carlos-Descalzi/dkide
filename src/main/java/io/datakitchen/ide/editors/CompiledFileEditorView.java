package io.datakitchen.ide.editors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.palette.ModuleComponentSource;
import io.datakitchen.ide.service.CompilerService;
import io.datakitchen.ide.tools.sql.SqlExecutionBar;
import io.datakitchen.ide.ui.EditorUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import javax.swing.*;
import java.awt.*;

public class CompiledFileEditorView extends AbstractFileEditor implements SqlExecutionBar.StatementSource {

    private final Project project;
    private final VirtualFile file;
    private final JPanel panel = new JPanel();
    private final JLabel status = new JLabel();
    private final Editor editor;

    public CompiledFileEditorView(Project project, VirtualFile file){
        this.project = project;
        this.file = file;
        panel.setLayout(new BorderLayout());
        status.setPreferredSize(new Dimension(100,28));
        editor = EditorUtil.createViewer(project,file.getName());

        panel.add(editor.getComponent(),BorderLayout.CENTER);
        panel.add(status, BorderLayout.SOUTH);

        if (file.getName().endsWith(".sql")){
            Module module = ModuleUtil.findModuleForFile(file, project);
            SqlExecutionBar sqlExecutionBar = new SqlExecutionBar(
                    project,
                    this,
                    new ModuleComponentSource(module)
            );
            panel.add(sqlExecutionBar,BorderLayout.NORTH);
        }
    }

    public void dispose(){
        super.dispose();
        EditorFactory.getInstance().releaseEditor(editor);
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
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
        return "Compiled view";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void selectNotify() {
        ApplicationManager.getApplication().saveAll();
        SwingUtilities.invokeLater(()->{
            status.setText("Updating ...");
        });
        ApplicationManager.getApplication().invokeLater(this::updateView);
    }

    private void updateView() {
        compile(file);
    }

    private void compile(VirtualFile inFile) {

        showText("Compiling, please wait ...");
        Module module = ModuleUtil.findModuleForFile(file, project);
        CompilerService compilerService = CompilerService.getInstance(module);

        compilerService.compileFile(inFile, (String content)->{
            showText(content);
        });

    }

    public void showText(String str) {
        SwingUtilities.invokeLater(()->{
            ApplicationManager.getApplication().runWriteAction(()->{
                editor.getDocument().setText(str);
            });
            status.setText("");
        });
    }

    @Override
    public Promise<String> getSql() {
        AsyncPromise<String> p = new AsyncPromise<>();
        p.setResult(editor.getDocument().getText());
        return p;
    }
}
