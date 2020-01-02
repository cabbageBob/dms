package net.htwater.sesame.dms.web.service;

import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.dataSource.DynamicDataSource;
import net.htwater.sesame.dms.web.entity.InDBDynamicDataSourceConfig;

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
    private InDBDynamicDataSourceConfig config;

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
                           InDBDynamicDataSourceConfig config, boolean closed) {
        this.hash = hash;
        this.dataSource = dataSource;
        this.initLatch = initLatch;
        this.config = config;
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }


    public void closeDataSource() {
        closed = true;
    }

    public InDBDynamicDataSourceConfig getConfig() {
        return config;
    }

}
