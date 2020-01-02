package net.htwater.sesame.dms.web.service;

import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.dataSource.DynamicDataSource;
import net.htwater.sesame.dms.web.dataSource.DynamicDataSourceService;
import net.htwater.sesame.dms.web.domain.DataSourceStatus;
import net.htwater.sesame.dms.web.repository.DataSourceStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Jokki
 */
@Component
@Slf4j
public class AsyncTask {

    private final DataSourceStatusRepository dataSourceStatusRepository;

    private final DynamicDataSourceService dynamicDataSourceService;

    @Autowired
    public AsyncTask(DataSourceStatusRepository dataSourceStatusRepository,
                     DynamicDataSourceService dynamicDataSourceService) {
        this.dataSourceStatusRepository = dataSourceStatusRepository;
        this.dynamicDataSourceService = dynamicDataSourceService;
    }

    @Async
    public void saveStatus(String datasourceId) {
        DataSourceStatus dataSourceStatus = new DataSourceStatus();
        dataSourceStatus.setId(datasourceId);
        try {
            DynamicDataSource dataSource = dynamicDataSourceService.getDataSource(datasourceId);
            if (dataSource != null && dataSource.getNative() != null) {
                dataSource.getNative().getConnection().close();
                dataSourceStatus.setStatus(1);
            }
        } catch (Exception e) {
            log.warn(String.format("数据源[%s]连接失败", datasourceId), e);
            dynamicDataSourceService.removeCache(datasourceId);
        }
        dataSourceStatus.setUpdateAt(new Date());
        dataSourceStatusRepository.save(dataSourceStatus);
    }
}
