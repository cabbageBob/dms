package net.htwater.sesame.dms.web.domain;

import lombok.Data;
import net.htwater.sesame.dms.web.entity.ColumnClass;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author Jokki
 */
@Data
@Document("custom_column_class")
@CompoundIndexes({
        @CompoundIndex(name = "datasourceId_table", def = "{datasourceId:1, table:1}"),
})
public class CustomColumnClassDO {

    @Id
    private String id;

    private String datasourceId;

    private String table;

    private String column;

    private ColumnClass columnClass;

    private LinkedHashSet<Map<Object, String>> values;
}
