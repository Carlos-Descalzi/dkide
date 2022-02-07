package io.datakitchen.ide.psi;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SqlFileType implements FileType {

    public static final SqlFileType INSTANCE = new SqlFileType();

    @Override
    public @NonNls @NotNull String getName() {
        return "SQL File";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "SQL File";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "sql";
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.FileTypes.Custom;
    }

    @Override
    public boolean isBinary() {
        return false;
    }
}
