package net.htwater.sesame.dms.web.dataSource;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

/**
 * @author Jokki
 */
@Configuration
@Order(-1)
public class DynamicDataSourceAutoConfiguration {

    @Bean
    public DefaultJdbcExecutor sqlExecutor() {
        return new DefaultJdbcExecutor();
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
