package io.datakitchen.ide.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.editors.neweditors.palette.ModuleComponentSource;
import io.datakitchen.ide.service.CompilerService;
import io.datakitchen.ide.tools.sql.SqlExecutionBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class SQLKeyEditorField extends JBTabbedPane implements DocumentEditor, DocumentListener, SqlExecutionBar.StatementSource, Disposable {
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final Editor editor;
    private final Editor preview;
    private final Module module;

    public SQLKeyEditorField(Module module){
        this.module = module;
        Project project = module.getProject();
        editor = EditorUtil.createSqlEditor(project);
        preview = EditorUtil.createSqlViewer(project);

        addTab("Editor",editor.getComponent());

        JPanel previewPanel = new JPanel(new BorderLayout());
        SqlExecutionBar sqlExecutionBar = new SqlExecutionBar(project, this, new ModuleComponentSource(module));
        previewPanel.add(sqlExecutionBar,BorderLayout.NORTH);
        previewPanel.add(preview.getComponent());
        setSelectedIndex(0);
        addChangeListener(this::tabChanged);
        editor.getDocument().addDocumentListener(this);
        addTab("Preview",previewPanel);
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
        EditorFactory.getInstance().releaseEditor(preview);
    }

    private void tabChanged(ChangeEvent changeEvent) {
        if (getSelectedIndex() == 1) {
            String text = editor.getDocument().getText();
            compile(text);
        }
    }
    private void compile(String text) {
        showText("Compiling, please wait ...");
        CompilerService compilerService = CompilerService.getInstance(module);

        compilerService.compileText(text, this::showText);
    }

    public void showText(String str) {
        SwingUtilities.invokeLater(()->{
            ApplicationManager.getApplication().runWriteAction(()->{
                preview.getDocument().setText(str.replace("\\n","\n").replace("\\t","\t"));
            });
        });
    }
    public String getText(){
        return editor.getDocument().getText();
    }

    public void setText(String text){
        editor.getDocument().removeDocumentListener(this);
        EditorUtil.setText(editor, text);
        setSelectedIndex(0);
        editor.getDocument().addDocumentListener(this);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        editor.getContentComponent().setEnabled(enabled);
    }

    @Override
    public void addDocumentChangeListener(DocumentChangeListener listener) {
        eventSupport.addListener(listener);
    }

    @Override
    public void removeDocumentChangeListener(DocumentChangeListener listener) {
        eventSupport.removeListener(listener);
    }

    @Override
    public JComponent getEditorComponent() {
        return this;
    }

    public void documentChanged(@NotNull DocumentEvent event) {
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    @Override
    public Promise<String> getSql() {
        AsyncPromise<String> p = new AsyncPromise<>();
        p.setResult(preview.getDocument().getText());
        return p;
    }

}
