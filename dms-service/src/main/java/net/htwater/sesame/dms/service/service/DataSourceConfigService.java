package net.htwater.sesame.dms.service.service;

import net.htwater.sesame.dms.service.config.InDBDynamicDataSourceConfig;

import java.util.List;

public interface DataSourceConfigService {
    List<InDBDynamicDataSourceConfig> findAll();
    InDBDynamicDataSourceConfig findById(String dataSourceId);
    boolean add(InDBDynamicDataSourceConfig config);
    boolean remove(String dataSourceId);
}
