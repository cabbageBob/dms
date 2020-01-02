package net.htwater.sesame.dms.web.reporter;

import java.util.List;

/**
 * @author Jokki
 */
public interface IndexNameGenerator {

    /**
     * 创建带时间戳的IndexName
     * @param metrics 监控指标
     * @return indexName
     */
    String generate(Metrics metrics);

    List<String> getIndexName(MetricType type, long from, long to);

    String getTodayIndexName(MetricType type);
}
