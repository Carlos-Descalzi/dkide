package io.datakitchen.ide.tools;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.GlobalConfiguration;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.platform.ServiceClient;
import io.datakitchen.ide.tools.orders.OrderListCellRenderer;
import io.datakitchen.ide.tools.orders.OrderListTableModel;
import io.datakitchen.ide.ui.RegExValidatedField;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMonitor extends JPanel implements Disposable {

    private final ComboBox<Site> site = new ComboBox<>();
    private final ComboBox<Account> accounts = new ComboBox<>();
    private final ComboBox<String> kitchen = new ComboBox<>();
    private final Action connectAction = new SimpleAction("Connect", this::connect);
    private final RegExValidatedField nOrders = new RegExValidatedField(RegExValidatedField.NUMBER);
    private ServiceClient serviceClient;
    private final JTable table = new JBTable();

    public OrderMonitor(Project project){

        GlobalConfiguration configuration = ConfigurationService.getInstance(project).getGlobalConfiguration();

        site.setModel(new DefaultComboBoxModel<>(configuration.getAllSites().toArray(Site[]::new)));
        accounts.setModel(new DefaultComboBoxModel<>(configuration.getAccounts().toArray(Account[]::new)));

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Site:"));
        topPanel.add(site);
        topPanel.add(new JLabel("Account:"));
        topPanel.add(accounts);
        topPanel.add(new JButton(connectAction));
        topPanel.add(new JLabel("Kitchen:"));
        topPanel.add(kitchen);
        topPanel.add(new JLabel("# Orders:"));
        topPanel.add(nOrders);
        nOrders.setText("30");
        nOrders.addActionListener(this::setNOrders);
        add(topPanel, BorderLayout.NORTH);
        kitchen.addActionListener(this::selectKitchen);
        add(new JBScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        table.setShowGrid(false);

        table.setAutoCreateColumnsFromModel(false);

        OrderListCellRenderer renderer = new OrderListCellRenderer();

        TableColumn c0 = new TableColumn(0);
        c0.setHeaderValue("Order ID");
        c0.setCellRenderer(renderer);
        table.getColumnModel().addColumn(c0);

        TableColumn c1 = new TableColumn(1);
        c1.setHeaderValue("Recipe/Variation");
        c1.setCellRenderer(renderer);
        table.getColumnModel().addColumn(c1);

        TableColumn c2 = new TableColumn(2);
        c2.setHeaderValue("User");
        c2.setCellRenderer(renderer);
        table.getColumnModel().addColumn(c2);

        TableColumn c3 = new TableColumn(3);
        c3.setHeaderValue("Order status");
        c3.setMinWidth(120);
        c3.setMaxWidth(120);
        c3.setCellRenderer(renderer);
        table.getColumnModel().addColumn(c3);

        TableColumn c4 = new TableColumn(4);
        c4.setHeaderValue("Schedule");
        c4.setMinWidth(100);
        c4.setMaxWidth(100);
        c4.setCellRenderer(renderer);
        table.getColumnModel().addColumn(c4);

        TableColumn c5 = new TableColumn(5);
        c5.setHeaderValue("Order run ID");
        c5.setCellRenderer(renderer);
        table.getColumnModel().addColumn(c5);

        TableColumn c6 = new TableColumn(6);
        c6.setHeaderValue("End time");
        c6.setCellRenderer(renderer);
        table.getColumnModel().addColumn(c6);

        TableColumn c7 = new TableColumn(7);
        c7.setHeaderValue("Status");
        c7.setMinWidth(90);
        c7.setMaxWidth(90);
        c7.setCellRenderer(renderer);
        table.getColumnModel().addColumn(c7);

    }

    private void setNOrders(ActionEvent event) {
        TableModel model  = table.getModel();
        if (model instanceof OrderListTableModel){
            ((OrderListTableModel)model).setOrderCount(Integer.parseInt(nOrders.getText()));
        }
    }

    private void selectKitchen(ActionEvent event) {
        TableModel model  = table.getModel();
        if (model instanceof OrderListTableModel){
            ((OrderListTableModel)model).setKitchenName(kitchen.getItem());
        }
    }

    @Override
    public void dispose() {
        TableModel model  = table.getModel();
        if (model instanceof OrderListTableModel){
            ((OrderListTableModel)model).finish();
        }
    }


    private void connect(ActionEvent event) {
        connectAction.setEnabled(false);
        serviceClient = new ServiceClient(site.getItem().getUrl());

        new Thread(()->{
            try {
                Account account = this.accounts.getItem();
                serviceClient.login(account.getUsername(), account.getPassword());
                fillKitchens();
            }catch(Exception ex){
                ex.printStackTrace();
            }finally {
                SwingUtilities.invokeLater(this::enableConnect);
            }
        }).start();
    }

    private void fillKitchens() {
        try {
            List<String> kitchenNames = serviceClient.getKitchens()
                    .stream().map(k -> (String) k.get("name"))
                    .collect(Collectors.toList());
            kitchen.setModel(new DefaultComboBoxModel<>(kitchenNames.toArray(String[]::new)));
            table.setModel(new OrderListTableModel(serviceClient));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void enableConnect() {
        connectAction.setEnabled(true);
    }

}
