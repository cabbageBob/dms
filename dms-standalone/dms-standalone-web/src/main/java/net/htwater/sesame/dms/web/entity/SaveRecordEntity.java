package net.htwater.sesame.dms.web.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveRecordEntity {
    private Integer records;
    private RecordType recordType;
}
