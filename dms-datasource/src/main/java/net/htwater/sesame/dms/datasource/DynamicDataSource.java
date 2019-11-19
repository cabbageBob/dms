package net.htwater.sesame.dms.datasource;


import net.htwater.sesame.dms.datasource.switcher.DataSourceSwitcher;

import javax.sql.DataSource;

/**
 * 动态数据源
 * @author Jokki
 */
public interface DynamicDataSource {


    /**
     * 获取数据源ID
     * @return 数据源ID
     * @see DataSourceSwitcher#currentDataSourceId()
     */
    String getId();

    /**
     * 获取数据源类型
     * @return 数据库类型
     * @see DatabaseType
     */
    DatabaseType getType();

    /**
     * 获取原始数据源
     * @return 原始数据源
     */
    DataSource getNative();



}
