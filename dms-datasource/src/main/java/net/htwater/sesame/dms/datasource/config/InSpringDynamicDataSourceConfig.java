package net.htwater.sesame.dms.datasource.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * spring容器里的数据源配置
 * @author Jokki
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InSpringDynamicDataSourceConfig extends DynamicDataSourceConfig {
    private String beanName;
}
