package io.datakitchen.ide.editors.tests;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.ItemList;
import io.datakitchen.ide.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestsEditor extends JPanel implements Disposable {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);
    private final ItemList<Test> testsList = new ItemList<>(this::createTest, true);
    private final DocumentChangeListener testEditorListener = e -> fireUpdate();
    private final ListSelectionListener listListener = e -> selectTest();
    private final TestEditor testEditor;

    private Test currentTest;

    public TestsEditor(Project project){

        setLayout(new BorderLayout());

        add(testsList, BorderLayout.WEST);
        testEditor = new TestEditor(project);
        Disposer.register(this, testEditor);
        testEditor.setBorder(UIUtil.EMPTY_BORDER_10x10);
        add(testEditor, BorderLayout.CENTER);
        updateActions();
        enableEvents();
    }

    private Test createTest() {
        return new Test("test-"+(testsList.getDataSize()+1));
    }

    private void enableEvents(){
        testEditor.addDocumentChangeListener(testEditorListener);
        testsList.addListSelectionListener(listListener);
        testsList.addDocumentChangeListener(testEditorListener);
    }

    private void disableEvents(){
        testEditor.removeDocumentChangeListener(testEditorListener);
        testsList.removeListSelectionListener(listListener);
        testsList.removeDocumentChangeListener(testEditorListener);
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }

    private void fireUpdate(){
        testsList.repaint();
        testEditor.saveTest(currentTest);
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    private void updateActions(){
        testEditor.setEnabled(currentTest != null);
    }


    private void selectTest(){
        disableEvents();
        if (currentTest != null){
            testEditor.saveTest(currentTest);
        }

        currentTest = testsList.getSelected();

        testEditor.loadTest(currentTest);
        updateActions();
        enableEvents();
    }

    public void setTests(Map<String,Object> tests) {
        disableEvents();
        testEditor.loadTest(null);

        List<Test> testList = new ArrayList<>();
        if (tests != null) {
            testList.addAll(tests.entrySet().stream().map(Test::fromEntry).collect(Collectors.toList()));

        }
        this.testsList.setData(testList);
        updateActions();
        enableEvents();
        if (testList.size() > 0){
            this.testsList.setSelectedIndex(0);
        }
    }

    public Map<String,Object> getTests() {
        Map<String,Object> testsJson = new LinkedHashMap<>();

        for (Test test: testsList.getData()){
            if (test.isValid()) {
                testsJson.put(test.getName(), test.getTest());
            }
        }

        return testsJson;
    }

    @Override
    public void dispose() {

    }
}
