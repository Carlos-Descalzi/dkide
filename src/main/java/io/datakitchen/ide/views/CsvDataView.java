package io.datakitchen.ide.views;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.ui.SimpleAction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.*;

public class CsvDataView extends JPanel implements Disposable, ClipboardOwner {
    private final Map userData = new HashMap();
    private final Project project;
    private final File file;
    private final JTextField fieldDelimiter = new JTextField(",");
    private final JTextField recordDelimiter = new JTextField("\\n");
    private final JCheckBox headers = new JCheckBox("Headers");
    private final JTable table = new JBTable();
    private final Action loadMoreAction = new SimpleAction("Load more",this::loadMore);
    private final Action copyToClipboardAction = new SimpleAction(AllIcons.General.CopyHovered, "Copy to clipboard",this::copyToClipboard);

    public CsvDataView(Project project, File file){
        this.project = project;
        this.file = file;
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new GridLayout(1,2));
        JPanel topPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel topPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(topPanel1);
        topPanel1.add(new JLabel("Field delimiter:"));
        topPanel1.add(fieldDelimiter);
        topPanel1.add(new JLabel("Record delimiter:"));
        topPanel1.add(recordDelimiter);
        topPanel1.add(headers);
        topPanel1.add(new JButton(copyToClipboardAction));


        add(topPanel,BorderLayout.NORTH);
        add(new JBScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
        topPanel2.add(new JButton(loadMoreAction));
        topPanel.add(topPanel2);
        fieldDelimiter.addActionListener(this::reload);
        recordDelimiter.addActionListener(this::reload);
        headers.addActionListener(this::reload);
        FocusListener listener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                loadFile();
            }
        };
        fieldDelimiter.addFocusListener(listener);
        recordDelimiter.addFocusListener(listener);
        loadFile();
    }

    private void reload(ActionEvent e){
        loadFile();
    }

    private void loadFile() {
        CsvModel model = new CsvModel(file, fieldDelimiter.getText(), recordDelimiter.getText(), headers.isSelected());
        table.setModel(model);
        loadMoreAction.setEnabled(model.valid());
    }

    @Override
    public boolean isValid() {
        return false;
    }

    private void loadMore(ActionEvent e){
        if (!((CsvModel)table.getModel()).loadMore()){
            loadMoreAction.setEnabled(false);
        }
    }

    public void dispose(){
        ((CsvModel)table.getModel()).dispose();
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }

    private static class CsvModel extends AbstractTableModel {
        private final List<List<String>> records = new ArrayList<>();
        private final boolean firstRowHeaders;
        private Iterator<CSVRecord> iterator;
        private List<String> headers = null;
        private CSVParser parser;

        public CsvModel(File file, String fieldDelimiter, String recordDelimiter, boolean headers){
            this.firstRowHeaders = headers;
            try (InputStream input = new FileInputStream(file)) {
                CSVFormat format = buildFormat(fieldDelimiter, recordDelimiter);
                parser = CSVParser.parse(new InputStreamReader(input), format);
                iterator = parser.iterator();
                loadMore();
            }catch (Exception ex){
                ex.printStackTrace();
                records.add(List.of("Unable to open file"));
            }
        }
        public boolean valid(){
            return iterator != null;
        }
        public void dispose() {
            iterator = null;
            if (parser != null){
                try {
                    parser.close();
                }catch(Exception ignored){}
            }
        }

        private CSVFormat buildFormat(String fieldDelimiter, String recordDelimiter) {
            return CSVFormat.Builder
                    .create()
                    .setDelimiter(parseDelimiter(fieldDelimiter))
                    .setRecordSeparator(parseDelimiter(recordDelimiter))
                    .build();
        }

        private String parseDelimiter(String fieldDelimiter) {
            switch(fieldDelimiter){
                case "\\n": return "\n";
                case "\\t": return "\t";
                case "\\b": return "\b";
                default: return fieldDelimiter.length() == 0 ? "," : fieldDelimiter;
            }
        }

        public boolean loadMore(){
            if (iterator != null) {
                int actualRows = records.size();
                for (int i = 0; i < 100 && iterator.hasNext(); i++) {
                    if (firstRowHeaders && headers == null){
                        headers = iterator.next().toList();
                    } else {
                        records.add(iterator.next().toList());
                    }
                }
                fireTableRowsInserted(actualRows, records.size() - 1);
                return iterator.hasNext();
            }
            return false;
        }

        @Override
        public String getColumnName(int column) {
            if (headers != null){
                return headers.get(column);
            }
            return "Column "+(column+1);
        }

        @Override
        public int getRowCount() {
            return records.size();
        }

        @Override
        public int getColumnCount() {
            return records.isEmpty() ? 0 : records.get(0).size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            List<String> row = records.get(rowIndex);

            return row.get(columnIndex);
        }

    }

    private static final DataFlavor[] FLAVORS = new DataFlavor[]{
        DataFlavor.javaFileListFlavor,
                DataFlavor.stringFlavor
    };


    private void copyToClipboard(ActionEvent event) {

        Transferable t = new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return FLAVORS;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return Set.of(FLAVORS).contains(flavor);
            }

            @NotNull
            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (DataFlavor.javaFileListFlavor.equals(flavor)){
                    return List.of(file);
                } else if (DataFlavor.stringFlavor.equals(flavor)){
                    return FileUtils.readFileToString(file, Charset.defaultCharset());
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };
        getToolkit().getSystemClipboard().setContents(t, this);
    }

}
