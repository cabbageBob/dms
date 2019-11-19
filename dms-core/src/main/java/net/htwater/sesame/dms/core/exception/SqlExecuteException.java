package net.htwater.sesame.dms.core.exception;

/**
 * sql执行报错
 * @author Jokki
 */
public class SqlExecuteException extends AbstractManagementException {
    private static final long serialVersionUID = 4578173050769688422L;
    private final String sql;

    public SqlExecuteException(String message, Throwable cause, String sql) {
        super(message, cause);
        this.sql = sql;
    }

    public SqlExecuteException(Throwable cause, String sql) {
        super(cause);
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
