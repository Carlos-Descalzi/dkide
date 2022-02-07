package io.datakitchen.ide.tools;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class ScriptResultsView extends AbstractItemListView {

    private final VirtualFile outputFolder;
    private final VirtualFile dockerShareFolder;
    private final JEditorPane textArea = new JEditorPane();

    public ScriptResultsView(Project project, VirtualFile dockerShareFolder, VirtualFile outputFolder){
        super(project);
        this.outputFolder = outputFolder;
        this.dockerShareFolder = dockerShareFolder;

        contentPane.add(new JBScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        textArea.setFont(new Font("Monospaced",Font.PLAIN,12));

        refresh();
    }

    public void refresh() {

        List<File> fileNames = Arrays.asList(new File(outputFolder.getPath()).listFiles());
        List<File> dockerShareFileNames = Arrays.asList(new File(dockerShareFolder.getPath()).listFiles());

        fileNames.removeAll(dockerShareFileNames);

        itemList.setModel(new DefaultComboBoxModel<>(fileNames.toArray(File[]::new)));
    }
    protected void showItem(){
        if (itemList.getSelectedIndex() != -1) {
            File file = (File) itemList.getSelectedValue();
            JTabbedPane pane = new JBTabbedPane();
            try (InputStream in = new FileInputStream(file)){
                textArea.setText(IOUtils.toString(new InputStreamReader(in)));
            } catch(Exception ex){

            }
        }
    }

}
