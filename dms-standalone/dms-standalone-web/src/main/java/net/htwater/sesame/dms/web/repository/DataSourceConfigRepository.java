package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Jokki
 */
@Repository
public interface DataSourceConfigRepository extends MongoRepository<DataSourceConfig, String> {
}
