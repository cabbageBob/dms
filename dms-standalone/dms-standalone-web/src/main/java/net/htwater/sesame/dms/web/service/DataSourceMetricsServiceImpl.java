package net.htwater.sesame.dms.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import net.htwater.sesame.dms.web.entity.HistoryAnalytics;
import net.htwater.sesame.dms.web.entity.SummaryAnalytics;
import net.htwater.sesame.dms.web.reporter.*;
import net.htwater.sesame.dms.web.repository.TableCacheRepository;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.ParsedTopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jokki
 */
@Service
@Slf4j
public class DataSourceMetricsServiceImpl implements DataSourceMetricsService {
    private final RestHighLevelClient client;

    private final ObjectMapper objectMapper;

    private final ReporterConfiguration reporterConfiguration;

    private final IndexNameGenerator indexNameGenerator;

    private final DateTimeFormatter sdf;

    private final DateTimeFormatter dtf;

    private final DataSourceConfigService dataSourceConfigService;

    private final TableCacheRepository tableCacheRepository;


    @Autowired
    public DataSourceMetricsServiceImpl(RestHighLevelClient client, ObjectMapper objectMapper,
                                        ReporterConfiguration reporterConfiguration,
                                        IndexNameGenerator indexNameGenerator,
                                        DataSourceConfigService dataSourceConfigService,
                                        TableCacheRepository tableCacheRepository) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.reporterConfiguration = reporterConfiguration;
        this.indexNameGenerator = indexNameGenerator;
        this.dataSourceConfigService = dataSourceConfigService;
        this.tableCacheRepository = tableCacheRepository;
        this.sdf = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM").withZone(ZoneId.systemDefault());
        this.dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[XXX]").withZone(ZoneId.systemDefault());
    }

    /** 初始化index template
     * @throws IOException
     */
    @PostConstruct
    public void init() throws IOException {
        PutIndexTemplateRequest request = new PutIndexTemplateRequest(
                reporterConfiguration.getIndexName() + "-monitor");
        request.patterns(Collections.singletonList("dms-monitor-*"));
        request.settings(Settings
                .builder()
                .put("index.number_of_shards", reporterConfiguration.getNumberOfShards())
                .put("index.number_of_replicas", reporterConfiguration.getNumberOfReplicas()));
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject(Fields.DATASOURCE);
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject(Fields.SIZE);
                {
                    builder.field("type", "float");
                }
                builder.endObject();
                builder.startObject(Fields.COUNT);
                {
                    builder.field("type", "long");
                }
                builder.endObject();
                builder.startObject(Fields.CONNS);
                {
                    builder.field("type", "integer");
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        request.mapping(MetricType.MONITOR.getType(), builder);
        AcknowledgedResponse putTemplateResponse = client.indices().putTemplate(request, RequestOptions.DEFAULT);
        if (putTemplateResponse.isAcknowledged()) {
            log.info("put index template success");
        }
    }

    @Override
    public void bulkAsync(Metrics metrics) {
        BulkRequest request = new BulkRequest();
        Map<String, Object> source = Maps.newHashMap();
        if (metrics instanceof DataSourceMetrics) {
            source = getSource((DataSourceMetrics) metrics);
        }
        request.add(new IndexRequest(indexNameGenerator.generate(metrics), MetricType.getType(metrics))
                .source(source));
        request.timeout(TimeValue.timeValueSeconds(30));
        client.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                log.debug("bulk success.");
            }

            @Override
            public void onFailure(Exception e) {
                log.error("bulk failed: ", e);
            }
        });
    }

    private Map<String, Object> getSource(DataSourceMetrics metrics) {
        Map<String, Object> jsonMap = Maps.newHashMap();
        jsonMap.put(Fields.DATASOURCE, metrics.getDatasource());
        jsonMap.put(Fields.COUNT, metrics.getCount());
        jsonMap.put(Fields.SIZE, metrics.getSize());
        jsonMap.put(Fields.CONNS, metrics.getConns());
        jsonMap.put(Fields.SPECIAL_TIMESTAMP, dtf.format(metrics.timestamp()));
        return jsonMap;
    }

    @Override
    public HistoryAnalytics dateHistogramQuery(long from, long to, String datasource) {
        HistoryAnalytics historyAnalytics = new HistoryAnalytics();
        historyAnalytics.setDatasource(datasource);
        List<String> indices = indexNameGenerator.getIndexName(MetricType.MONITOR, from, to);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indicesOptions(IndicesOptions.fromOptions(true, true,
                true, true));
        searchRequest.indices(indices.toArray(new String[]{}));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(Fields.DATASOURCE, datasource))
                .must(QueryBuilders.rangeQuery(Fields.SPECIAL_TIMESTAMP).from(from, true)
                        .to(to, true)));
        sourceBuilder.from(0);
        sourceBuilder.size(10000);
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            List<HistoryAnalytics.Bucket> buckets = Lists.newArrayList();
            for (SearchHit hit : response.getHits().getHits()) {
                HistoryAnalytics.Bucket bucket = new HistoryAnalytics.Bucket();
                Map<String, Object> source = hit.getSourceAsMap();
                long timestamp = LocalDateTime.parse(source.get(Fields.SPECIAL_TIMESTAMP).toString(), dtf)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                bucket.setTimestamp(timestamp);
                DataSourceMetrics metrics = DataSourceMetrics.builder().datasource(datasource)
                        .size(Float.valueOf(source.getOrDefault(Fields.SIZE, "0").toString()))
                        .count(Long.valueOf(source.getOrDefault(Fields.COUNT, "0").toString()))
                        .conns(Integer.valueOf(source.getOrDefault(Fields.CONNS,"0").toString()))
                        .build();
                bucket.setValue(metrics);
                buckets.add(bucket);
            }
            historyAnalytics.setValues(buckets);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return historyAnalytics;
    }

    @Override
    public SummaryAnalytics summaryAnalytics() {
        SummaryAnalytics summaryAnalytics = new SummaryAnalytics();
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexNameGenerator.getTodayIndexName(MetricType.MONITOR));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders
                .terms("by_datasource")
                .field(Fields.DATASOURCE)
                .size(Integer.MAX_VALUE);
        aggregationBuilder.subAggregation(AggregationBuilders
                .topHits("top_time_hits")
                .size(1)
                .sort(Fields.SPECIAL_TIMESTAMP, SortOrder.DESC)
                .fetchSource(new String[]{Fields.SPECIAL_TIMESTAMP, Fields.SIZE, Fields.COUNT, Fields.CONNS}, null));
        sourceBuilder.aggregation(aggregationBuilder);
        searchRequest.source(sourceBuilder);
        try {
            List<DataSourceConfig> dataSourceConfigs = dataSourceConfigService.findAll();
            Map<String, SummaryAnalytics.AnalyticDetail> detailMap = dataSourceConfigs
                    .stream()
                    .collect(Collectors.toMap(DataSourceConfig::getId, dataSourceConfig -> {
                        SummaryAnalytics.AnalyticDetail detail = new SummaryAnalytics.AnalyticDetail();
                        detail.setId(dataSourceConfig.getId());
                        detail.setPort(dataSourceConfig.getPort());
                        detail.setDbName(dataSourceConfig.getDbname());
                        detail.setIp(dataSourceConfig.getIp());
                        detail.setName(dataSourceConfig.getName());
                        detail.setDatabaseGenre(dataSourceConfig.getDatabaseGenre());
                        return detail;
                    }));
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            Terms terms = response.getAggregations().get("by_datasource");
            for (Terms.Bucket bucket : terms.getBuckets()) {
                SummaryAnalytics.AnalyticDetail detail = detailMap.get(bucket.getKey().toString());
                if (detail != null) {
                    SearchHit hit = ((ParsedTopHits)bucket.getAggregations().get("top_time_hits")).getHits().getHits()[0];
                    JsonNode source = objectMapper.readTree(hit.toString()).path("_source");
                    long count = source.path(Fields.COUNT).asLong(0);
                    double size =  source.path(Fields.SIZE).asDouble(0);
                    int conns = source.path(Fields.CONNS).asInt(0);
                    summaryAnalytics.setAllCount(summaryAnalytics.getAllCount() +
                            count);
                    summaryAnalytics.setAllSize(summaryAnalytics.getAllSize() +
                           size);
                    summaryAnalytics.setAllConns(summaryAnalytics.getAllConns() +
                           conns);
                    detail.setCount(count);
                    detail.setSize(size);
                    detail.setConns(conns);
                }
            }

            //数据库,表,字段数
            tableCacheRepository.tableColumnCount().forEach(tableColumnCount -> {
                        detailMap.computeIfPresent(tableColumnCount.getId(), (k, v) -> {
                            v.setFieldNum(tableColumnCount.getColumnsNum());
                            v.setTableNum(tableColumnCount.getTablesNum());
                            summaryAnalytics.setAllFieldNum(summaryAnalytics.getAllFieldNum() +
                                     tableColumnCount.getColumnsNum());
                            summaryAnalytics.setAllTableNum(summaryAnalytics.getAllTableNum() +
                                     tableColumnCount.getTablesNum());
                            return v;
                        });
                    });
            summaryAnalytics.setDatasourceNum(detailMap.size());
            summaryAnalytics.setDetails(detailMap.entrySet()
                    .stream()
                    .map(Map.Entry::getValue)
                    .sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()))
                    .collect(Collectors.toList())
            );
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return summaryAnalytics;
    }

    static final class Fields {
        private Fields() {
        }
        static final String SPECIAL_TIMESTAMP = "@timestamp";
        static final String DATASOURCE = "datasource";
        static final String SIZE = "size";
        static final String COUNT = "count";
        static final String CONNS = "conns";
    }

}
