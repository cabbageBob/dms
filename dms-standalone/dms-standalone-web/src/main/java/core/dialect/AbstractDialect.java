package core.dialect;


import core.DatabaseType;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 特定关系型数据库的SQL方言
 * @author Jokki
 */
public abstract class AbstractDialect implements Dialect {
    private SqlExecutor sqlExecutor;

    public static final String COLUMNS_NUM = "100";
    public AbstractDialect(SqlExecutor sqlExecutor, DatabaseType... databaseTypes){
        this.sqlExecutor = sqlExecutor;
        supportDataBases.addAll(Arrays.asList(databaseTypes));
    }
    private Set<DatabaseType> supportDataBases = new HashSet<>();
    @Override
    public boolean isSupport(DatabaseType type) {
        return supportDataBases.contains(type);
    }
    @Override
    public Dialect get() {
        return this;
    }

    protected String quote(String value) {
        return openQuote() + value + closeQuote();
    }
}
