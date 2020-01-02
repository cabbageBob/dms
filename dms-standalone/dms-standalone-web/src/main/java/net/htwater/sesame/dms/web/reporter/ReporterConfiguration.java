package net.htwater.sesame.dms.web.reporter;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Jokki
 */
@Component
@Getter
public final class ReporterConfiguration {

    @Value("${spring.elasticsearch.index.name:dms}")
    private String indexName;

    @Value("${spring.elasticsearch.index.shards:5}")
    private int numberOfShards;

    @Value("${spring.elasticsearch.index.replicas:1}")
    private int numberOfReplicas;
}
