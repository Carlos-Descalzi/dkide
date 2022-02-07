package io.datakitchen.ide.tools;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.ui.ListListModel;
import io.datakitchen.ide.views.ArchiveFileView;
import io.datakitchen.ide.views.BinaryDataView;
import io.datakitchen.ide.views.CsvDataView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseResultsView extends AbstractItemListView{

    protected final VirtualFile folder;

    public BaseResultsView(Project project, VirtualFile folder) {
        super(project);
        this.folder = folder;
    }

    @Override
    public void refresh() {
        if (this.folder != null) {
            onRefresh();
            // VirtualFile interface doesn't work well in this case with temporary files.
            // Refresh sometimes doesn't work.
            File file = new File(folder.getPath());
            Collection<File> files = FileUtils.listFiles(file, null, true);
            ListModel<File> model =
                new ListListModel<>(
                    files
                        .stream()
                            .sorted(this::compareFiles)
                        .filter(File::isFile)
                        .collect(Collectors.toList()));
            SwingUtilities.invokeLater(()->{
                itemList.setModel(model);
            });
        }
    }

    private final static List<String> FILE_ORDER = Arrays.asList("compiled.json","runtime-vars.json","progress.json");

    private int compareFiles(File file1, File file2) {
        String fileName1 = file1.getName();
        String fileName2 = file2.getName();

        int index1 = FILE_ORDER.indexOf(fileName1);
        int index2 = FILE_ORDER.indexOf(fileName2);

        if (index1 != -1 && index2 != -1){
            return index1 - index2;
        } else if (index1 != -1){
            return -index1;
        } else if (index2 != -1){
            return index2;
        }

        return fileName1.compareTo(fileName2);
    }

    @Override
    protected void showItem() {
        if (itemList.getSelectedIndex() != -1) {
            File file = (File)itemList.getSelectedValue();
            contentPane.removeAll();
            if (file.getName().equals("progress.json")){
                contentPane.add(new ProgressView(file), BorderLayout.CENTER);
            } else if (file.getName().endsWith(".output")){
                String format = getFileFormat(file);
                if (file.isDirectory()){
                    contentPane.add(new ArchiveFileView(project, file, OutputFormats.FOLDER), BorderLayout.CENTER);
                } else if (OutputFormats.CSV.equals(format)){
                    contentPane.add(new CsvDataView(project, file), BorderLayout.CENTER);
                } else if (OutputFormats.BINARY.equals(format)){
                    contentPane.add(new BinaryDataView(project, file), BorderLayout.CENTER);
                } else if (OutputFormats.ARCHIVE_FORMATS.contains(format)) {
                    contentPane.add(new ArchiveFileView(project, file, format), BorderLayout.CENTER);
                } else {
                    showAsText(file);
                }
            } else {
                showAsText(file);
            }
            contentPane.validate();
        }
    }

    protected void showAsText(File file) {
        try {
            String text = IOUtils.toString(new FileReader(file));
            JTextPane textPane = new JTextPane();
            textPane.setText(text);
            textPane.setFont(new Font("Monospaced",Font.PLAIN,13));
            contentPane.add(new JBScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    protected abstract void onRefresh();

    protected abstract String getFileFormat(File file);

}
