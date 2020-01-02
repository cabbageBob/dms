package net.htwater.sesame.dms.web.event;

import com.google.common.eventbus.Subscribe;
import net.htwater.sesame.dms.web.entity.RecordEntity;
import net.htwater.sesame.dms.web.entity.SaveRecordEntity;
import net.htwater.sesame.dms.web.repository.DataImportExportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
@Component
public class RecordEventListener {
    private DataImportExportRepository dataImportExportRepository;
    @Autowired
    public RecordEventListener(DataImportExportRepository dataImportExportRepository){
        this.dataImportExportRepository = dataImportExportRepository;
    }

    @Subscribe
    public void saveRecord(SaveRecordEntity entity){
        long now =LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        RecordEntity recordEntity = new RecordEntity(null,entity.getRecords(),now,entity.getRecordType());
        dataImportExportRepository.insert(recordEntity);
    }
}
