package io.datakitchen.ide.tools.sql;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StatementUtil {

    public enum JoinType {
        FOREIGN_KEYS,
        SAME_NAME,
        NONE
    }

    public static String makeSelect(TreePath[] paths, JoinType joinType){
        List<DatabaseTreeModel.Node> nodes = new ArrayList<>();
        for (TreePath path:paths){
            nodes.add((DatabaseTreeModel.Node) path.getLastPathComponent());
        }

        List<String> fieldNames = collectFields(nodes);
        List<DatabaseTreeModel.TableNode> tableNodes = collectTables(nodes);
        List<String> tableNames = tableNodes.stream().map(StatementUtil::getTableName).collect(Collectors.toList());
        List<String> joins;

        switch (joinType){
            case FOREIGN_KEYS:
                joins = makeJoinsByForeignKeys(tableNodes);
                break;
            case SAME_NAME:
                joins = makeJoinsMatchingName(tableNodes);
                break;
            default:
                joins = new ArrayList<>();
                break;
        }

        StringBuilder builder = new StringBuilder("SELECT\n")
            .append(String.join(",\n", fieldNames.stream().map((String s)-> "\t"+s).collect(Collectors.toList())))
            .append("\nFROM\n")
            .append(String.join(",\n", tableNames.stream().map((String s)-> "\t"+s).collect(Collectors.toList())));

        if (!joins.isEmpty()){
            builder
                .append("\nWHERE\n")
                .append(String.join(",\n",joins.stream().map((String s)-> "\t"+s).collect(Collectors.toList())));
        }

        builder.append(";\n");
        return builder.toString();
    }

    private static List<String> makeJoinsMatchingName(List<DatabaseTreeModel.TableNode> tableNodes) {
        Set<String> joins = new LinkedHashSet<>();

        for (DatabaseTreeModel.TableNode node1:tableNodes){
            for (DatabaseTreeModel.TableNode node2:tableNodes){
                if (node1 != node2){
                    for (DatabaseTreeModel.ColumnNode c1: node1.getChildren()){
                        for (DatabaseTreeModel.ColumnNode c2: node2.getChildren()){
                            if (c1.getName().equals(c2.getName())){
                                joins.add(getTableName(node1)+"."+c1.getName()+" = "+getTableName(node2)+"."+c2.getName());
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(joins);
    }

    private static List<String> makeJoinsByForeignKeys(List<DatabaseTreeModel.TableNode> tableNodes) {
        Set<String> joins = new LinkedHashSet<>();

        Set<String> tableNames = tableNodes.stream().map(StatementUtil::getTableName).collect(Collectors.toSet());

        for (DatabaseTreeModel.TableNode node:tableNodes){
            for (DatabaseTreeModel.Key key:node.getImportedKeys()){
                String fkTableName = key.fkTableName();
                if (tableNames.contains(fkTableName)){
                    joins.add(getTableName(node)+"."+key.getFkColumn()+" = "+fkTableName+"."+key.getPkColumn());
                }
            }
        }

        return new ArrayList<>(joins);
    }

    private static List<String> collectFields(List<DatabaseTreeModel.Node> nodes){
        List<String> fields = new ArrayList<>();

        for (DatabaseTreeModel.Node node:nodes){
            if (node instanceof DatabaseTreeModel.TableNode){
                fields.add(node.getName()+".*");
            } else if (node instanceof DatabaseTreeModel.ColumnNode){
                fields.add(node.getParent().getName()+"."+node.getName());
            }
        }

        return fields;
    }

    private static String getTableName(DatabaseTreeModel.TableNode tableNode){
        List<String> name = new ArrayList<>();
        DatabaseTreeModel.Node node = tableNode;

        while(!(node instanceof DatabaseTreeModel.RootNode)){
            name.add(0,node.getName());
            node = node.getParent();
        }

        return String.join(".",name);
    }

    private static List<DatabaseTreeModel.TableNode> collectTables(List<DatabaseTreeModel.Node> nodes) {
        Set<DatabaseTreeModel.TableNode> tableNodes = new LinkedHashSet<>();

        for (DatabaseTreeModel.Node node :nodes){
            if (node instanceof DatabaseTreeModel.TableNode){
                tableNodes.add((DatabaseTreeModel.TableNode) node);
            } else if (node instanceof DatabaseTreeModel.ColumnNode){
                tableNodes.add(((DatabaseTreeModel.ColumnNode)node).getParent());
            }
        }

        return new ArrayList<>(tableNodes);
    }

    public static String makeCreateTable(TreePath path) {
        DatabaseTreeModel.TableNode node = (DatabaseTreeModel.TableNode)path.getLastPathComponent();

        StringBuilder builder = new StringBuilder("CREATE TABLE ")
                .append(getTableName(node))
                .append("(");

        List<String> columnDefinitions = node.getChildren().stream().map(StatementUtil::getColumnDefinition).collect(Collectors.toList());

        builder.append(String.join(",\n", columnDefinitions.stream().map((String s)-> "\t"+s).collect(Collectors.toList())));

        builder.append(");");
        return builder.toString();
    }

    private static String getColumnDefinition(DatabaseTreeModel.ColumnNode node) {
        return node.getName()+" "+node.getTypeName();
    }

    public static String makeDropTableStatement(TreePath path) {
        DatabaseTreeModel.TableNode node = (DatabaseTreeModel.TableNode)path.getLastPathComponent();

        return "DROP TABLE "+getTableName(node)+";\n";
    }

    public static String makeInsertStatementTemplate(TreePath path) {
        DatabaseTreeModel.TableNode node = (DatabaseTreeModel.TableNode)path.getLastPathComponent();

        List<String> columnNames = node.getChildren().stream()
                .map((DatabaseTreeModel.Node::getName))
                .collect(Collectors.toList());

        List<String> placeHolders = columnNames.stream().map((String s)->" ")
                .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ")
                .append(getTableName(node))
                .append("(")
                .append(String.join(", ", columnNames))
                .append(") VALUES (")
                .append(String.join(", ", placeHolders))
                .append(");");

        return builder.toString();
    }
}
