package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.domain.CustomColumnClassDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author Jokki
 */
@Repository
public interface CustomColumnClassRepository extends MongoRepository<CustomColumnClassDO, String> {

    @Query("{datasourceId: ?0, table: ?1}")
    Set<CustomColumnClassDO> findByDatasourceIdAndAndTable(String datasourceId, String table);
}
