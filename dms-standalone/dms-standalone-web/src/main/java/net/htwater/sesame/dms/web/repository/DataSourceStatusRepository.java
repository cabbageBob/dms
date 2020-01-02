package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.domain.DataSourceStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Jokki
 */
@Repository
public interface DataSourceStatusRepository extends MongoRepository<DataSourceStatus, String> {
}
