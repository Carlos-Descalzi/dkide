package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileChooserDialog extends DialogWrapper {

    private final JFileChooser chooser = new JFileChooser();

    public FileChooserDialog(){
        super(true);
        init();
    }

    public void setFileFilter(FileFilter filter){
        chooser.setFileFilter(filter);
    }
    @Override
    protected @Nullable JComponent createCenterPanel() {
        return chooser;
    }

    public File getSelectedFile(){
        return chooser.getSelectedFile();
    }
}
