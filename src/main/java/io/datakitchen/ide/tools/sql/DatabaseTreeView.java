package io.datakitchen.ide.tools.sql;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.treeStructure.Tree;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.service.SQLRunnerService;
import io.datakitchen.ide.tools.DatabaseConfiguration;
import io.datakitchen.ide.ui.SimpleAction;
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTreeView extends JPanel implements Disposable, ClipboardOwner {

    private final Project project;
    private Action connectAction = new SimpleAction("Connect", this::connect);
    private JTree tree = new Tree();
    private JComboBox connection = new ComboBox();
    private JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private TableDetailsView tableDetails = new TableDetailsView();

    public DatabaseTreeView(Project project){
        this.project = project;
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Connection"));
        connection.setModel(
            new DefaultComboBoxModel<>(
                ConfigurationService.getInstance(project).getConnections().stream().map(DatabaseConfiguration::getName)
                        .toArray()));
        topPanel.add(connection);
        topPanel.add(new JButton(connectAction));
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("No connection")));
        add(topPanel, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        split.setLeftComponent(new JScrollPane(tree,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        split.setRightComponent(tableDetails);
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                ((DatabaseTreeModel.Node)event.getPath().getLastPathComponent()).load();
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {}
        });
        tree.setCellRenderer(new DefaultTreeCellRenderer(){
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel component = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                if (value instanceof DatabaseTreeModel.CatalogNode){
                    component.setIcon(IconLoader.getIcon("/icons/db/catalog.png",getClass()));
                } else if (value instanceof DatabaseTreeModel.ViewNode){
                    component.setIcon(IconLoader.getIcon("/icons/db/view.png",getClass()));
                } else if (value instanceof DatabaseTreeModel.TableNode){
                    component.setIcon(IconLoader.getIcon("/icons/db/table.png",getClass()));
                } else if (value instanceof DatabaseTreeModel.RootNode){
                    component.setIcon(IconLoader.getIcon("/icons/db/database.png",getClass()));
                } else if (value instanceof DatabaseTreeModel.ColumnNode){
                    component.setIcon(IconLoader.getIcon("/icons/db/column.png",getClass()));
                } else if (value instanceof DatabaseTreeModel.SchemaNode){
                    component.setIcon(IconLoader.getIcon("/icons/db/schema.png",getClass()));
                } else {
                    component.setIcon(null);
                }

                return component;
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger() && tree.getModel() instanceof DatabaseTreeModel){
                showPopup(e);
            }
            }
        });

    }

    private void connect(ActionEvent event) {
        try {
            Connection connection = SQLRunnerService.getInstance(project).getConnection((String)this.connection.getSelectedItem());
            tree.setModel(new DatabaseTreeModel(connection));
            tree.collapsePath(new TreePath(tree.getModel().getRoot()));
        }catch(Exception ex){
            Messages.showErrorDialog(ex.getMessage(),"Error");
        }
    }

    @Override
    public void dispose() {
        if (tree.getModel() instanceof DatabaseTreeModel){
            ((DatabaseTreeModel)tree.getModel()).closeConnection();
        }
    }

    private Action copyAction = new SimpleAction("Copy", this::copy);
    private Action copyAsSelectJoinFkAction = new SimpleAction("Join by foreign keys", this::copyAsSelectStatement);
    private Action copyAsSelectJoinNameAction = new SimpleAction("Join by column names", this::copyAsSelectStatementJoiningByName);
    private Action copyAsDropTableAction = new SimpleAction("Copy as DROP TABLE statement", this::copyAsDropTableStatement);
    private Action copyAsInsertTemplateAction = new SimpleAction("Copy as INSERT template", this::copyAsInsertTemplate);
    private Action copyAsCreateTableAction = new SimpleAction("Copy as CREATE TABLE statement", this::copyAsCreateTableStatement);
    private Action viewDetailsAction = new SimpleAction("View details",this::viewDetails);

    private void viewDetails(ActionEvent event) {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths.length == 1) {
            Object lastItem = paths[0].getLastPathComponent();

            if (lastItem instanceof DatabaseTreeModel.TableNode) {
                DatabaseTreeModel.TableNode tableNode = (DatabaseTreeModel.TableNode) lastItem;

                String tableName = tableNode.getName();
                String schemaName = tableNode.getParent().getName();
                String catalogName = tableNode.getParent().getParent().getName();

                Connection connection = ((DatabaseTreeModel) tree.getModel()).getConnection();

                tableDetails.setTable(connection, catalogName, schemaName, tableName);
            } else {
                tableDetails.setTable(null, null, null, null);
            }
        } else {
            tableDetails.setTable(null, null, null, null);
        }
    }

    private void showPopup(MouseEvent e){
        JPopupMenu popup = new JPopupMenu();
        updateActions();
        popup.add(viewDetailsAction);
        JMenu menu = new JMenu("Copy as SELECT statement");
        menu.add(copyAsSelectJoinFkAction);
        menu.add(copyAsSelectJoinNameAction);
        popup.add(menu);
        popup.add(copyAsDropTableAction);
        popup.add(copyAsInsertTemplateAction);
        popup.add(copyAsCreateTableAction);

        popup.show(tree, e.getX(),e.getY());
    }

    private void updateActions() {
        TreePath[] paths = tree.getSelectionPaths();
        copyAsDropTableAction.setEnabled(paths.length == 1 && paths[0].getLastPathComponent() instanceof DatabaseTreeModel.TableNode);
        copyAsInsertTemplateAction.setEnabled(paths.length == 1 && paths[0].getLastPathComponent() instanceof DatabaseTreeModel.TableNode);
        copyAsCreateTableAction.setEnabled(paths.length == 1 && paths[0].getLastPathComponent() instanceof DatabaseTreeModel.TableNode);
        viewDetailsAction.setEnabled(paths.length == 1 && paths[0].getLastPathComponent() instanceof DatabaseTreeModel.TableNode);
    }

    private void copy(ActionEvent event) {
        List<String> items = new ArrayList<>();

        for (TreePath path: tree.getSelectionPaths()){
            items.add(((DatabaseTreeModel.Node)path.getLastPathComponent()).getName());
        }

        putInClipboard(String.join(", ",items));
    }

    private void copyAsDropTableStatement(ActionEvent event) {
        putInClipboard(StatementUtil.makeDropTableStatement(tree.getSelectionPath()));
    }

    private void copyAsInsertTemplate(ActionEvent event) {
        putInClipboard(StatementUtil.makeInsertStatementTemplate(tree.getSelectionPath()));
    }

    private void copyAsCreateTableStatement(ActionEvent event) {
        putInClipboard(StatementUtil.makeCreateTable(tree.getSelectionPath()));
    }

    private void copyAsSelectStatement(ActionEvent event) {
        putInClipboard(StatementUtil.makeSelect(tree.getSelectionPaths(), StatementUtil.JoinType.FOREIGN_KEYS));
    }

    private void copyAsSelectStatementJoiningByName(ActionEvent event) {
        putInClipboard(StatementUtil.makeSelect(tree.getSelectionPaths(), StatementUtil.JoinType.SAME_NAME));
    }

    private void putInClipboard(String content){
        getToolkit().getSystemClipboard().setContents(new BasicTransferable(content, null), this);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }
}
