package net.htwater.sesame.dms.web.service;


import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import net.htwater.sesame.dms.web.domain.DataSourceStatus;
import net.htwater.sesame.dms.web.entity.UpdatedDataSourceConfig;
import net.htwater.sesame.dms.web.entity.cache.DataSourceInfo;
import net.htwater.sesame.dms.web.repository.DataSourceConfigRepository;
import net.htwater.sesame.dms.web.repository.DataSourceStatusRepository;
import net.htwater.sesame.dms.web.repository.TableCacheRepository;
import net.htwater.sesame.dms.web.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class SimpleDataSourceConfigService implements DataSourceConfigService {
    private final DataSourceConfigRepository dataSourceConfigDao;
    private final DataSourceStatusRepository dataSourceStatusRepository;
    private final DataSourceStatusAsyncTask dataSourceStatusAsyncTask;
    private final TableCacheRepository tableCacheRepository;
    private final EventBus eventBus;

    @Autowired
    public SimpleDataSourceConfigService(DataSourceConfigRepository dataSourceConfigDao,
                                         DataSourceStatusRepository dataSourceStatusRepository,
                                         DataSourceStatusAsyncTask dataSourceStatusAsyncTask,
                                         TableCacheRepository tableCacheRepository, EventBus eventBus) {
        this.dataSourceConfigDao = dataSourceConfigDao;
        this.dataSourceStatusRepository = dataSourceStatusRepository;
        this.dataSourceStatusAsyncTask = dataSourceStatusAsyncTask;
        this.tableCacheRepository = tableCacheRepository;
        this.eventBus = eventBus;
    }

    @Override
    public List<DataSourceConfig> findAll() {
        return dataSourceConfigDao.findAll();
    }

    @Override
    public DataSourceConfig findById(String dataSourceId) {
        return dataSourceConfigDao.findById(dataSourceId).orElse(null);
    }

    @Override
    public DataSourceConfig add(DataSourceConfig config) {
        String password =AESUtil.encrypt(config.getPassword(), AESUtil.ENCODE_KEY);
        String id = UUID.randomUUID().toString();
        config.setPassword(password);
        config.setId(id);
        DataSourceConfig newDataSourceConfig = dataSourceConfigDao.save(config);
        dataSourceStatusAsyncTask.saveStatus(newDataSourceConfig);
        return newDataSourceConfig;
    }

    @Override
    public DataSourceConfig update(UpdatedDataSourceConfig updateConfig) {
        if (updateConfig.getIsUpdatePassword() == 1) {
            updateConfig.setPassword(AESUtil.encrypt(updateConfig.getPassword(),AESUtil.ENCODE_KEY));
        }
        DataSourceConfig config = convert(updateConfig);
        DataSourceConfig newConfig = dataSourceConfigDao.save(config);
        eventBus.post(config.getId());
        return newConfig;
    }

    @Override
    public boolean remove(String dataSourceId) {
        log.debug("删除数据源: ", dataSourceId);
        dataSourceConfigDao.deleteById(dataSourceId);
        eventBus.post(dataSourceId);
        try {
            dataSourceStatusRepository.deleteById(dataSourceId);
            tableCacheRepository.deleteAllByDatasourceId(dataSourceId);
        } catch (Exception e) {
            log.warn(String.format("删除数据源[%s]状态或缓存失败:", dataSourceId));
        }
        return true;
    }

    @Override
    public List<DataSourceInfo> getInfo(){
        return findAll().stream()
                .map(config -> {
                    Optional<DataSourceStatus> status =
                            dataSourceStatusRepository.findById(config.getId());
                    return status.map(dataSourceStatus ->
                            convert(config, dataSourceStatus.getStatus())).orElseGet(() ->
                            convert(config, 0));
                }).collect(Collectors.toList());
    }


    private DataSourceInfo convert(DataSourceConfig config, int status) {
        return new DataSourceInfo(config.getId(), config.getIp(), config.getPort(),
                config.getDbname(), config.getName(), config.getUsername(), config.getPassword(), config.getDbtype(), config.getDescribe(), status);
    }

    private DataSourceConfig convert(UpdatedDataSourceConfig updateConfig) {
        DataSourceConfig config = new DataSourceConfig();
        config.setId(updateConfig.getId());
        config.setPassword(updateConfig.getPassword());
        config.setDbname(updateConfig.getDbname());
        config.setDbtype(updateConfig.getDbtype());
        config.setDescribe(updateConfig.getDescribe());
        config.setIp(updateConfig.getIp());
        config.setPort(updateConfig.getPort());
        config.setName(updateConfig.getName());
        config.setUsername(updateConfig.getUsername());
        config.setDatabaseGenre(updateConfig.getDatabaseGenre());
        return config;
    }
}
