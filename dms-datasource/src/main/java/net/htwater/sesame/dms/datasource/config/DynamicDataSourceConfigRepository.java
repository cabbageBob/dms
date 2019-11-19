package net.htwater.sesame.dms.datasource.config;

import java.util.List;

/**
 * 动态数据源配置存储
 * @author Jokki
 */
public interface DynamicDataSourceConfigRepository<C extends DynamicDataSourceConfig> {
    List<C> findAll();

    C findById(String dataSourceId);

    C add(C config);

    C remove(String dataSourceId);
}
