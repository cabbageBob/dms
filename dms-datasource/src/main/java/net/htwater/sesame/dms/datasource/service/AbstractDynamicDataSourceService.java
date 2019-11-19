package net.htwater.sesame.dms.datasource.service;

import net.htwater.sesame.dms.datasource.DynamicDataSource;
import net.htwater.sesame.dms.datasource.DynamicDataSourceService;
import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfig;
import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfigRepository;
import net.htwater.sesame.dms.datasource.exception.DataSourceNotFoundException;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础动态数据源服务
 * @author Jokki
 */
public abstract class AbstractDynamicDataSourceService<C extends DynamicDataSourceConfig> implements DynamicDataSourceService {
    protected final Map<String, DataSourceCache> dataSourceStore = new ConcurrentHashMap<>(32);

    private DynamicDataSourceConfigRepository<C> repository;

    public void setRepository(DynamicDataSourceConfigRepository<C> repository) {
        this.repository = repository;
    }

    public AbstractDynamicDataSourceService(DynamicDataSourceConfigRepository<C> repository) {
        this.repository = repository;
    }

    @PreDestroy
    public void destroy() {
        dataSourceStore.values().forEach(DataSourceCache::closeDataSource);
    }

    @Override
    public DynamicDataSource getDataSource(String dataSourceId) {
        C config = repository.findById(dataSourceId);
        if (config == null) {
            throw new DataSourceNotFoundException(dataSourceId);
        }
        DataSourceCache cache = dataSourceStore.get(dataSourceId);
        if (cache == null) {
            cache = createCache(config);
            dataSourceStore.put(dataSourceId, cache);
            return cache.getDataSource();
        }
        if (cache.getHash() != config.hashCode()) {
            dataSourceStore.remove(dataSourceId);
            cache.closeDataSource();
            //重新获取
            return getDataSource(dataSourceId);
        }
        return cache.getDataSource();
    }

    protected abstract DataSourceCache createCache(C config);

    public DataSourceCache removeCache(String id) {
        return dataSourceStore.remove(id);
    }
}
