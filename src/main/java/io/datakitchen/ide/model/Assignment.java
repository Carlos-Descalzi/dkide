package io.datakitchen.ide.model;

public class Assignment {
    private String file;
    private String variable;

    public Assignment() {
    }

    public Assignment(String file, String variable) {
        this.file = file;
        this.variable = variable;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }
}
