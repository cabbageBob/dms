package net.htwater.sesame.dms.web.service;

import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Jokki
 */
@Component
public class DataSourceMetaAsyncTask {

    private final SimpleDatabaseManagerService simpleDatabaseManagerService;


    @Autowired
    public DataSourceMetaAsyncTask(SimpleDatabaseManagerService simpleDatabaseManagerService) {
        this.simpleDatabaseManagerService = simpleDatabaseManagerService;
    }

    @Async
    public void initMetas(String datasourceId) {
        DataSourceHolder.switcher().use(datasourceId);
        simpleDatabaseManagerService.getMetas();
    }
}
