package net.htwater.sesame.dms.web.reporter;

/**
 * @author Jokki
 */
public enum MetricType {
    /**
     * 数据源监控
     */
    MONITOR("monitor");

    private String type;

    MetricType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static String getType(Metrics metrics) {
        String type = null;
        if (metrics instanceof DataSourceMetrics) {
            type = MetricType.MONITOR.getType();
        }
        return type;
    }
}
