package net.htwater.sesame.dms.datasource.service;

import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.datasource.DynamicDataSource;
import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfig;

import java.util.concurrent.CountDownLatch;

/**
 * 初始化完成的数据源缓存
 * @author Jokki
 */
@Slf4j
public class DataSourceCache {

    private long hash;

    private volatile boolean closed;

    private DynamicDataSource dataSource;

    private volatile CountDownLatch initLatch;

    public long getHash() {
        return hash;
    }
    private DynamicDataSourceConfig config;

    public DynamicDataSource getDataSource() {
        if (initLatch != null) {
            try {
                //等待初始化完成
                initLatch.await();
            } catch (Exception ignored) {
                log.warn(ignored.getMessage(),ignored);

            } finally {
                initLatch = null;
            }
        }
        return dataSource;
    }

    public DataSourceCache(long hash, DynamicDataSource dataSource, CountDownLatch initLatch,
                           DynamicDataSourceConfig config) {
        this.hash = hash;
        this.dataSource = dataSource;
        this.initLatch = initLatch;
        this.config=config;
    }

    public boolean isClosed() {
        return closed;
    }


    public void closeDataSource() {
        closed = true;
    }

    public DynamicDataSourceConfig getConfig() {
        return config;
    }

}
