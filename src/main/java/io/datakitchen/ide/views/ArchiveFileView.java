package io.datakitchen.ide.views;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.tools.OutputFormats;
import io.datakitchen.ide.util.NumberUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class ArchiveFileView extends JPanel implements Disposable {

    public ArchiveFileView(Project project, File file, String format){
        JTable table = new JBTable(new ArchiveTableModel(project, file, format));

        setLayout(new BorderLayout());
        add(new JBScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

    }

    @Override
    public void dispose() {

    }

    private static class ArchiveTableModel extends AbstractTableModel {

        private static final String[] COLUMN_NAMES = {"Path","Size","Last Modified"};
        private final List<String[]> rows = new ArrayList<>();

        public ArchiveTableModel(Project project, File file, String format) {
            if (format.equals(OutputFormats.FOLDER)){
                for (File aFile:FileUtils.listFiles(file,null,true)){
                    rows.add(new String[]{
                        file.getPath(),
                        NumberUtil.formatInBytes(file.length()),
                        new Date(file.lastModified()).toString()
                    });
                }
            } else {
                try {
                    ArchiveInputStream inputStream = createInputStream(file, format);

                    if (inputStream != null) {
                        ArchiveEntry entry;

                        while ((entry = inputStream.getNextEntry()) != null) {
                            rows.add(new String[]{
                                entry.getName(),
                                NumberUtil.formatInBytes(entry.getSize()),
                                entry.getLastModifiedDate().toString()
                            });
                        }
                        inputStream.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        private ArchiveInputStream createInputStream(File file, String format) throws IOException {
            if (format == null){
                format = OutputFormats.getByExtension(file.getName());
            }

            if (format != null){
                switch (format) {
                    case OutputFormats.ZIP:
                        return new ZipArchiveInputStream(new FileInputStream(file));
                    case OutputFormats.TAR:
                        return new TarArchiveInputStream(new FileInputStream(file));
                    case OutputFormats.TAR_GZ:
                        return new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(file)));
                    case OutputFormats.TAR_BZ2:
                        return new TarArchiveInputStream(new CBZip2InputStream(new FileInputStream(file)));
                }
            }

            return null;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String[] item = rows.get(rowIndex);
            return item[columnIndex];
        }
    }
}
