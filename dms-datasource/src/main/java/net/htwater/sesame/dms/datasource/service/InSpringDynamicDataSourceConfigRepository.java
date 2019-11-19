package net.htwater.sesame.dms.datasource.service;

import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfigRepository;
import net.htwater.sesame.dms.datasource.config.InSpringDynamicDataSourceConfig;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * spring容器中的数据源
 * @author Jokki
 */
public class InSpringDynamicDataSourceConfigRepository implements DynamicDataSourceConfigRepository<InSpringDynamicDataSourceConfig>, BeanPostProcessor {
    private Map<String, InSpringDynamicDataSourceConfig> configMap = new HashMap<>();

    @Override
    public List<InSpringDynamicDataSourceConfig> findAll() {
        return new ArrayList<>(configMap.values());
    }

    @Override
    public InSpringDynamicDataSourceConfig findById(String dataSourceId) {
        return configMap.get(dataSourceId);
    }

    @Override
    public InSpringDynamicDataSourceConfig add(InSpringDynamicDataSourceConfig config) {
        return configMap.put(config.getId(), config);
    }

    @Override
    public InSpringDynamicDataSourceConfig remove(String dataSourceId) {
        return configMap.remove(dataSourceId);
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) {
        if (o instanceof DataSource) {
            InSpringDynamicDataSourceConfig config = new InSpringDynamicDataSourceConfig();
            config.setId(s);
            config.setBeanName(s);
            config.setName(s);
            add(config);
        }
        return o;
    }
}
