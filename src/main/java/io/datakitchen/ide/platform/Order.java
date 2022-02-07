package io.datakitchen.ide.platform;

import java.util.List;
import java.util.Map;

public class Order {

    private Map<String, Object> content;

    public Order(Map<String, Object> content){
        this.content = content;
    }

    public String getId(){
        return (String)content.get("hid");
    }

    public String getStatus() {
        return (String)content.get("order_status");
    }

    public List<Map<String, Object>> getOrderRuns() {
        return (List<Map<String, Object>>) content.get("servings");
    }

    public boolean update(Order order) {
        return false;
    }

    public Map<String, Object> getContent() {
        return content;
    }
}
