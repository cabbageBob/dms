package net.htwater.sesame.dms.datasource;

import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfigRepository;
import net.htwater.sesame.dms.datasource.config.InSpringDynamicDataSourceConfig;
import net.htwater.sesame.dms.datasource.service.InSpringContextDynamicDataSourceService;
import net.htwater.sesame.dms.datasource.service.InSpringDynamicDataSourceConfigRepository;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

/**
 * @author Jokki
 */
@Configuration
public class DynamicDataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SqlExecutor.class)
    public SqlExecutor sqlExecutor() {
        return new DefaultJdbcExecutor();
    }

    @Bean
    @ConditionalOnMissingBean(DynamicDataSourceConfigRepository.class)
    public InSpringDynamicDataSourceConfigRepository inSpringDynamicDataSourceConfigRepository() {
        return new InSpringDynamicDataSourceConfigRepository();
    }

    @Bean
    @ConditionalOnMissingBean(DynamicDataSourceService.class)
    public InSpringContextDynamicDataSourceService inMemoryDynamicDataSourceService
            (DynamicDataSourceConfigRepository<InSpringDynamicDataSourceConfig> repository) {
        return new InSpringContextDynamicDataSourceService(repository);
    }

    @Bean
    public BeanPostProcessor switcherInitProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(@Nullable Object bean, String beanName) {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
                if (bean instanceof DynamicDataSourceService) {
                    DataSourceHolder.dynamicDataSourceService = ((DynamicDataSourceService) bean);
                }
                return bean;
            }
        };
    }

}
