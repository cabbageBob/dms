package net.htwater.sesame.dms.service.config;

import net.htwater.sesame.dms.datasource.DynamicDataSourceService;
import net.htwater.sesame.dms.service.service.DataSourceConfigService;
import net.htwater.sesame.dms.service.service.InDBDynamicDataSourceService;
import net.htwater.sesame.dms.service.service.SimpleDataSourceConfigService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class applicationconfig {

    @Bean
    @Qualifier("simpleDataSourceConfigService")
    DataSourceConfigService dataSourceConfigService(){
        return new SimpleDataSourceConfigService();
    }
    @Bean
    public InDBDataSourceConfigRepository inDBDataSourceRepository(DataSourceConfigService dataSourceConfigService) {
        return new InDBDataSourceConfigRepository(dataSourceConfigService);
    }
    @Bean
    public DynamicDataSourceService inDBDynamicDataSourceService(InDBDataSourceConfigRepository dbDataSourceConfigRepository){
        return  new InDBDynamicDataSourceService(dbDataSourceConfigRepository);
    }
}
