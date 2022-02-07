package io.datakitchen.ide.service;

import com.intellij.openapi.diagnostic.Logger;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class ConnectionInfo {

    private static final Logger LOGGER = Logger.getInstance(ConnectionInfo.class);
    private final String driverClass;
    private final String urlTemplate;

    public ConnectionInfo(String driverClass, String urlTemplate) {
        this.driverClass = driverClass;
        this.urlTemplate = urlTemplate;
    }

    public String buildUrl(Map<String, Object> properties) {
        String url = urlTemplate;
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (entry.getValue() != null) {
                String key = entry.getKey();
                String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8);
                url = url.replace("{{" + key + "}}", value);
            }
        }
        return url;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    protected Connection createConnection(String url, String username, String password) throws SQLException {
        try {
            Class driverClass = Class.forName(this.driverClass);
            Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
            Properties properties = new Properties();
            properties.put("user", username);
            properties.put("password", password);

            return driver.connect(url, properties);
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new SQLException(ex.getMessage());
        }

    }

    public Connection createConnection(Map<String, Object> config) throws SQLException {
        return createConnection(buildUrl(config), getUsername(config), getPassword(config));
    }

    private String getPassword(Map<String, Object> config) {
        return (String) config.get("password");
    }

    private String getUsername(Map<String, Object> config) {
        return (String) config.get("username");
    }
}
