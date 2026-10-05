package com.example.unimarket.config;

import java.util.Locale;
import java.util.Set;

/** Rejects production startup when the JDBC URL targets a MySQL/TiDB system database. */
public final class ProductionDatabaseGuard {
    private static final Set<String> SYSTEM_DATABASES = Set.of(
            "information_schema", "mysql", "performance_schema", "sys");

    private ProductionDatabaseGuard() { }

    public static void validateEnvironment() {
        String profiles = System.getenv("SPRING_PROFILES_ACTIVE");
        if (profiles == null || profiles.lines().flatMap(line -> java.util.Arrays.stream(line.split(",")))
                .map(String::trim).noneMatch("prod"::equalsIgnoreCase)) {
            return;
        }

        String databaseUrl = System.getenv("DB_URL");
        String database = databaseName(databaseUrl);
        if (database == null || SYSTEM_DATABASES.contains(database.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException("Production DB_URL must include a dedicated application database and must not target a TiDB/MySQL system database. "
                    + "Create community_store, then use jdbc:mysql://<host>:<port>/community_store?... Current database: "
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
}
