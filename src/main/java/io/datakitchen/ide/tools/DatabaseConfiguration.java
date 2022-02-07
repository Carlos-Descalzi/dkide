package io.datakitchen.ide.tools;

import io.datakitchen.ide.ui.NamedObject;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseConfiguration implements NamedObject {
    private static final long serialVersionUID = 1;

    private String name;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private List<String> driverJarPaths = new ArrayList<>();
    private Driver driver;

    public DatabaseConfiguration(String name) {
        this.name = name;
    }

    public String toString(){
        return StringUtils.isBlank(name) ? "(no name)" : name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public List<String> getDriverJarPaths() {
        return driverJarPaths;
    }

    public void setDriverJarPaths(List<String> driverJarPaths) {
        this.driverJarPaths = driverJarPaths;
    }

    private Driver getDriver() throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (driver == null) {
            List<URL> urls = new ArrayList<>();
            for (int i = 0; i < driverJarPaths.size(); i++) {
                try {
                    urls.add(Path.of(driverJarPaths.get(i)).toUri().toURL());
                } catch (Exception ex) {
                }
            }

            URLClassLoader classLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));

            Class<Driver> driverClass = (Class<Driver>) classLoader.loadClass(driverClassName);
            driver = driverClass.getConstructor().newInstance();
        }
        return driver;
    }

    public Connection connect() throws SQLException {
        try {
            driver = getDriver();
            Properties properties = new Properties();
            properties.put("user", username);
            properties.put("password", password);
            return driver.connect(url, properties);
        }catch (SQLException ex){
            throw ex;
        }catch (Exception ex){
            ex.printStackTrace();
            throw new SQLException(ex.getMessage());
        }
    }

}
