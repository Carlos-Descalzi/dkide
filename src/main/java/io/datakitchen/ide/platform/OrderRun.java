package io.datakitchen.ide.platform;

import java.util.Date;
import java.util.Map;

public class OrderRun {
    public enum OrderStatus {

        ACTIVE("Active" , "ACTIVE_ORDER"),
        COMPLETED("Completed", "COMPLETED_ORDER"),
        STOPPED("Stopped", "STOPPED_ORDER"),
        PAUSED("Paused", "PAUSED_ORDER"),
        ERROR("Error", "ORDER_ERROR");

        OrderStatus(String displayName, String statusString){
            this.displayName = displayName;
            this.statusString = statusString;
        }

        private final String displayName;
        private final String statusString;

        public String getDisplayName() {
            return displayName;
        }

        public String getStatusString() {
            return statusString;
        }

        public static OrderStatus fromStatusString(String statusString){
            for (OrderStatus s:values()){
                if (s.statusString.equals(statusString)){
                    return s;
                }
            }
            return null;
        }
    }

    public enum OrderRunStatus {
        PLANNED("Planned", "PLANNED_SERVING"),
        ACTIVE("Active", "ACTIVE_SERVING"),
        COMPLETED("Completed", "COMPLETED_SERVING"),
        STOPPED("Stopped", "STOPPED_SERVING"),
        ERROR("Error","SERVING_ERROR"),
        RERAN("Resumed", "SERVING_RERAN"),
        TIMED_OUT("Timed out", "SERVING_TIMED_OUT"),
        ACTIVE_TIMED_OUT("Active timed out", "ACTIVE_TIMED_OUT_SERVING"),
        NOT_RUN("Not run", "NOT_RUN");

        OrderRunStatus(String displayName, String statusString){
            this.displayName = displayName;
            this.statusString = statusString;
        }

        private final String displayName;
        private final String statusString;

        public String getDisplayName() {
            return displayName;
        }

        public String getStatusString() {
            return statusString;
        }

        public static OrderRunStatus fromStatusString(String status) {
            for (OrderRunStatus s:values()){
                if (s.statusString.equals(status)){
                    return s;
                }
            }
            return null;
        }
    }

    private Map<String, Object> order;
    private Map<String, Object> orderRun;
    public OrderRun(Map<String, Object> order, Map<String, Object> orderRun){
        this.order = order;
        this.orderRun = orderRun;
    }

    public String getOrderId(){
        return (String)order.get("hid");
    }

    public OrderStatus getOrderStatus() {
        return OrderStatus.fromStatusString((String)order.get("order_status"));
    }

    public String getHid(){
        return (String)orderRun.get("hid");
    }

    public OrderRunStatus getStatus(){
        return OrderRunStatus.fromStatusString((String)orderRun.get("status"));
    }

    public String getSchedule() { return (String)order.get("schedule");}

    public String getVariation() {
        return order.get("recipe")+"/"+order.get("variation");
    }

    public String getUser() {
        return (String)((Map<String, Object>)order.get("input_settings")).get("email");
    }
    public Object getEndTime(){
        Map<String, Object> timings = (Map<String, Object>)orderRun.get("timings");
        Number timing = (Number)timings.get("end_time");
        if (timing != null) {
            Date date = new Date(timing.longValue());
            return date;
        }
        return "";
    }

    public Object getDuration(){
        Map<String, Object> timings = (Map<String, Object>)orderRun.get("timings");
        return timings.get("duration");
    }

}
