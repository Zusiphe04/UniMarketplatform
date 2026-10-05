package com.example.unimarket.config;

import java.util.Locale;

/** Validates deployment-critical database environment before Spring starts. */
public final class ProductionDatabaseGuard {
    private static final String EXPECTED_DATABASE = "unimarket";

    private ProductionDatabaseGuard() { }

    public static void validateEnvironment() {
        String profiles = System.getenv("SPRING_PROFILES_ACTIVE");
        if (profiles == null || profiles.lines().flatMap(line -> java.util.Arrays.stream(line.split(",")))
                .map(String::trim).noneMatch("prod"::equalsIgnoreCase)) {
            return;
        }

        String databaseUrl = required("DB_URL");
        required("DB_USERNAME");
        required("DB_PASSWORD");
        required("UNIMARKET_JWT_SECRET");
        required("JWT_ISSUER");
        required("FRONTEND_ORIGINS");
        String ddlMode = required("JPA_DDL_AUTO");
        if (!"update".equalsIgnoreCase(ddlMode) && !"validate".equalsIgnoreCase(ddlMode)) {
            throw new IllegalStateException("Production JPA_DDL_AUTO must be 'update' for the first schema boot or 'validate' afterwards.");
        }

        if (!databaseUrl.equals(databaseUrl.trim()) || databaseUrl.indexOf('"') >= 0
                || databaseUrl.indexOf('\'') >= 0) {
            throw new IllegalStateException("Production DB_URL must not contain surrounding whitespace or quotes.");
        }
        String database = databaseName(databaseUrl);
        if (database == null || !EXPECTED_DATABASE.equals(database.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException("Production DB_URL must target the dedicated 'unimarket' database. "
                    + "Use jdbc:mysql://<TiDB-host>:4000/unimarket?... Current database: "
                    + (database == null ? "<missing>" : database));
        }
    }

    static String databaseName(String databaseUrl) {
        if (databaseUrl == null || !databaseUrl.startsWith("jdbc:mysql://")) return null;
        int authorityStart = "jdbc:mysql://".length();
        int pathStart = databaseUrl.indexOf('/', authorityStart);
        if (pathStart < 0 || pathStart + 1 >= databaseUrl.length()) return null;
        int queryStart = databaseUrl.indexOf('?', pathStart + 1);
        String database = databaseUrl.substring(pathStart + 1,
                queryStart < 0 ? databaseUrl.length() : queryStart).trim();
        return database.isEmpty() ? null : database;
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Production environment variable " + name + " is required.");
        }
        return value;
    }
}
