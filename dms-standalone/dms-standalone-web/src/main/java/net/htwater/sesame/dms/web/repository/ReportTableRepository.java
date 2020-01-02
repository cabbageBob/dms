package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.domain.ReportTableDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Jokki
 */
@Repository
public interface ReportTableRepository extends MongoRepository<ReportTableDO, String> {
}
