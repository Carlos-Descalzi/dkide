package io.datakitchen.ide.tools.orders;

import io.datakitchen.ide.platform.Order;
import io.datakitchen.ide.platform.OrderRun;
import io.datakitchen.ide.platform.ServiceClient;
import net.minidev.json.parser.ParseException;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.*;

public class OrderListTableModel extends AbstractTableModel {
    private ServiceClient serviceClient;
    private String kitchenName = "master";
    private boolean running = false;
    private Thread thread;
    private List<OrderRun> orders = new ArrayList<>();
    private int orderCount = 30;

    public OrderListTableModel(ServiceClient client){
        this.serviceClient = client;
        this.thread = new Thread(this::loop);
        this.thread.setDaemon(true);
        this.running = true;
        this.thread.start();
    }

    public String getKitchenName() {
        return kitchenName;
    }

    public void setKitchenName(String kitchenName) {
        this.kitchenName = kitchenName;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public void finish(){
        if (this.thread != null){
            this.running = false;
        }
    }

    private void loop(){
        while (running){
            try {
                System.out.println("Getting orders ...");
                setOrders(this.serviceClient.getOrders(kitchenName, orderCount));
                Thread.sleep(1000);
            }catch (IOException | ParseException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex){
                break;
            }
        }
    }

    private void setOrders(List<Order> orders) {
        List<OrderRun> newOrders = new ArrayList<>();
        for (Order order: orders){
            for (Map<String, Object> orderRun: order.getOrderRuns()){
                newOrders.add(new OrderRun(order.getContent(), orderRun));
            }
        }
        this.orders = newOrders;
        fireTableChanged(new TableModelEvent(this));

    }

    @Override
    public int getRowCount() {
        return orders.size();
    }

    @Override
    public int getColumnCount() {
        return 8;
    }

    private static final String[] COLUMN_NAMES = {
            "Order ID",
            "Recipe/Variation",
            "User",
            "Order Status",
            "Schedule",
            "Order Run ID",
            "End time",
            "Status"
    };

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            OrderRun orderRun = orders.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return orderRun.getOrderId();
                case 1:
                    return orderRun.getVariation();
                case 2:
                    return orderRun.getUser();
                case 3:
                    return orderRun.getOrderStatus();
                case 4:
                    return orderRun.getSchedule();
                case 5:
                    return orderRun.getHid();
                case 6:
                    return String.valueOf(orderRun.getEndTime());
                case 7:
                    return orderRun.getStatus();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public OrderRun getOrderRun(int row) {
        return orders.get(row);
    }
}
