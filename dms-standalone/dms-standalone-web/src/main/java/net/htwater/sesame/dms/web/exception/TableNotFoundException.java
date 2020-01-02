package net.htwater.sesame.dms.web.exception;

import core.exception.AbstractNotFoundException;

/**
 * @author Jokki
 */
public class TableNotFoundException extends AbstractNotFoundException {

    private final String datasourceId;

    private final String table;

    public TableNotFoundException(String datasourceId, String table) {
        this.datasourceId = datasourceId;
        this.table = table;
    }

    @Override
    public String getMessage() {
        return "数据源[" + datasourceId + "-" + table + "] 不存在.";
    }
}
