package net.htwater.sesame.dms.web.entity;

import lombok.Getter;
import lombok.Setter;
import net.htwater.sesame.dms.web.reporter.DataSourceMetrics;

import java.util.List;

/**
 * @author Jokki
 */
@Getter
@Setter
public class HistoryAnalytics {

    private String datasource;

    List<Bucket> values;

    @Setter
    @Getter
    public static class Bucket {
        private Long timestamp;

        private DataSourceMetrics value;
    }
}
