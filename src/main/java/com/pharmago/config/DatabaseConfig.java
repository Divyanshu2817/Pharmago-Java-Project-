package com.pharmago.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final String PRIMARY_PROPERTIES_FILE = "application.properties";
    private static final String FALLBACK_PROPERTIES_FILE = "application.example.properties";
    private final Properties properties = new Properties();

    public DatabaseConfig() {
        loadProperties();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        );
    }

    private void loadProperties() {
        try (InputStream inputStream = openPropertiesStream()) {
            if (inputStream == null) {
                throw new IllegalStateException(
                        "Missing database config. Create application.properties or update application.example.properties."
                );
            }
            properties.load(inputStream);
            validateRequiredProperties();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load database configuration.", exception);
        }
    }

    private InputStream openPropertiesStream() {
        InputStream primary = getClass().getClassLoader().getResourceAsStream(PRIMARY_PROPERTIES_FILE);
        if (primary != null) {
            return primary;
        }
        return getClass().getClassLoader().getResourceAsStream(FALLBACK_PROPERTIES_FILE);
    }

    private void validateRequiredProperties() {
        validate("db.url");
        validate("db.username");
    }

    private void validate(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Database configuration value is missing: " + key);
        }
    }
}
