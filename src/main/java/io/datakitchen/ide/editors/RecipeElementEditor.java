package io.datakitchen.ide.editors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.tests.TestsEditor;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import net.minidev.json.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class RecipeElementEditor extends AbstractFileEditor {

    protected final Project project;
    protected final VirtualFile file;
    protected final JPanel contentPane = new JPanel(new BorderLayout());
    private final JTabbedPane tabs = new JBTabbedPane();
    private final TestsEditor testsEditor;
    private final DocumentChangeListener testEditorListener = (DocumentChangeEvent e) ->saveDocument();
    private boolean documentLoaded = false;

    public RecipeElementEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        testsEditor = new TestsEditor(project);
        Disposer.register(this, testsEditor);
        testsEditor.setBorder(JBUI.Borders.empty(10));
        Map<String,JComponent> tabs = getTabs();
        for (Map.Entry<String, JComponent> entry : tabs.entrySet()) {
            this.tabs.addTab(entry.getKey(), entry.getValue());
        }
        this.tabs.addTab("Tests", testsEditor);
        this.tabs.addChangeListener(e-> this.tabs.requestFocus());
        loadDocument();
    }

    protected void saveDocument(){
        if (documentLoaded) {
            ApplicationManager.getApplication().invokeLater(() -> {
                Map<String, Object> document = new LinkedHashMap<>();
                doSaveDocument(document);
                document.put("tests", testsEditor.getTests());
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        JsonUtil.write(document, this.file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            });
        }
    }

    protected void loadDocument(){
        this.file.refresh(true,true,()->{
            try {
                Map<String, Object> document = JsonUtil.read(this.file);
                SwingUtilities.invokeLater(() -> {
                    try {
                        documentLoaded = false;
                        stopEvents();
                        doLoadDocument(document);
                        testsEditor.setTests(ObjectUtil.cast(document.get("tests")));
                        documentLoaded = true;
                        doShow();
                    }catch(ParseException ex){
                        documentLoaded = false;
                        SwingUtilities.invokeLater(this::doShow);
                    }
                });
            }catch (ParseException ex){
                documentLoaded = false;
                SwingUtilities.invokeLater(this::doShow);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    protected void doShow(){
        contentPane.removeAll();
        if (documentLoaded){
            contentPane.add(tabs, BorderLayout.CENTER);
            onEditorReady();
            startEvents();
        } else {
            contentPane.add(createErrorMessagePanel(), BorderLayout.CENTER);
        }
        contentPane.validate();
        contentPane.repaint();

    }

    @NotNull
    private JPanel createErrorMessagePanel() {
        JPanel panel1 = new JPanel(new VerticalFlowLayout( VerticalFlowLayout.MIDDLE,false, false));
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel2.add(panel1);

        panel1.add(new JLabel("Form cannot be displayed since document is not well-formed JSON."));
        panel1.add(new JButton(new SimpleAction("Open with text editor", this::switchToSourceView)));
        return panel2;
    }

    protected void switchToSourceView(ActionEvent event) {
        FileEditorManager mgr = FileEditorManager.getInstance(project);
        ApplicationManager.getApplication().putUserData(RecipeUtil.CUSTOM_EDITOR_ENABLED, false);
        mgr.closeFile(file);
        mgr.openFile(file, true);
        ApplicationManager.getApplication().putUserData(RecipeUtil.CUSTOM_EDITOR_ENABLED, true);
    }

    private void stopEvents(){
        testsEditor.removeDocumentChangeListener(this.testEditorListener);
        disableEvents();
    }

    private void startEvents(){
        testsEditor.addDocumentChangeListener(this.testEditorListener);
        enableEvents();
    }

    protected void onEditorReady(){}

    protected abstract void disableEvents();

    protected abstract void enableEvents();

    protected abstract Map<String, JComponent> getTabs();

    protected abstract void doLoadDocument(Map<String,Object> document) throws ParseException;

    protected abstract void doSaveDocument(Map<String,Object> document);

    @Override
    public void deselectNotify() {
        saveDocument();
        stopEvents();
    }

    @Override
    public void selectNotify() {
        startEvents();
    }
    @Override
    public @NotNull JComponent getComponent() {
        return contentPane;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return tabs;
    }

    public @NotNull VirtualFile getFile(){
        return file;
    }
}
