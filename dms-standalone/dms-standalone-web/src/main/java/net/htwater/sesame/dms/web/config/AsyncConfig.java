package net.htwater.sesame.dms.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Jokki
 */
@Configuration
@ConfigurationProperties(prefix = "async")
@EnableAsync(proxyTargetClass = true)
public class AsyncConfig {

    private Integer corePoolSize;

    private Integer maxPoolSize;

    private Integer queueCapacity;

    private Integer keepAliveSeconds;

    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public void setKeepAliveSeconds(Integer keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void setQueueCapacity(Integer queueCapacity) {
        this.queueCapacity = queueCapacity;
    }


    /**
     * 异步执行线程池
     * @return Executor
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor sentinelSimpleAsync(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        if (corePoolSize != null) {
            executor.setCorePoolSize(corePoolSize);
        }
        if (maxPoolSize != null) {
            executor.setMaxPoolSize(maxPoolSize);
        }
        if (queueCapacity != null) {
            executor.setQueueCapacity(queueCapacity);
        }
        if (keepAliveSeconds != null) {
            executor.setKeepAliveSeconds(keepAliveSeconds);
        }
        executor.setThreadNamePrefix("MyAsyncExecutor-");
        executor.initialize();
        return executor;
    }
}
