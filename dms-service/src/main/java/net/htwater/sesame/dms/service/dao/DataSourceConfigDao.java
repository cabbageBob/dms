package net.htwater.sesame.dms.service.dao;

import net.htwater.sesame.dms.service.config.InDBDynamicDataSourceConfig;

import java.util.List;

public interface DataSourceConfigDao {
    List<InDBDynamicDataSourceConfig> findAll();
    InDBDynamicDataSourceConfig findById(String dataSourceId);
    boolean add(InDBDynamicDataSourceConfig config);
    boolean remove(String dataSourceId);
}
