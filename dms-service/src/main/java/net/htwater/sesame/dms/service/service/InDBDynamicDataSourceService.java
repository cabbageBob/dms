package net.htwater.sesame.dms.service.service;

import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.datasource.DataSourceHolder;
import net.htwater.sesame.dms.datasource.DynamicDataSource;
import net.htwater.sesame.dms.datasource.DynamicDataSourceProxy;
import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfigRepository;
import net.htwater.sesame.dms.datasource.service.AbstractDynamicDataSourceService;
import net.htwater.sesame.dms.datasource.service.DataSourceCache;
import net.htwater.sesame.dms.service.config.InDBDynamicDataSourceConfig;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class InDBDynamicDataSourceService extends AbstractDynamicDataSourceService<InDBDynamicDataSourceConfig> {
    public InDBDynamicDataSourceService(DynamicDataSourceConfigRepository<InDBDynamicDataSourceConfig> repository) {
        super(repository);
    }
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    @Override
    public DynamicDataSource getDataSource(String dataSourceId){
        try {
            DataSourceHolder.switcher().useDefault();
            return super.getDataSource(dataSourceId);
        }finally {
            DataSourceHolder.switcher().useLast();
        }
    }

    protected void closeDataSource(DataSource dataSource){
        if(null==dataSource)
            return;
        try {
            if (dataSource instanceof Closeable){
                ((Closeable) dataSource).close();
            }else {
                Method closeMethod = ReflectionUtils.findMethod(dataSource.getClass(),"close");
                if (closeMethod !=null){
                    ReflectionUtils.invokeMethod(closeMethod,dataSource);
                }
            }
        } catch (Exception e) {
            log.warn("关闭数据源[{}]失败", dataSource, e);
        }
    }
    @Override
    protected DataSourceCache createCache(InDBDynamicDataSourceConfig config) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        Map<String,Object> properties =  config.getProperties();
        dataSourceProperties.setDriverClassName((String) properties.get("DriverClassName"));
        dataSourceProperties.setUrl((String) properties.get("url"));
        dataSourceProperties.setUsername((String) properties.get("username"));
        dataSourceProperties.setPassword((String) properties.get("passowrd"));
        AtomicReference<DataSource> dataSourceReference = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean();
        AtomicBoolean success = new AtomicBoolean();
        int initTimeOut = Integer.parseInt(String.valueOf(config.getProperties().getOrDefault("InitTimeout", "20")));

        executorService.submit(()->{
            try {
                DataSource dataSource = dataSourceProperties
                        .initializeDataSourceBuilder()
                        .build();
                dataSourceReference.set(dataSource);
                dataSource.getConnection().close();
                if (closed.get()){
                    closeDataSource(dataSource);
                }else {
                    success.set(true);
                }
            }catch (Exception e){
                log.warn("初始化数据源[{}]失败", config.getId(), e);
            }finally {
                countDownLatch.countDown();
            }
        });

        try{
            @SuppressWarnings("all")
            boolean waitSuccess = countDownLatch.await(initTimeOut,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          //  e.printStackTrace();
        }
        if (!success.get()){
            closed.set(true);
            closeDataSource(dataSourceReference.get());
            try {
                throw new Exception("初始化数据源[" + config.getId() + "]失败");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new DataSourceCache(
                config.getProperties().hashCode(),
                new DynamicDataSourceProxy(config.getId(),dataSourceReference.get()),
                countDownLatch,config){
            @Override
            public void closeDataSource(){
                super.closeDataSource();
                closed.set(true);
                InDBDynamicDataSourceService.this.closeDataSource(getDataSource().getNative());
            }
        };
    }
}
