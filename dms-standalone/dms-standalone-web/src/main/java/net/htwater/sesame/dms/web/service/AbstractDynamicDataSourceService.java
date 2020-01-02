package net.htwater.sesame.dms.web.service;

import net.htwater.sesame.dms.web.dataSource.DynamicDataSource;
import net.htwater.sesame.dms.web.dataSource.DynamicDataSourceService;
import net.htwater.sesame.dms.web.entity.DynamicDataSourceConfig;
import net.htwater.sesame.dms.web.exception.DataSourceNotFoundException;

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
        if (cache.getHash() != config.hashCode() || cache.isClosed()) {
            dataSourceStore.remove(dataSourceId);
            cache.closeDataSource();
            //重新获取
            return getDataSource(dataSourceId);
        }
        return cache.getDataSource();
    }

    protected abstract DataSourceCache createCache(C config);

    @Override
    public DataSourceCache removeCache(String id) {
        return dataSourceStore.remove(id);
    }
}
