package net.htwater.sesame.dms.web.entity;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author Jokki
 */
@Getter
@Setter
public class ReportTableQuery {

    @NotNull
    private String datasourceId;

    @NotNull
    private String table;

    @NotNull
    private ReportType.TimeNumber timeNumber;
}
