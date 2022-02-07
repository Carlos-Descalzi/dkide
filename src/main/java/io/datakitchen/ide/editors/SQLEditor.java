package io.datakitchen.ide.editors;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.palette.ModuleComponentSource;
import io.datakitchen.ide.psi.SqlFileType;
import io.datakitchen.ide.service.CompilerService;
import io.datakitchen.ide.tools.sql.SqlExecutionBar;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import javax.swing.*;
import java.awt.*;

public class SQLEditor extends AbstractFileEditor{

    private final Editor editor;
    private final JPanel panel = new JPanel(new BorderLayout());
    private final VirtualFile file;
    private final Module module;

    public SQLEditor(Project project, VirtualFile file){
        this.file = file;
        this.module = ModuleUtil.findModuleForFile(file, project);
        Document document = FileDocumentManager.getInstance().getDocument(file);
        assert document != null;
        editor = EditorFactory.getInstance().createEditor(document, project, SqlFileType.INSTANCE, false);
        SqlExecutionBar toolBar = new SqlExecutionBar(project, this::getSql, new ModuleComponentSource(module));
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(editor.getComponent(), BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
        super.dispose();
        EditorFactory.getInstance().releaseEditor(editor);
    }

    private Promise<String> getSql() {
        AsyncPromise<String> promise = new AsyncPromise<>();
        String sqlRaw = editor.getDocument().getText();
        CompilerService.getInstance(module).compileText(sqlRaw, promise::setResult);
        return promise;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return editor.getContentComponent();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "SQL Editor";
    }

    @Override
    public @Nullable VirtualFile getFile() {
        return file;
    }
}
