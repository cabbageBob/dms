package net.htwater.sesame.dms.web.domain;

import lombok.Data;
import net.htwater.sesame.dms.web.entity.ReportType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Jokki
 */
@Document("table_report")
@Data
@CompoundIndexes({
        @CompoundIndex(name = "datasourceId_table", def = "{datasourceId:1, table:1}"),
})
public class ReportTableDO {

    @Id
    private String id;

    /**
     * 报表类型
     * @see ReportType
     */
    private ReportType reportType;

    /**
     * 表名
     */
    private String table;

    /**
     * 数据源id
     */
    private String datasourceId;

    /**
     * 截止日期的字段
     */
    private String endTimeField;
}
