package net.htwater.sesame.dms.web.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "record")
public class RecordEntity {
    @Id
    String id;
    int records;
    long time;
    RecordType type;
}
