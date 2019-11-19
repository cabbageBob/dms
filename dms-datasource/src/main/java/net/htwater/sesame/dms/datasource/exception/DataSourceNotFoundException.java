package net.htwater.sesame.dms.datasource.exception;

import net.htwater.sesame.dms.core.exception.AbstractNotFoundException;

/**
 * @author Jokki
 */
public class DataSourceNotFoundException extends AbstractNotFoundException {
    private static final long serialVersionUID = 7091240262256939753L;
    private final String dataSourceId;

    public DataSourceNotFoundException(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    @Override
    public String getMessage() {
        return "数据源[" + dataSourceId + "] 不存在.";
    }

    public String getDataSourceId() {
        return dataSourceId;
    }
}
