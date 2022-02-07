package io.datakitchen.ide.tools.sql;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDetailsView extends JBTabbedPane {

    private JTable columnsTable = new JBTable();
    private JTable pksTable = new JBTable();
    private JTable importedKeysTable = new JBTable();
    private JTable exportedKeysTable = new JBTable();

    public TableDetailsView(){
        addTab("Columns",new JBScrollPane(columnsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        addTab("Primary keys",new JBScrollPane(pksTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        addTab("Imported keys", new JBScrollPane(importedKeysTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        addTab("Exported keys", new JBScrollPane(exportedKeysTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }

    public void setTable(Connection connection, String catalog, String schema, String table){
        if (connection != null) {
            new Thread(()->{
                try {
                    DatabaseMetaData metadata = connection.getMetaData();
                    ResultSetModel columnsModel = new ResultSetModel(metadata.getColumns(catalog, schema, table, null));
                    ResultSetModel pksModel = new ResultSetModel(metadata.getPrimaryKeys(catalog, schema, table));
                    ResultSetModel importedKeysModel = new ResultSetModel(metadata.getImportedKeys(catalog, schema, table));
                    ResultSetModel exportedKeysModel = new ResultSetModel(metadata.getExportedKeys(catalog, schema, table));

                    SwingUtilities.invokeLater(()->{
                        columnsTable.setModel(columnsModel);
                        pksTable.setModel(pksModel);
                        importedKeysTable.setModel(importedKeysModel);
                        exportedKeysTable.setModel(exportedKeysModel);
                    });
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }).start();
        } else {
            columnsTable.setModel(new DefaultTableModel(new String[]{""},0));
            pksTable.setModel(new DefaultTableModel(new String[]{""},0));
            importedKeysTable.setModel(new DefaultTableModel(new String[]{""},0));
            exportedKeysTable.setModel(new DefaultTableModel(new String[]{""},0));
        }
    }

    private class ResultSetModel extends AbstractTableModel {

        private String[] columnNames;
        private List<String[]> items = new ArrayList<>();

        public ResultSetModel(ResultSet resultSet) {
            try {
                ResultSetMetaData metadata = resultSet.getMetaData();
                int columnCount = metadata.getColumnCount()-3; // skip catalog/schema/table

                columnNames = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    columnNames[i] = metadata.getColumnName(i + 4);
                }
                while (resultSet.next()) {
                    String[] row = new String[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = String.valueOf(resultSet.getObject(i + 4));
                    }
                    items.add(row);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        @Override
        public int getRowCount() {
            return items.size();
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            String[] row = items.get(rowIndex);
            return row[columnIndex];
        }
    }
}
