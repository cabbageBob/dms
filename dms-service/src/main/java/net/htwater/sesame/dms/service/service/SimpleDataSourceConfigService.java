package net.htwater.sesame.dms.service.service;

import net.htwater.sesame.dms.service.config.InDBDynamicDataSourceConfig;
import net.htwater.sesame.dms.service.dao.DataSourceConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimpleDataSourceConfigService implements DataSourceConfigService {
    @Autowired
    private DataSourceConfigDao dataSourceConfigDao;

    public List<InDBDynamicDataSourceConfig> findAll() {
        return dataSourceConfigDao.findAll();
    }

    public InDBDynamicDataSourceConfig findById(String dataSourceId) {
        return dataSourceConfigDao.findById(dataSourceId);
    }

    public boolean add(InDBDynamicDataSourceConfig config) {
        return dataSourceConfigDao.add(config);
    }

    public boolean remove(String dataSourceId) {
        return dataSourceConfigDao.remove(dataSourceId);
    }

}
