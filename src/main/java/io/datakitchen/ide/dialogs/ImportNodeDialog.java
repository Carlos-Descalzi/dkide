package io.datakitchen.ide.dialogs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import io.datakitchen.ide.HelpMessages;
import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.platform.ServiceClient;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.HelpContainer;
import io.datakitchen.ide.ui.SimpleAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ImportNodeDialog extends DialogWrapper {

    private final FormPanel panel;
    private final ComboBox<Site> site;
    private final ComboBox<Account> account;
    private final JTree kitchenTree;

    private ServiceClient client;
    private final Action connectAction = new SimpleAction("Connect ...",this::connect);
    private final Project project;
    private final VirtualFile folder;

    public ImportNodeDialog(Project project, VirtualFile folder) {
        super(true);
        this.project = project;
        this.folder = folder;

        List<Site> sites = ConfigurationService.getInstance(project).getGlobalConfiguration().getAllSites();

        site = new ComboBox<>(new DefaultComboBoxModel<>(sites.toArray(Site[]::new)));
        account = new ComboBox<>(new DefaultComboBoxModel<>(
                ConfigurationService
                        .getInstance(project)
                        .getGlobalConfiguration()
                        .getAccounts().toArray(Account[]::new)));
        kitchenTree = new Tree();
        JScrollPane treePane = new JBScrollPane(kitchenTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        kitchenTree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                onTreeExpanded(event);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {}
        });
        panel = new FormPanel();

        panel.addField("Site", new HelpContainer(site, HelpMessages.SITE_HELP_MSG));

        JPanel accountPanel = new JPanel(new BorderLayout());
        accountPanel.add(new HelpContainer(account, HelpMessages.ACCOUNT_HELP_MSG), BorderLayout.CENTER);
        accountPanel.add(new JButton(connectAction), BorderLayout.EAST);

        panel.addField("Account", accountPanel, new Dimension(300,28));
        panel.addField("Select node", treePane, new Dimension(400,300));

        updateActions();
        init();
    }

    private void onTreeExpanded(TreeExpansionEvent event) {
        Object object = event.getPath().getLastPathComponent();
        if (object instanceof KitchenTreeModel.LoadableNode){
            KitchenTreeModel.LoadableNode node = (KitchenTreeModel.LoadableNode)object;
            if (!node.isLoaded()){
                node.load();
            }
        }
    }

    private void connect(ActionEvent event) {
        connectAction.setEnabled(false);
        kitchenTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Fetching ...")));
        ApplicationManager.getApplication().invokeLater(this::connect);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    private void updateActions(){
    }

    private void connect(){
        Site site = this.site.getItem();
        client = new ServiceClient(site.getUrl());
        Account account = this.account.getItem();
        try {
            client.login(account.getUsername(), account.getPassword());
            kitchenTree.setModel(new KitchenTreeModel(client));
        }catch (Exception ex){
            client = null;
            ex.printStackTrace();
        } finally {
            SwingUtilities.invokeLater(()->{
                updateActions();
                connectAction.setEnabled(true);
            });

        }
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        TreePath path = this.kitchenTree.getSelectionPath();
        if (path == null || path.getPath().length != 4){
            return new ValidationInfo("A node must be selected",kitchenTree);
        }
        return null;
    }

    public void pullNode(){
        TreePath path = this.kitchenTree.getSelectionPath();
        if (path != null && path.getPath().length == 4){
            KitchenTreeModel.RecipeNode recipeNode = (KitchenTreeModel.RecipeNode) path.getLastPathComponent();
            ServiceClient.Node node = recipeNode.getNode();
            ApplicationManager.getApplication().invokeLater(()->{
                try {
                    this.client.pullNode(node, folder,project);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
        }
    }
}
