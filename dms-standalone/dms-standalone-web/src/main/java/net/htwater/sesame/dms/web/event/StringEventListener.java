package net.htwater.sesame.dms.web.event;

import com.google.common.eventbus.Subscribe;
import net.htwater.sesame.dms.web.dataSource.DynamicDataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Jokki
 */
@Component
public class StringEventListener {

    private final DynamicDataSourceService dynamicDataSourceService;

    @Autowired
    public StringEventListener(DynamicDataSourceService dynamicDataSourceService) {
        this.dynamicDataSourceService = dynamicDataSourceService;
    }

    @Subscribe
    public void removeDatasourceCache(String datasourceId) {
        dynamicDataSourceService.removeCache(datasourceId);
    }
}
