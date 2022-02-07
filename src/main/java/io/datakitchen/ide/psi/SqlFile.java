package io.datakitchen.ide.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class SqlFile extends PsiFileBase {
    protected SqlFile(@NotNull FileViewProvider provider) {
        super(provider, SqlLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return SqlFileType.INSTANCE;
    }

}
