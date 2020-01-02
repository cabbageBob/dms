package net.htwater.sesame.dms.web.job;

import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import net.htwater.sesame.dms.web.service.DataSourceConfigService;
import net.htwater.sesame.dms.web.service.SimpleDatabaseManagerService;
import net.htwater.sesame.dms.web.service.TableCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DayJob {
    private final SimpleDatabaseManagerService simpleDatabaseManagerService;

    private final TableCacheService tableCacheService;

    private final DataSourceConfigService configService;

    @Autowired
    public DayJob(SimpleDatabaseManagerService simpleDatabaseManagerService,
                  TableCacheService tableCacheService,
                  DataSourceConfigService configService) {
        this.simpleDatabaseManagerService = simpleDatabaseManagerService;
        this.tableCacheService = tableCacheService;
        this.configService = configService;
    }

   /* @Scheduled(cron = "0 0 0 * * ? ")
    public void importDBTable(){
        log.info("开始更新所有表缓存任务");
        simpleDatabaseManagerService.importDBTable();
        log.info("任务结束");
    }*/

    @Scheduled(cron = "0 0 1 * * ? ")
    public void refreshCache() {
        log.debug("开始更新所有表缓存任务");
        configService.findAll()
                .forEach(config -> {
                    DataSourceHolder.switcher().use(config.getId());
                    tableCacheService.deleteByDatasourceId(config.getId());
                    simpleDatabaseManagerService.getMetas();
                });
        log.debug("任务结束");
    }
}
