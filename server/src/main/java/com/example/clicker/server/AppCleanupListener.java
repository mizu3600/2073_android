package com.example.clicker.server;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

@WebListener
public class AppCleanupListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ClassLoader webAppClassLoader = Thread.currentThread().getContextClassLoader();

        Collections.list(DriverManager.getDrivers()).forEach(driver -> deregisterIfOwnedByWebApp(driver, webAppClassLoader));

        AbandonedConnectionCleanupThread.checkedShutdown();
    }

    private void deregisterIfOwnedByWebApp(Driver driver, ClassLoader webAppClassLoader) {
        if (driver.getClass().getClassLoader() != webAppClassLoader) {
            return;
        }

        try {
            DriverManager.deregisterDriver(driver);
        } catch (SQLException ignored) {
        }
    }
}
