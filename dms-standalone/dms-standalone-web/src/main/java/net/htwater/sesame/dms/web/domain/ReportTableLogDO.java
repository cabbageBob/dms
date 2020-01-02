package net.htwater.sesame.dms.web.domain;

import lombok.Data;
import net.htwater.sesame.dms.web.entity.ReportType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Jokki
 */
@Document("table_report_log")
@Data
public class ReportTableLogDO {

    @Id
    private ReportTableLogPk id;

    /**
     * 填报时间
     */
    private String reportTime;

    @Data
    public static class ReportTableLogPk {

        private String datasourceId;

        private String table;

        private ReportType reportType;

        /**
         * 截止时间
         */
        private String endTime;

    }
}
