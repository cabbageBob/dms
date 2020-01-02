package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.domain.ReportTableLogDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Jokki
 */
@Repository
public interface ReportTableLogRepository extends MongoRepository<ReportTableLogDO, ReportTableLogDO.ReportTableLogPk> {

    @Query("{'id.datasourceId': ?0, 'id.table': ?1, 'id.reportType': ?2, 'id.endTime': ?3}")
    Optional<ReportTableLogDO> findByDatasourceIdAndTableAndReportTypeAndEndTime(String datasourceId,
                                                                                 String table,
                                                                                 String reportType,
                                                                                 String endTime);
}
