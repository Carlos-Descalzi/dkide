package io.datakitchen.ide.service;

@FunctionalInterface
public interface PullProgressListener {
    void progress(String id, boolean success, Long current, Long total, String errorMessage);
}
