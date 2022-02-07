package io.datakitchen.ide.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SqlElementType extends IElementType {
    public SqlElementType(@NonNls @NotNull String debugName) {
        super(debugName, SqlLanguage.INSTANCE);
    }
}
