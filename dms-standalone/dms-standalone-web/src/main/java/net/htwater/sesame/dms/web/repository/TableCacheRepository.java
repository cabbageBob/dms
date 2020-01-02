package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.domain.TableCache;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Jokki
 */
@Repository
public interface TableCacheRepository extends MongoRepository<TableCache, String>, TableCacheRepositoryCustom {

    @Query("{datasourceId: ?0}")
    List<TableCache> findAllByDatasourceId(String datasourceId);

    @Query("{_id: {$in: ?0}}")
    List<TableCache> findAllByIds(List<String> ids);

    @Query("{$or: [{name: {$regex: ?0, $options: \"i\"}}, {comment: {$regex: ?0, $options: \"i\"}}]}")
    List<TableCache> findAllByRegex(String regex);

    @DeleteQuery("{datasourceId: ?0}")
    void deleteAllByDatasourceId(String datasourceId);

    @Query(value = "{\"columns\": {$elemMatch: {$or: [{\"name\": {$regex: ?0, $options: \"i\"}}, " +
            "{\"comment\": {$regex: ?0, $options: \"i\"}}]}}, \"datasourceId\": {$in: ?1}},",
            fields = "{\"datasourceId\": 1, \"name\": 1, \"comment\": 1, \"columns.$\": 1}")
    List<TableCache> findAllFieldsByRegex(String regex, List<String> datasourceIds);

}
