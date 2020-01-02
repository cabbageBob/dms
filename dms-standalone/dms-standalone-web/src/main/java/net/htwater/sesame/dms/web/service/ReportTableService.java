package net.htwater.sesame.dms.web.service;

import net.htwater.sesame.dms.web.domain.ReportTableDO;
import net.htwater.sesame.dms.web.domain.ReportTableLogDO;
import net.htwater.sesame.dms.web.entity.NewReportTable;
import net.htwater.sesame.dms.web.entity.NewReportTableLog;
import net.htwater.sesame.dms.web.entity.ReportTableDTO;
import net.htwater.sesame.dms.web.entity.ReportTableQuery;

import java.util.List;

/**
 * @author Jokki
 */
public interface ReportTableService {


    /**
     * @return ReportTable 所有需要上报的表格
     */
    List<ReportTableDTO> findAllReportTables();

    ReportTableDO findById(String datasourceId, String table);

    ReportTableDTO findReportTableByQuery(ReportTableQuery reportTableQuery);

    ReportTableDO saveReportTable(NewReportTable newReportTableQuery);

    ReportTableLogDO saveReportTableLog(NewReportTableLog newReportTableLog);
}
