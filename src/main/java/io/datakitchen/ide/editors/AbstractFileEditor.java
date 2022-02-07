package io.datakitchen.ide.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;
import io.datakitchen.ide.ui.EventSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFileEditor implements FileEditor, DumbAware {
    private final Map<Key<?>, Object> userData = new HashMap<>();
    private final EventSupport<PropertyChangeListener> eventSupport = EventSupport.of(PropertyChangeListener.class);

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        eventSupport.addListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        eventSupport.removeListener(listener);
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return (T)userData.get(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        userData.put(key, value);
    }


    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }

}
