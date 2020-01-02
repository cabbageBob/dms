package net.htwater.sesame.dms.web.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import net.htwater.sesame.dms.web.domain.TableCache;
import net.htwater.sesame.dms.web.entity.SearchFieldEntity;
import net.htwater.sesame.dms.web.repository.TableCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jokki
 */
@Service
@Slf4j
public class TableCacheServiceImpl implements TableCacheService {

    private final TableCacheRepository tableCacheRepository;

    private final DataSourceConfigService dataSourceConfigService;

    private static final ConcurrentHashMap<String, AtomicInteger> LOCK_MAP = new ConcurrentHashMap<>(20);

    @Autowired
    public TableCacheServiceImpl(TableCacheRepository tableCacheRepository,
                                 DataSourceConfigService dataSourceConfigService) {
        this.tableCacheRepository = tableCacheRepository;
        this.dataSourceConfigService = dataSourceConfigService;
    }

    @Override
    public List<TableCache> findAllByDatasourceId(String datasourceId) {
        return tableCacheRepository.findAllByDatasourceId(datasourceId);
    }

    @Override
    public TableCache save(TableCache cache) {
        TableCache tableCache = new TableCache();
        try {
            synchronized (getAtomicInteger(cache.getId())) {
                tableCache = tableCacheRepository.save(cache);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            giveUpAtomicInteger(cache.getId());
        }
        return tableCache;
    }

    @Override
    public Optional<TableCache> findById(String id) {
        return tableCacheRepository.findById(id);
    }

    @Override
    public void deleteById(String id) {
        tableCacheRepository.deleteById(id);
    }

    @Override
    public void deleteByDatasourceId(String datasourceId) {
        log.debug("清除数据源数据字典缓存", datasourceId);
        tableCacheRepository.deleteAllByDatasourceId(datasourceId);
    }

    @Override
    public void deleteAll() {
        log.debug("清除所有数据源数据字典缓存");
        tableCacheRepository.deleteAll();
    }

    @Override
    public List<TableCache> findAllByIds(List<String> ids) {
        return tableCacheRepository.findAllByIds(ids);
    }

    @Override
    public List<TableCache> findAllByQ(String q) {
        return tableCacheRepository.findAllByRegex(q);
    }

    @Override
    public List<SearchFieldEntity> searchFieldsByQ(String q) {
        List<DataSourceConfig> dataSourceConfigs = dataSourceConfigService.findAll();
        List<String> datasourceIds = new ArrayList<>(dataSourceConfigs.size());
        Map<String, DataSourceConfig> stringDataSourceConfigMap = Maps.newHashMap();
        dataSourceConfigs.forEach(dataSourceConfig -> {
            datasourceIds.add(dataSourceConfig.getId());
            stringDataSourceConfigMap.put(dataSourceConfig.getId(), dataSourceConfig);
        });
        List<SearchFieldEntity> searchFields = Lists.newArrayList();
        tableCacheRepository.findAllFieldsByRegex(q, datasourceIds)
                .forEach(tableCache -> {
                    DataSourceConfig dataSourceConfig = stringDataSourceConfigMap.get(tableCache.getDatasourceId());
                    if (dataSourceConfig != null && !CollectionUtils.isEmpty(tableCache.getColumns())) {
                        SearchFieldEntity searchField = new SearchFieldEntity();
                        searchField.setDatasourceId(tableCache.getDatasourceId());
                        searchField.setDbName(dataSourceConfig.getDbname());
                        searchField.setDisplayDbName(dataSourceConfig.getName());
                        searchField.setIp(dataSourceConfig.getIp());
                        searchField.setPort(dataSourceConfig.getPort());
                        searchField.setTableComment(tableCache.getComment());
                        searchField.setTableName(tableCache.getName());
                        searchField.setName(tableCache.getColumns().get(0).getName());
                        searchField.setComment(tableCache.getColumns().get(0).getComment());
                        searchFields.add(searchField);
                    }
                });
        return searchFields;
    }

    private AtomicInteger getAtomicInteger(String key) {
        // 当实体ID锁资源为空,初始化锁
        if (LOCK_MAP.get(key) == null) {
            // 初始化一个竞争数为0的原子资源
            LOCK_MAP.putIfAbsent(key, new AtomicInteger(0));
        }
        AtomicInteger resource = LOCK_MAP.get(key);
        // 线程得到该资源,原子性+1
        int count = resource.incrementAndGet();
        log.debug("资源ID为: " + key + ", 争抢线程数: "  + count);
        // 返回该ID资源锁
        return resource;
    }

    private void giveUpAtomicInteger(String key) {
        if (LOCK_MAP.get(key) != null) {
            // 线程释放该资源,原子性-1
            int source = LOCK_MAP.get(key).decrementAndGet();
            // 当资源没有线程竞争的时候，就删除掉该锁,防止内存溢出
            if (source <= 0) {
                LOCK_MAP.remove(key);
                log.debug("资源ID为:" + key + "移除成功");
            }
            log.debug("资源ID为:" + key + ",争抢线程数:" + source);
        }
    }
}
