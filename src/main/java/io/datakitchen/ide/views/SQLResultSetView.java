package io.datakitchen.ide.views;

import com.intellij.openapi.Disposable;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLResultSetView extends JBTabbedPane implements Disposable {

    private SQLResultSetModel resultSetModel;
    private final Action loadMoreAction = new SimpleAction("Load more",this::loadMore);
    private final ResultSet resultSet;

    public SQLResultSetView(ResultSet resultSet){
        this.resultSet = resultSet;
        if (resultSet != null) {
            try {
                ResultSetMetaData metadata = resultSet.getMetaData();

                addTab("Result set", buildResultSetView(resultSet, metadata));
                addTab("Metadata", buildMetadataView(metadata));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            addTab("Query Result", new JLabel("Statement executed successfully")); // TODO improve this
        }
    }

    public void dispose(){
        try {
            this.resultSet.close();
        }catch (Exception ignored){}
    }

    private void loadMore(ActionEvent event) {
        doLoadMore();
    }

    private void doLoadMore() {
        loadMoreAction.setEnabled(false);
        new Thread(()->{
            if (resultSetModel.fetchMore()){
                SwingUtilities.invokeLater(()-> loadMoreAction.setEnabled(true));
            }
        }).start();
    }

    private Component buildMetadataView(ResultSetMetaData metadata) {
        return new JBScrollPane(new JBTable(new SQLMetaDataModel(metadata)),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private Component buildResultSetView(ResultSet resultSet, ResultSetMetaData metadata) throws SQLException{

        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        resultSetModel = new SQLResultSetModel(resultSet,metadata);

        buttons.add(new JButton(loadMoreAction));
        panel.add(buttons,BorderLayout.NORTH);
        panel.add(new JBScrollPane(new JBTable(resultSetModel), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        return panel;
    }

    private static class SQLMetaDataModel extends AbstractTableModel {
        private final String[] columnNames = { "Column name", "Column type", "Nullable", "Autoincrement", "Scale","Presicion"};
        private final ResultSetMetaData metaData;

        public SQLMetaDataModel(ResultSetMetaData metaData){
            this.metaData = metaData;
        }

        @Override
        public int getRowCount() {
            try {
                return metaData.getColumnCount();
            }catch (Exception ex){
                return 0;
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                switch (columnIndex) {
                    case 0:
                        return metaData.getColumnName(rowIndex + 1);
                    case 1:
                        return metaData.getColumnTypeName(rowIndex +1);
                    case 2:
                        return String.valueOf(metaData.isNullable(rowIndex+1));
                    case 3:
                        return String.valueOf(metaData.isAutoIncrement(rowIndex+1));
                    case 4:
                        return String.valueOf(metaData.getScale(rowIndex+1));
                    case 5:
                        return String.valueOf(metaData.getPrecision(rowIndex+1));
                }
            }catch (SQLException ex){
                return null;
            }
            return null;
        }
    }

    private class SQLResultSetModel extends AbstractTableModel {
        private final ResultSet resultSet;
        private final int columnCount;
        private final String[] columnNames;
        private final Class<?>[] columnTypes;
        private final List<List<Object>> rows = new ArrayList<>();
        private boolean done;

        public SQLResultSetModel(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
            this.resultSet = resultSet;
            columnCount = metaData.getColumnCount();
            columnNames = new String[columnCount];
            columnTypes = new Class[columnCount];

            for (int i=0;i<columnCount;i++){
                columnNames[i] = metaData.getColumnName(i+1);
                try {
                    columnTypes[i] = Class.forName(metaData.getColumnClassName(i + 1));
                }catch (Exception ex){
                    columnTypes[i] = Object.class;
                }
            }
            doLoadMore();
        }

        public boolean fetchMore(){
            final int start = rows.size();
            int count = 0;
            for (int i=0;i<100 && !done; i++){

                try {
                    if (resultSet.next()) {
                        List<Object> row = new ArrayList<>();
                        for (int j=0;j<columnCount;j++){
                            row.add(resultSet.getObject(j+1));
                        }
                        rows.add(row);
                        count++;
                    } else {
                        done = true;
                    }
                }catch(SQLException ex){
                    done = true;
                }
            }
            final int end = start+count-1;
            SwingUtilities.invokeLater(()-> fireTableRowsInserted(start,end));

            return !done;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columnCount;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return rows.get(rowIndex).get(columnIndex);
        }
    }
}
