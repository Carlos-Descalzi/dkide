package io.datakitchen.ide.debugger;

import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.xdebugger.XSourcePosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SourcePosition implements XSourcePosition {

    private final VirtualFile file;
    private final int line;

    public static SourcePosition create(List<VirtualFile> sourceFolders, String containerBasePath, String filename, int line){
        if(filename.contains(containerBasePath)) {
            String relativePath = filename.substring(containerBasePath.length() + 1);
            for (VirtualFile sourceFolder : sourceFolders) {
                VirtualFile item = sourceFolder.findFileByRelativePath(relativePath);
                if (item != null) {
                    return new SourcePosition(item, line);
                }
            }
        }
        return null;
    }
    private SourcePosition(VirtualFile file, int line){
        this.file = file;
        this.line = line;
    }


    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getOffset() {
        return -1;
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }

    @Override
    public @NotNull Navigatable createNavigatable(@NotNull Project project) {
        VirtualFile file = getFile();
        return this.getOffset() != -1
            ? PsiNavigationSupport.getInstance().createNavigatable(project, file, this.getOffset())
            : new OpenFileDescriptor(project, file, this.getLine(), 0);
    }
}
