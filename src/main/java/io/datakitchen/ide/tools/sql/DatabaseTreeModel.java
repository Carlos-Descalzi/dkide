package io.datakitchen.ide.tools.sql;

import com.intellij.openapi.application.ApplicationManager;
import io.datakitchen.ide.ui.EventSupport;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseTreeModel implements TreeModel {

    private final EventSupport<TreeModelListener> listeners = EventSupport.of(TreeModelListener.class);

    private Connection connection;

    public void closeConnection() {
        try {
            connection.close();
        }catch ( Exception ex){

        }
    }

    public Connection getConnection() {
        return connection;
    }

    public abstract class Node<PT extends Node, CT extends Node> {
        protected PT parent;
        protected String name;
        private boolean loaded;
        private boolean error;
        protected List<CT> children = new ArrayList<>();

        public String toString(){
            return StringUtils.isNotBlank(name) ? name : "(default)";
        }

        public Node(PT parent, String name){
            this.parent = parent;
            this.name = name;
        }

        public PT getParent(){
            return parent;
        }

        public List<CT> getChildren(){
            return children;
        }

        public abstract void doLoad(Connection connection) throws SQLException;
        public void load(){
            if (!loaded && !error && !isLeaf()){
                ApplicationManager.getApplication().invokeLater(()->{
                    try {
                        doLoad(DatabaseTreeModel.this.connection);
                        DatabaseTreeModel.this.listeners.getProxy().treeStructureChanged(new TreeModelEvent(this,getPath().toArray()));
                        loaded = true;
                    }catch(SQLException ex){
                        ex.printStackTrace();
                        error = true;
                    }
                });
            }
        }
        public boolean isLoaded(){
            return loaded || isLeaf();
        }
        public boolean isLeaf(){
            return false;
        }

        private List<Node> getPath(){
            List<Node> path = new ArrayList<>();
            Node node = this;
            while(node != null){
                path.add(0,node);
                node = node.parent;
            }
            return path;
        }

        public String getName(){
            return name;
        }
    }

    public class RootNode extends Node<Node, CatalogNode> {

        public RootNode(){
            super(null, "Database");
        }

        @Override
        public void doLoad(Connection connection) throws SQLException {
            ResultSet result = connection.getMetaData().getCatalogs();
            Set<String> catalogs = new HashSet<>();
            while(result.next()){
                String catalogName = result.getString("TABLE_CAT");
                if (catalogs.add(catalogName)) {
                    this.children.add(new CatalogNode(this, catalogName));
                }
            }
        }
    }

    public class CatalogNode extends Node<RootNode, SchemaNode> {
        public CatalogNode(RootNode parent, String name){
            super(parent, name);
        }

        @Override
        public void doLoad(Connection connection) throws SQLException {
            try {
                ResultSet result = connection.getMetaData().getSchemas(name, null);
                Set<String> schemaNames = new HashSet<>();
                while (result.next()) {
                    String schemaName = result.getString("TABLE_SCHEM");
                    if (schemaNames.add(schemaName)) {
                        this.children.add(new SchemaNode(this, schemaName));
                    }
                }
            }catch(AbstractMethodError ex){
                this.children.add(new SchemaNode(this, null));
            }
        }
    }

    public class SchemaNode extends Node<CatalogNode, TableNode> {

        public SchemaNode(CatalogNode parent, String name){
            super(parent, name);
        }

        @Override
        public void doLoad(Connection connection) throws SQLException {
            ResultSet result = connection.getMetaData().getTables(parent.name, name,null, null);
            while (result.next()) {
                String tableName = result.getString("TABLE_NAME");
                String tableType = result.getString("TABLE_TYPE");
                if (tableType.equals("VIEW")){
                    this.children.add(new ViewNode(this, tableName, tableType));
                } else {
                    this.children.add(new TableNode(this, tableName, tableType));
                }
            }
            this.children = this.children.stream().sorted().collect(Collectors.toList());
        }
    }

    public class TableNode extends Node<SchemaNode, ColumnNode> implements Comparable<TableNode>{

        private String tableType;
        private List<Key> importedKeys = new ArrayList<>();

        public TableNode(SchemaNode parent, String name, String tableType){
            super(parent, name);
            this.tableType = tableType;
        }

        public void doLoad(Connection connection) throws SQLException {
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet result = metadata.getColumns(parent.parent.name,parent.name,name,null);

            while(result.next()){
                String columnName = result.getString("COLUMN_NAME");
                String typeName = result.getString("TYPE_NAME");
                int columnSize = result.getInt("COLUMN_SIZE");
                this.children.add(new ColumnNode(this,columnName, typeName, columnSize));
            }

            result = metadata.getImportedKeys(parent.parent.name,parent.name,name);

            while(result.next()){
                importedKeys.add(
                    new Key(
                            result.getString("PKTABLE_CAT"),
                            result.getString("PKTABLE_SCHEM"),
                            result.getString("PKTABLE_NAME"),
                            result.getString("PKCOLUMN_NAME"),
                            result.getString("FKCOLUMN_NAME")));
            }
        }

        @Override
        public int compareTo(@NotNull DatabaseTreeModel.TableNode o) {
            return fullyQualifiedName().compareTo(o.fullyQualifiedName());
        }

        public String fullyQualifiedName(){
            return String.join(".",parent.parent.name,parent.name,name);
        }

        public List<Key> getImportedKeys(){
            return importedKeys;
        }
    }

    public class Key {
        private String catalog;
        private String schema;
        private String table;
        private String pkColumn;
        private String fkColumn;

        public Key(String catalog, String schema, String table, String pkColumn, String fkColumn) {
            this.catalog = catalog;
            this.schema = schema;
            this.table = table;
            this.pkColumn = pkColumn;
            this.fkColumn = fkColumn;
        }

        public String getCatalog() {
            return catalog;
        }

        public String getSchema() {
            return schema;
        }

        public String getTable() {
            return table;
        }

        public String getPkColumn() {
            return pkColumn;
        }

        public String getFkColumn() {
            return fkColumn;
        }

        public String fkTableName(){
            return String.join(".",catalog,schema,table);
        }
    }

    public class ViewNode extends TableNode {

        public ViewNode(SchemaNode parent, String name, String tableType) {
            super(parent, name, tableType);
        }
    }

    public class ColumnNode extends Node<TableNode, Node> {
        private String typeName;
        private int columnSize;
        public ColumnNode(TableNode parent, String columnName, String typeName, int columnSize){
            super(parent, columnName);
            this.typeName = typeName;
            this.columnSize = columnSize;
        }

        public String toString(){
            return name + " "+typeName.toLowerCase()+ (columnSize != 0 ? "("+columnSize+")" : "");
        }

        public void doLoad(Connection connection) throws SQLException {}

        public boolean isLeaf(){
            return true;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    private RootNode root;

    public DatabaseTreeModel(Connection connection){
        this.connection = connection;
        this.root = new RootNode();
    }


    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((Node)parent).children.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((Node)parent).children.size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((Node)node).isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((Node)parent).children.indexOf(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        this.listeners.addListener(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        this.listeners.removeListener(l);
    }
}
