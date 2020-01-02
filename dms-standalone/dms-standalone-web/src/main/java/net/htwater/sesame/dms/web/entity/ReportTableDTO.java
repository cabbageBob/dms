package net.htwater.sesame.dms.web.entity;

import lombok.Data;

/**
 * @author Jokki
 */
@Data
public class ReportTableDTO {

    private String datasourceId;

    private String table;

    private String comment;

    private String reportTime;

    private String endTime;

    private ReportType reportType;

    /**
     * 完成状态(1: 已上报,0:未上报)
     */
    private Integer status;

    /**
     * 截止日期的字段
     */
    private String endTimeField;
}
