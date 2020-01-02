package net.htwater.sesame.dms.web.job;

import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.service.AsyncTask;
import net.htwater.sesame.dms.web.service.SimpleDataSourceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecondsJob {

    private final SimpleDataSourceConfigService simpleDataSourceConfigService;


    private final AsyncTask asyncTask;

    @Autowired
    public SecondsJob(SimpleDataSourceConfigService simpleDataSourceConfigService,
                      AsyncTask asyncTask) {
        this.simpleDataSourceConfigService = simpleDataSourceConfigService;
        this.asyncTask = asyncTask;
    }

    @Scheduled(cron = "0 0/2 * * * ? ")
    public void dataSourceInfoCache(){
        log.debug("开始更新数据源状态信息");
        simpleDataSourceConfigService
                .findAll()
                .forEach(config ->
                    asyncTask.saveStatus(config.getId())
                );
    }
}
