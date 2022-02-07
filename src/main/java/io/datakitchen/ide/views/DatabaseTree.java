package io.datakitchen.ide.views;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.AbstractTreeModel;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTree extends JPanel {
    private Connection connection;
    private JTree tree = new Tree();
    public DatabaseTree(Connection connection){
        this.connection = connection;
        setLayout(new BorderLayout());
        add(new JBScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
    }

    public void refresh(){

    }

    private class CatalogNode {
        private String name;
        private List<String> schemas = new ArrayList<>();
        private DatabaseTreeModel model;

        public CatalogNode(DatabaseTreeModel model, String name){
            this.model = model;
            this.name = name;
        }
    }

    private class DatabaseTreeModel extends AbstractTreeModel {

        public DatabaseTreeModel(Connection connection){
            try {
                DatabaseMetaData metadata = connection.getMetaData();
                ResultSet catalogRs = metadata.getCatalogs();
                List<CatalogNode> catalogs = new ArrayList<>();
                while (catalogRs.next()) {
                    catalogs.add(new CatalogNode(this, catalogRs.getString("TABLE_CAT")));
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        @Override
        public Object getRoot() {
            return null;
        }

        @Override
        public Object getChild(Object parent, int index) {
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            return 0;
        }

        @Override
        public boolean isLeaf(Object node) {
            return false;
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return 0;
        }
    }
}
