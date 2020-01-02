package net.htwater.sesame.dms.web.reporter;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Jokki
 */
public abstract class AbstractIndexNameGenerator implements IndexNameGenerator {

    @Autowired
    protected ReporterConfiguration configuration;

    protected final DateTimeFormatter sdf;

    public AbstractIndexNameGenerator() {
        this.sdf = DateTimeFormatter.ofPattern("yyyy.MM").withZone(ZoneId.systemDefault());
    }
}
