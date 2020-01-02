package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.domain.TableCache;
import net.htwater.sesame.dms.web.entity.TableColumnCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Jokki
 */
@Repository
public class TableCacheRepositoryImpl implements TableCacheRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public TableCacheRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<TableColumnCount> tableColumnCount() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("datasourceId")
                        .sum(ArrayOperators.Size.lengthOfArray("columns")).as("columnsNum")
                        .count().as("tablesNum")
        );
        AggregationResults<TableColumnCount> results =
                mongoTemplate.aggregate(aggregation, TableCache.class, TableColumnCount.class);
        return results.getMappedResults();
    }
}
