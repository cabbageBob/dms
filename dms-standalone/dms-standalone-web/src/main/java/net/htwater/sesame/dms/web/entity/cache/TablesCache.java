package net.htwater.sesame.dms.web.entity.cache;

import core.meta.ObjectType;
import core.meta.table.ColumnMetadata;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 所有数据源下的所有数据库的缓存
 */
@Document(collection = "tables_cache")
@CompoundIndexes({
        @CompoundIndex(name = "uq_datasourceId_name", def = "{datasourceId:1, name: 1}", unique = true)
})
@Data
public class TablesCache {
    @Id
    private String id;

    private String datasourceId;

    private String name;

    protected ObjectType type;

    private List<ColumnMetadata> result;

}
