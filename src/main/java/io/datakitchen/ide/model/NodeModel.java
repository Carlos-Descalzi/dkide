package io.datakitchen.ide.model;

import com.intellij.openapi.module.Module;

import java.util.Set;

public interface NodeModel {
    void addNodeModelListener(NodeModelListener listener);
    void removeNodeModelListener(NodeModelListener listener);
    String getDescription();
    void setDescription(String description);
    Set<Test> getTests();
    void addTest(Test test);
    void removeTest(Test test);
    Module getModule();

    String getNodeName();
}
