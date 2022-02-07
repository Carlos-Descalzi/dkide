package io.datakitchen.ide.model;

public interface FileKey extends Key{
    String getFile();
    void setFile(String file);
    boolean isWildcard();
}
