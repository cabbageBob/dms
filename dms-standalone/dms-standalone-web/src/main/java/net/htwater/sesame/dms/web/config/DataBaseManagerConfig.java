package net.htwater.sesame.dms.web.config;

import core.DatabaseType;
import core.dialect.Dialect;
import core.dialect.MySqlDialect;
import core.dialect.OracleDialect;
import core.dialect.SqlServerDialect;
import core.meta.ObjectType;
import net.htwater.sesame.dms.web.service.SimpleDatabaseManagerService;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataBaseManagerConfig {
    private final SqlExecutor sqlExecutor;
    private final SimpleDatabaseManagerService simpleDatabaseManagerService;

    @Autowired
    public DataBaseManagerConfig(SqlExecutor sqlExecutor, SimpleDatabaseManagerService simpleDatabaseManagerService) {
        this.sqlExecutor = sqlExecutor;
        this.simpleDatabaseManagerService = simpleDatabaseManagerService;
    }

    /*@Bean
    @Qualifier("simpleDataSourceConfigService")
    DataSourceConfigService dataSourceConfigService(){
        return new SimpleDataSourceConfigService();
    }*/
    /*@Bean
    public InDBDataSourceConfigRepository inDBDataSourceRepository(DataSourceConfigService dataSourceConfigService) {
        return new InDBDataSourceConfigRepository(dataSourceConfigService);
    }
    @Bean
    public DynamicDataSourceService inDBDynamicDataSourceService(InDBDataSourceConfigRepository dbDataSourceConfigRepository){
        return  new InDBDynamicDataSourceService(dbDataSourceConfigRepository);
    }*/

    @Bean
    @ConditionalOnClass(name = "com.mysql.cj.jdbc.Driver")
    public MySqlDialect mySqlDialect() {
        return new MySqlDialect(sqlExecutor);
    }

    @Bean
    @ConditionalOnClass(name = "oracle.jdbc.driver.OracleDriver")
    public OracleDialect oracleDialect() {
        return new OracleDialect(sqlExecutor);
    }

    @Bean
    @ConditionalOnClass(name = "com.microsoft.sqlserver.jdbc.SQLServerDriver")
    public SqlServerDialect sqlServerDialect() {
        return new SqlServerDialect(sqlExecutor);
    }

    @Bean
    public BeanPostProcessor tableMetaDataAutoParserRegister() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
                return o;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
                if (o instanceof Dialect) {
                    Dialect supplier = ((Dialect) o);
                    for (DatabaseType databaseType : DatabaseType.values()) {
                        if (supplier.isSupport(databaseType)) {
                            simpleDatabaseManagerService.registerMetaDataParser(databaseType, ObjectType.TABLE, supplier.get());
                        }
                    }
                }
                return o;
            }
        };
    }

}
