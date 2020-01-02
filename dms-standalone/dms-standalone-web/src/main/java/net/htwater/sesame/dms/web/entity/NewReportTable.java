package net.htwater.sesame.dms.web.entity;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author Jokki
 */
@Getter
@Setter
public class NewReportTable {

    private ReportType reportType;

    /**
     * 表名
     */
    @NotNull
    private String table;

    /**
     * 数据源id
     */
    @NotNull
    private String datasourceId;

    /**
     * 截止日期的字段
     */
    private String endTimeField;
}
