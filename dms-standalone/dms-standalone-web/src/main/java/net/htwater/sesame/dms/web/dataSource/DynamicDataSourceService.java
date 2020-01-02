package net.htwater.sesame.dms.web.dataSource;

import net.htwater.sesame.dms.web.exception.DataSourceNotFoundException;
import net.htwater.sesame.dms.web.service.DataSourceCache;

/**
 * 动态数据源服务
 * @author Jokki
 */
public interface DynamicDataSourceService {

    /**
     * 根据数据源ID获取动态数据源,数据源不存在将抛出{@link DataSourceNotFoundException}
     * @param dataSourceId 数据源ID
     * @return 动态数据源
     */
    DynamicDataSource getDataSource(String dataSourceId);

    DataSourceCache removeCache(String id);
}
