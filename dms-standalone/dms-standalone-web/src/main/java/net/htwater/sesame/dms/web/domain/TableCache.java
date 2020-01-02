package net.htwater.sesame.dms.web.domain;

import core.meta.table.ColumnMetadata;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author Jokki
 */
@Data
@Document("table_cache")
@CompoundIndexes({
        @CompoundIndex(name = "datasourceId", def = "{datasourceId:1}")
})
public class TableCache {

    @Id
    private String id;

    private String datasourceId;

    private String name;

    private String comment;

    private List<ColumnMetadata> columns;

}
