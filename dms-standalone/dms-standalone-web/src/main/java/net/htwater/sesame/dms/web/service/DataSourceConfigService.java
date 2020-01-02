package net.htwater.sesame.dms.web.service;


import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import net.htwater.sesame.dms.web.entity.UpdatedDataSourceConfig;
import net.htwater.sesame.dms.web.entity.cache.DataSourceInfo;

import java.util.List;

public interface DataSourceConfigService {
    List<DataSourceConfig> findAll();
    DataSourceConfig findById(String dataSourceId);
    DataSourceConfig add(DataSourceConfig config);
    boolean remove(String dataSourceId);
    DataSourceConfig update(UpdatedDataSourceConfig updateConfig);
    List<DataSourceInfo> getInfo();
}
