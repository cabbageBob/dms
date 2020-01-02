package net.htwater.sesame.dms.web.config;

import lombok.NoArgsConstructor;
import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import net.htwater.sesame.dms.web.service.DynamicDataSourceConfigRepository;
import net.htwater.sesame.dms.web.entity.InDBDynamicDataSourceConfig;
import net.htwater.sesame.dms.web.service.DataSourceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 从数据库获取数据源信息
 */
@NoArgsConstructor
@Component
public class InDBDataSourceConfigRepository implements DynamicDataSourceConfigRepository<InDBDynamicDataSourceConfig> {
    private DataSourceConfigService dataSourceConfigService;
    @Autowired
    public InDBDataSourceConfigRepository(DataSourceConfigService dataSourceConfigService){
        this.dataSourceConfigService = dataSourceConfigService;
    }

    @Override
    public List<InDBDynamicDataSourceConfig> findAll() {
        return dataSourceConfigService.findAll().stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    @Nullable
    public InDBDynamicDataSourceConfig findById(String dataSourceId) {
        return convert(dataSourceConfigService
                .findById(dataSourceId));
    }

    @Override
    public InDBDynamicDataSourceConfig add(InDBDynamicDataSourceConfig config) {
        throw new UnsupportedOperationException("add AtomikosDataSourceConfig not support");
    }

    @Override
    public InDBDynamicDataSourceConfig remove(String dataSourceId) {
        throw new UnsupportedOperationException("remove datasource not support");
    }

    private InDBDynamicDataSourceConfig convert(DataSourceConfig config) {
        if (config != null) {
            return new InDBDynamicDataSourceConfig(config.getId(), config.getIp(), config.getPort(), config.getDbname(),
                    config.getName(),
                    config.getUsername(), config.getPassword(), config.getDbtype(), config.getDescribe());
        }
        return null;
    }

}
