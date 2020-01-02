package net.htwater.sesame.dms.web.reporter;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jokki
 */
@Component
public class TypeIndexNameGenerator extends AbstractIndexNameGenerator {
    private String indexNameTemplate;

    @PostConstruct
    public void initialize() {
        indexNameTemplate = configuration.getIndexName() + "-%s-%s";
    }

    @Override
    public String generate(Metrics metrics) {
        String type = MetricType.getType(metrics);
        return String.format(indexNameTemplate, type, sdf.format(metrics.timestamp()));
    }

    @Override
    public List<String> getIndexName(MetricType type, long from, long to) {
        return rangedIndices(from, to)
                .stream()
                .map(s -> String.format(indexNameTemplate, type.getType(), s))
                .collect(Collectors.toList());
    }

    @Override
    public String getTodayIndexName(MetricType type) {
        return String.format(indexNameTemplate, type.getType(), sdf.format(Instant.now()));
    }

    private List<String> rangedIndices(final long from, final long to) {
        final Set<String> indices = new HashSet<>(4);

        LocalDate start = new Date(from).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate stop = new Date(to).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        while(start.isBefore(stop) || start.isEqual(stop)) {
            indices.add(sdf.format(start));
            start = start.plus(1, ChronoUnit.MONTHS);
        }
        indices.add(sdf.format(stop));
        return new ArrayList<>(indices);
    }
}
