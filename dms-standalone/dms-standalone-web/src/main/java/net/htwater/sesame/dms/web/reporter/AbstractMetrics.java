package net.htwater.sesame.dms.web.reporter;

import java.time.Instant;

/**
 * @author Jokki
 */
public abstract class AbstractMetrics implements Metrics {

    private final long timestamp;

    protected AbstractMetrics(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Instant timestamp() {
        return Instant.ofEpochMilli(this.timestamp);
    }
}
