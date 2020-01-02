package net.htwater.sesame.dms.web.entity;

import lombok.AllArgsConstructor;
import net.htwater.sesame.dms.web.repository.DataImportExportRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@AllArgsConstructor
public class AsyncRecord implements Runnable {

    private Integer records;
    private RecordType recordType;
    private DataImportExportRepository dataImportExportRepository;
    @Override
    public void run() {
        long now =LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        RecordEntity recordEntity = new RecordEntity(null,records,now,recordType);
        dataImportExportRepository.insert(recordEntity);
    }
}
