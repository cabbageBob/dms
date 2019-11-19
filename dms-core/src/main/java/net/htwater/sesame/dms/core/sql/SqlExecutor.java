package net.htwater.sesame.dms.core.sql;

import java.util.List;

/**
 * @author Jokki
 */
public interface SqlExecutor {

    /**
     * 执行sql并返回执行结果
     * @param request sql请求
     * @return sql执行结果
     */
    List<SqlExecuteResult> execute(SqlExecuteRequest request);
}
