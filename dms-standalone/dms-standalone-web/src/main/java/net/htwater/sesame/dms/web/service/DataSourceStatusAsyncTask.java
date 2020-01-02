package net.htwater.sesame.dms.web.service;

import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import net.htwater.sesame.dms.web.domain.DataSourceStatus;
import net.htwater.sesame.dms.web.repository.DataSourceStatusRepository;
import net.htwater.sesame.dms.web.util.AESUtil;
import net.htwater.sesame.dms.web.util.JdbcUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Jokki
 */
@Component
public class DataSourceStatusAsyncTask {

    private final DataSourceStatusRepository dataSourceStatusRepository;

    public DataSourceStatusAsyncTask(DataSourceStatusRepository dataSourceStatusRepository) {
        this.dataSourceStatusRepository = dataSourceStatusRepository;
    }

    @Async
    public void saveStatus(DataSourceConfig config) {
        DataSourceStatus dataSourceStatus = new DataSourceStatus();
        dataSourceStatus.setId(config.getId());
        if (JdbcUtil.testConncet(config.getDbtype(),config.getIp(),config.getPort(),
                config.getDbname(),config.getUsername(), AESUtil.decrypt(config.getPassword(), AESUtil.ENCODE_KEY))) {
            dataSourceStatus.setStatus(1);
        }
        dataSourceStatus.setUpdateAt(new Date());
        dataSourceStatusRepository.save(dataSourceStatus);
    }
}
