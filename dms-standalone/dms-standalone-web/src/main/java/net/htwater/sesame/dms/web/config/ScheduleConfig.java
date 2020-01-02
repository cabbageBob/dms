package net.htwater.sesame.dms.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author jokki
 */
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {


    /**
     * 配置定时任务池
     * @param scheduledTaskRegistrar schedule registrar
     */
    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar scheduledTaskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setDaemon(true);
        scheduler.setThreadNamePrefix("MyTaskScheduler-");
        scheduler.initialize();
        scheduledTaskRegistrar.setTaskScheduler(scheduler);
    }
}
