package net.htwater.sesame.dms.web.job;

import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.service.DataSourceConfigService;
import net.htwater.sesame.dms.web.service.SimpleDatabaseManagerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Jokki
 */
@Component
@Slf4j
public class MonitorJob {
    private final SimpleDatabaseManagerService simpleDatabaseManagerService;

    private final DataSourceConfigService configService;

    public MonitorJob(SimpleDatabaseManagerService simpleDatabaseManagerService,
                      DataSourceConfigService configService) {
        this.simpleDatabaseManagerService = simpleDatabaseManagerService;
        this.configService = configService;
    }

    @Scheduled(cron = "0 0/30 * * * ? ")
    public void refreshCache() {
        log.debug("开始数据源监控任务");
        configService.findAll()
                .forEach(config ->
                    simpleDatabaseManagerService.monitorDatasource(config.getId(), config.getDbname())
                );
    }
}
