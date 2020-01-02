package core;

import core.dialect.Dialect;
import core.sql.SqlExecuteRequest;
import core.sql.SqlExecuteResult;
import core.sql.SqlExecutor;
import org.springframework.scheduling.annotation.Async;

/**
 * 数据库管理服务
 * @author Jokki
 */
public interface DatabaseManagerService extends SqlExecutor {

    SqlExecuteResult executeSqlBatch(SqlExecuteRequest request);

    Dialect getTableDialect();

    @Async
    void monitorDatasource(String datasourceId, String dbName);
}
