package net.htwater.sesame.dms.web.controller;

import net.htwater.sesame.dms.web.entity.HistoryAnalytics;
import net.htwater.sesame.dms.web.entity.SummaryAnalytics;
import net.htwater.sesame.dms.web.service.DataSourceMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jokki
 */
@RestController
@RequestMapping("/database/search")
public class SearchController {

    private final DataSourceMetricsService dataSourceMetricsService;

    @Autowired
    public SearchController(DataSourceMetricsService dataSourceMetricsService) {
        this.dataSourceMetricsService = dataSourceMetricsService;
    }

    @GetMapping("summary")
    public SummaryAnalytics summary() {
        return dataSourceMetricsService.summaryAnalytics();
    }

    @GetMapping("history")
    public HistoryAnalytics history(@RequestParam("from") Long from, @RequestParam("to") Long to,
                                    @RequestParam("datasource") String datasource) {
        return dataSourceMetricsService.dateHistogramQuery(from, to, datasource);
    }
}
