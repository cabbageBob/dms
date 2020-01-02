package net.htwater.sesame.dms.web.service;

import net.htwater.sesame.dms.web.entity.HistoryAnalytics;
import net.htwater.sesame.dms.web.entity.SummaryAnalytics;
import net.htwater.sesame.dms.web.reporter.Metrics;

/**
 * @author Jokki
 */
public interface DataSourceMetricsService {
    /**
     * 异步bulk
     * @param metrics
     */
    void bulkAsync(Metrics metrics);

    HistoryAnalytics dateHistogramQuery(long from, long to, String datasource);

    SummaryAnalytics summaryAnalytics();
}
