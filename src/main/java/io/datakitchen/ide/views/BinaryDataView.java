package io.datakitchen.ide.views;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.ObjectUtil;
import net.razorvine.pickle.Unpickler;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class BinaryDataView extends JPanel implements Disposable {
    private final Map userData = new HashMap<>();
    private final Project project;
    private final File file;
    private final JTable table = new JBTable();
    private final Action loadMoreAction = new SimpleAction("Load more",this::loadMore);

    public BinaryDataView(Project project, File file){
        this.project = project;
        this.file = file;
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        add(topPanel,BorderLayout.NORTH);
        add(new JBScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
        topPanel.add(new JButton(loadMoreAction));
        loadFile();
    }

    private void loadFile() {
        try {
            CsvModel model = new CsvModel(new FileInputStream(file));
            table.setModel(model);
            loadMoreAction.setEnabled(model.valid());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void dispose(){
        if (table.getModel() instanceof CsvModel){
            ((CsvModel)table.getModel()).dispose();
        }
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

    private static class CsvModel extends AbstractTableModel {
        private final List<List<String>> records = new ArrayList<>();
        private Unpickler unpickler;
        private InputStream input;

        public boolean valid(){
            try {
                return input.available() > 0;
            }catch(Exception ex){
                return false;
            }
        }

        public CsvModel(InputStream input){

            try {
                this.input = input;
                unpickler = new Unpickler();
                loadMore();
            }catch (Exception ex){
                ex.printStackTrace();
                records.add(List.of("Unable to open file"));
            }
        }

        private String convertValue(Object obj){
            if (obj instanceof GregorianCalendar){
                return ((GregorianCalendar)obj).getTime().toString();
            }
            return String.valueOf(obj);
        }

        public boolean loadMore(){
            try {
                if (input != null){
                    for (int i=0;i<100 && input.available() > 0;i++){
                        Object row = unpickler.load(input);
                        if (row instanceof Object[]){
                            records.add(
                                Arrays.stream((Object[])row)
                                .map(this::convertValue)
                                .collect(Collectors.toList())
                            );
                        } else if (row instanceof List){
                            records.add(ObjectUtil.cast(row));
                        }

                    }
                    boolean hasMore = input.available() > 0;
                    if (input.available() == 0){
                        input.close();
                        input = null;
                    }
                    return hasMore;
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
            return false;
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

        public void dispose() {
            try {
                input.close();
            }catch (Exception ignored){}
        }
    }
}
