package net.htwater.sesame.dms.web.reporter;

import lombok.Getter;

/**
 * @author Jokki
 */

@Getter
public class DataSourceMetrics extends AbstractMetrics {

    private String datasource;

    private float size;

    private long count;

    private int conns;

    public DataSourceMetrics(long timestamp) {
        super(timestamp);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long timestamp;
        private String datasource;
        private float size;
        private long count;
        private int conns;
        private Builder() {
        }

        public Builder at(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder datasource(String datasource) {
            this.datasource = datasource;
            return this;
        }

        public Builder size(float size) {
            this.size = size;
            return this;
        }

        public Builder count(long count) {
            this.count = count;
            return this;
        }

        public Builder conns(int conns){
            this.conns=conns;
            return this;
        }

        public DataSourceMetrics build() {
            DataSourceMetrics dataSourceMetrics = new DataSourceMetrics(this.timestamp);
            dataSourceMetrics.datasource = this.datasource;
            dataSourceMetrics.count = this.count;
            dataSourceMetrics.size = this.size;
            dataSourceMetrics.conns = this.conns;
            return dataSourceMetrics;
        }
    }
}
