package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.entity.RecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DataImportExportRepository extends MongoRepository<RecordEntity,String> {
    @Query
    List<RecordEntity> findRecordEntityByTimeBetween(long begin,long end);
}
