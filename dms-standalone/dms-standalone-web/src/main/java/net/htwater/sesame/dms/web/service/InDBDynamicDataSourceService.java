package net.htwater.sesame.dms.web.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import net.htwater.sesame.dms.web.dataSource.DynamicDataSource;
import net.htwater.sesame.dms.web.dataSource.DynamicDataSourceProxy;
import net.htwater.sesame.dms.web.entity.InDBDynamicDataSourceConfig;
import net.htwater.sesame.dms.web.util.AESUtil;
import net.htwater.sesame.dms.web.util.JdbcUtil;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class InDBDynamicDataSourceService extends AbstractDynamicDataSourceService<InDBDynamicDataSourceConfig> {
    public InDBDynamicDataSourceService(DynamicDataSourceConfigRepository<InDBDynamicDataSourceConfig> repository) {
        super(repository);
    }
    private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("InDBDynamicDataSourceService-pool-%d").build();
    private ExecutorService executorService = new ThreadPoolExecutor(1, 2,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    @Override
    public DynamicDataSource getDataSource(String dataSourceId){
        try {
            DataSourceHolder.switcher().useDefault();
            return super.getDataSource(dataSourceId);
        }finally {
            DataSourceHolder.switcher().useLast();
        }
    }

    private void closeDataSource(DataSource dataSource){
        if(null==dataSource) {
            return;
        }
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
        dataSourceProperties.setType(HikariDataSource.class);
        dataSourceProperties.setUrl(JdbcUtil.parseUrl(config.getDbtype(),config.getIp(),config.getPort(),config.getDbname()));
        dataSourceProperties.setUsername(config.getUsername());
        dataSourceProperties.setPassword(AESUtil.decrypt(config.getPassword(),AESUtil.ENCODE_KEY));
        AtomicReference<DataSource> dataSourceReference = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean();
        AtomicBoolean success = new AtomicBoolean();
        int initTimeOut = 10;

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
            log.warn("初始化数据源[{}]时发生错误", config.getId(), e);
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
                config.hashCode(),
                new DynamicDataSourceProxy(config.getId(),dataSourceReference.get()),
                countDownLatch,config, closed.get()){
            @Override
            public void closeDataSource(){
                super.closeDataSource();
                closed.set(true);
                InDBDynamicDataSourceService.this.closeDataSource(getDataSource().getNative());
            }
        };
    }

    @Override
    public DataSourceCache removeCache(String id) {
        log.debug("remove datasourceCache - ", id);
        DataSourceCache dataSourceCache = dataSourceStore.get(id);
        if (dataSourceCache != null) {
            dataSourceCache.closeDataSource();
            return dataSourceStore.remove(id);
        }
        return null;
    }
}
