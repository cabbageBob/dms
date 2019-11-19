package net.htwater.sesame.dms.service.config;

import lombok.NoArgsConstructor;
import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfigRepository;
import net.htwater.sesame.dms.service.service.DataSourceConfigService;

import java.util.List;

/**
 * 从数据库获取数据源信息
 */
@NoArgsConstructor
public class InDBDataSourceConfigRepository implements DynamicDataSourceConfigRepository<InDBDynamicDataSourceConfig> {
    private DataSourceConfigService dataSourceConfigService;
    public InDBDataSourceConfigRepository(DataSourceConfigService dataSourceConfigService){
        this.dataSourceConfigService = dataSourceConfigService;
    }
    public void setDataSourceConfigService(DataSourceConfigService dataSourceConfigService) {
        this.dataSourceConfigService = dataSourceConfigService;
    }
    @Override
    public List<InDBDynamicDataSourceConfig> findAll() {
        return dataSourceConfigService.findAll();
    }

    @Override
    public InDBDynamicDataSourceConfig findById(String dataSourceId) {
        return dataSourceConfigService.findById(dataSourceId);
    }

    @Override
    public InDBDynamicDataSourceConfig add(InDBDynamicDataSourceConfig config) {
        throw new UnsupportedOperationException("add AtomikosDataSourceConfig not support");
    }

    @Override
    public InDBDynamicDataSourceConfig remove(String dataSourceId) {
        throw new UnsupportedOperationException("remove datasource not support");
    }
}
