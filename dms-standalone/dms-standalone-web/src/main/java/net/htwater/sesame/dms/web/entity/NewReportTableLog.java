package net.htwater.sesame.dms.web.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Jokki
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewReportTableLog {

    private String datasourceId;

    private String table;

    /**
     * 截止时间
     */
    private String endTime;
}
