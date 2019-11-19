package net.htwater.sesame.dms.service.config;

import lombok.Getter;
import lombok.Setter;
import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfig;

import java.util.Map;

@Getter
@Setter
public class InDBDynamicDataSourceConfig extends DynamicDataSourceConfig {

    private static final long serialVersionUID = -1194234628206484357L;

    private Map<String,Object> properties;

    @Override
    public boolean equals(Object o) {
        if (o instanceof InDBDynamicDataSourceConfig) {
            return o.hashCode() == hashCode();
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return properties == null ? 0 : properties.hashCode();
    }

}
