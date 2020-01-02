package net.htwater.sesame.dms.web.controller;

import net.htwater.sesame.dms.web.domain.ReportTableDO;
import net.htwater.sesame.dms.web.domain.ReportTableLogDO;
import net.htwater.sesame.dms.web.entity.NewReportTable;
import net.htwater.sesame.dms.web.entity.NewReportTableLog;
import net.htwater.sesame.dms.web.entity.ReportTableDTO;
import net.htwater.sesame.dms.web.entity.ReportTableQuery;
import net.htwater.sesame.dms.web.service.ReportTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Jokki
 */
@RestController
@RequestMapping("/reports")
public class ReportTableController {

    private final ReportTableService reportTableService;

    @Autowired
    public ReportTableController(ReportTableService reportTableService) {
        this.reportTableService = reportTableService;
    }

    @GetMapping()
    public List<ReportTableDTO> listReportTables() {
        return reportTableService.findAllReportTables();
    }

    @GetMapping("{datasourceId}/{table}")
    public ReportTableDO findReportTableById(@PathVariable("datasourceId") String datasourceId,
                                             @PathVariable("table") String table) {
        return reportTableService.findById(datasourceId, table);
    }

    @PostMapping("report")
    public ReportTableDTO findReportTableByQuery(@RequestBody ReportTableQuery reportTableQuery) {
        return reportTableService.findReportTableByQuery(reportTableQuery);
    }

    @PostMapping("report/save")
    public ReportTableDO saveReportTable(@RequestBody NewReportTable newReportTableQuery) {
        return reportTableService.saveReportTable(newReportTableQuery);
    }

    @PostMapping("report/savelog")
    public ReportTableLogDO saveReportTableLog(@RequestBody NewReportTableLog newReportTableLog) {
        return reportTableService.saveReportTableLog(newReportTableLog);
    }
}
