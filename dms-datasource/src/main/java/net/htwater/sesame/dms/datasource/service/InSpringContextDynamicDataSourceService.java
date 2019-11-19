package net.htwater.sesame.dms.datasource.service;

import net.htwater.sesame.dms.datasource.DynamicDataSourceProxy;
import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfigRepository;
import net.htwater.sesame.dms.datasource.config.InSpringDynamicDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.util.concurrent.CountDownLatch;

/**
 * 基于spring容器的动态数据源服务.从spring容器中获取数据源
 * @author Jokki
 */
public class InSpringContextDynamicDataSourceService extends AbstractDynamicDataSourceService<InSpringDynamicDataSourceConfig> {
    private ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public InSpringContextDynamicDataSourceService(DynamicDataSourceConfigRepository<InSpringDynamicDataSourceConfig> repository) {
        super(repository);
    }

    @Override
    protected DataSourceCache createCache(InSpringDynamicDataSourceConfig config) {
        DataSource dataSource = applicationContext.getBean(config.getBeanName(), DataSource.class);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            return new DataSourceCache(config.hashCode(),
                    new DynamicDataSourceProxy(config.getId(), dataSource),
                    countDownLatch,
                    config);
        } finally {
            countDownLatch.countDown();
        }
    }
}
