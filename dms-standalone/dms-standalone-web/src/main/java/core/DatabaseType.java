package core;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 数据库类型枚举
 * @author Jokki
 */
public enum DatabaseType {
    /**未知类型*/
    UNKNOWN(null, null, String::isEmpty),
    /**mysql*/
    MYSQL("com.mysql.cj.jdbc.Driver", "select 1", createUrlPredicate("mysql")),
    /**oracle*/
    ORACLE("oracle.jdbc.driver.OracleDriver", "select 1 from dual", createUrlPredicate("oracle")),
    /**sql server*/
    SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", "select 1 t", createUrlPredicate("sqlserver"));

    private final String testQuery;

    private final String driverClassName;

    private final Predicate<String> urlPredicate;

    static Predicate<String> createUrlPredicate(String name) {
        return url -> {
            String urlWithoutPrefix = url.substring("jdbc".length()).toLowerCase();
            String prefix = ":" + name.toLowerCase() + ":";
            return urlWithoutPrefix.startsWith(prefix);
        };
    }


    DatabaseType(String driverClassName, String testQuery, Predicate<String> urlPredicate) {
        this.driverClassName = driverClassName;
        this.testQuery = testQuery;
        this.urlPredicate = urlPredicate;
    }

    public static DatabaseType fromJdbcUrl(String url) {
        if (Objects.nonNull(url)) {
            return Arrays.stream(values()).filter(type -> type.urlPredicate.test(url)).findFirst().orElse(UNKNOWN);
        }
        return UNKNOWN;
    }

}
