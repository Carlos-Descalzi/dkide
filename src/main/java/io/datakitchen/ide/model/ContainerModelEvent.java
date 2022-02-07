package io.datakitchen.ide.model;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.EventObject;
import java.util.List;

public class ContainerModelEvent extends EventObject {
    private List<VirtualFile> files;
    private Assignment assignment;
    private String propertyName;
    private Object oldValue;
    private Object newValue;
    private boolean newFile;
    private boolean testFile;
    private VirtualFile file;
    private String oldName;

    public ContainerModelEvent(Object source, VirtualFile file, String oldName){
        super(source);
        this.file = file;
        this.oldName = oldName;
    }

    public ContainerModelEvent(Object source, List<VirtualFile> files, boolean testFile){
        this(source, files, false, testFile);
    }

    public ContainerModelEvent(Object source, List<VirtualFile> files, boolean newFile, boolean testFile){
        super(source);
        this.files = files;
        this.newFile = newFile;
        this.testFile = testFile;
    }

    public ContainerModelEvent(Object source, Assignment assignment){
        super(source);
        this.assignment = assignment;
    }

    public ContainerModelEvent(Object source, String propertyName, Object oldValue, Object newValue){
        super(source);
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public List<VirtualFile> getFiles() {
        return files;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public boolean isNewFile() {
        return newFile;
    }

    public boolean isTestFile() {
        return testFile;
    }

    public VirtualFile getFile() {
        return file;
    }

    public String getOldName() {
        return oldName;
    }
}
