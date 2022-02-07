package io.datakitchen.ide.hooks;

public interface ActionHook {
    String[] getActions();
    boolean before(String recipe, String selectedFile);
    void after(String recipe, String selectedFile);
}
