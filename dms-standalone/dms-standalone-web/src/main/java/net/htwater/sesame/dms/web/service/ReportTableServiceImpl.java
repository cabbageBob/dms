package net.htwater.sesame.dms.web.service;

import com.google.common.collect.Lists;
import core.dialect.Dialect;
import core.meta.ObjectType;
import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import net.htwater.sesame.dms.web.dataSource.DefaultJdbcExecutor;
import net.htwater.sesame.dms.web.domain.ReportTableDO;
import net.htwater.sesame.dms.web.domain.ReportTableLogDO;
import net.htwater.sesame.dms.web.domain.TableCache;
import net.htwater.sesame.dms.web.entity.NewReportTable;
import net.htwater.sesame.dms.web.entity.NewReportTableLog;
import net.htwater.sesame.dms.web.entity.ReportTableDTO;
import net.htwater.sesame.dms.web.entity.ReportTableQuery;
import net.htwater.sesame.dms.web.exception.TableNotFoundException;
import net.htwater.sesame.dms.web.repository.ReportTableLogRepository;
import net.htwater.sesame.dms.web.repository.ReportTableRepository;
import net.htwater.sesame.dms.web.util.DataManagerUtil;
import org.hswebframework.ezorm.rdb.executor.SQL;
import org.hswebframework.ezorm.rdb.render.support.simple.SimpleSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jokki
 */
@Service
@Slf4j
public class ReportTableServiceImpl implements ReportTableService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ReportTableRepository reportTableRepository;

    private final ReportTableLogRepository reportTableLogRepository;

    private final DefaultJdbcExecutor sqlExecutor;

    private final SimpleDatabaseManagerService simpleDatabaseManagerService;

    private final TableCacheService tableCacheService;

    @Autowired
    public ReportTableServiceImpl(ReportTableRepository reportTableRepository,
                                  ReportTableLogRepository reportTableLogRepository,
                                  DefaultJdbcExecutor sqlExecutor,
                                  SimpleDatabaseManagerService simpleDatabaseManagerService,
                                  TableCacheService tableCacheService) {
        this.reportTableRepository = reportTableRepository;
        this.reportTableLogRepository = reportTableLogRepository;
        this.sqlExecutor = sqlExecutor;
        this.simpleDatabaseManagerService = simpleDatabaseManagerService;
        this.tableCacheService = tableCacheService;
    }

    @Override
    public List<ReportTableDTO> findAllReportTables() {
        List<ReportTableDO> reportTableDOS = reportTableRepository.findAll();
        Map<String, ReportTableDO> idReportTableMap = reportTableDOS.stream()
                .filter(reportTableDO -> reportTableDO.getReportType() != null)
                .collect(Collectors.toMap(reportTableDO ->
                        DataManagerUtil.formatId(reportTableDO.getDatasourceId(), reportTableDO.getTable()),
                        reportTableDO -> reportTableDO));
        List<TableCache> tableCaches = tableCacheService.findAllByIds(Lists.newArrayList(idReportTableMap.keySet()));
        return tableCaches
                .stream().map(tableCache -> {
                    ReportTableDO reportTableDO = idReportTableMap.get(tableCache.getId());
                    ReportTableDTO reportTableDTO = new ReportTableDTO();
                    reportTableDTO.setComment(tableCache.getComment());
                    reportTableDTO.setDatasourceId(reportTableDO.getDatasourceId());
                    reportTableDTO.setReportType(reportTableDO.getReportType());
                    reportTableDTO.setEndTimeField(reportTableDO.getEndTimeField());
                    reportTableDTO.setTable(reportTableDO.getTable());
                    return reportTableDTO;
                }).collect(Collectors.toList());
    }

    @Override
    public ReportTableDO findById(String datasourceId, String table) {
        return reportTableRepository.findById(DataManagerUtil.formatId(datasourceId, table))
                .orElse(new ReportTableDO());
    }

    @Override
    public ReportTableDTO findReportTableByQuery(ReportTableQuery reportTableQuery) {
        String id = DataManagerUtil.formatId(reportTableQuery.getDatasourceId(), reportTableQuery.getTable());
        Optional<ReportTableDO> tableDOOptional = reportTableRepository.findById(id);
        if (tableDOOptional.isPresent()) {
            ReportTableDTO reportTableDTO = new ReportTableDTO();
            ReportTableDO reportTableDO = tableDOOptional.get();
            //截止时间
            String endTime = reportTableDO.getReportType().endTime(reportTableQuery.getTimeNumber());
            //先查询数据库是否存在记录,即上报状态
            String endTimeField = reportTableDO.getEndTimeField();
            reportTableDTO.setTable(reportTableDO.getTable());
            reportTableDTO.setEndTimeField(endTimeField);
            reportTableDTO.setDatasourceId(reportTableDO.getDatasourceId());
            reportTableDTO.setReportType(reportTableDO.getReportType());
            reportTableDTO.setEndTime(endTime);
            DataSourceHolder.switcher().use(reportTableQuery.getDatasourceId());
            Dialect dialect = simpleDatabaseManagerService.getParserRepo().get(DataSourceHolder.currentDatabaseType())
                    .get(ObjectType.TABLE);
            String sb = "" + "select * from " +
                    dialect.openQuote() +
                    reportTableQuery.getTable() +
                    dialect.closeQuote() +
                    " where " +
                    dialect.openQuote() +
                    endTimeField +
                    dialect.closeQuote() +
                    "=#{endTime}";
            Map<String, Object> params = Collections.singletonMap("endTime",
                    Date.from(LocalDate.parse(endTime, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            SQL sql = new SimpleSQL(sb, params);
            List records = null;
            try {
                records = sqlExecutor.list(sql);
            } catch (SQLException e) {
                logger.warn(e.getMessage(), e);
            }
            if (!CollectionUtils.isEmpty(records)) {
                reportTableDTO.setStatus(1);
                reportTableLogRepository.findByDatasourceIdAndTableAndReportTypeAndEndTime(
                        reportTableQuery.getDatasourceId(),
                        reportTableQuery.getTable(),
                        reportTableDO.getReportType().name(), endTime)
                        .ifPresent(reportTableLogDO ->
                                reportTableDTO.setReportTime(reportTableLogDO.getReportTime()));
            } else {
                reportTableDTO.setStatus(0);
            }
            return reportTableDTO;
        }
        throw new TableNotFoundException(reportTableQuery.getDatasourceId(), reportTableQuery.getTable());
    }

    @Override
    public ReportTableDO saveReportTable(NewReportTable newReportTableQuery) {
        String id = DataManagerUtil.formatId(newReportTableQuery.getDatasourceId(), newReportTableQuery.getTable());
        ReportTableDO reportTableDO = new ReportTableDO();
        reportTableDO.setDatasourceId(newReportTableQuery.getDatasourceId());
        reportTableDO.setId(id);
        reportTableDO.setEndTimeField(newReportTableQuery.getEndTimeField());
        reportTableDO.setReportType(newReportTableQuery.getReportType());
        reportTableDO.setTable(newReportTableQuery.getTable());
        if (newReportTableQuery.getReportType() == null) {
            reportTableRepository.findById(id)
                    .ifPresent(existReportTable ->
                        reportTableRepository.deleteById(id)
                    );
            return new ReportTableDO();
        } else {
            return reportTableRepository.save(reportTableDO);
        }
    }

    @Override
    public ReportTableLogDO saveReportTableLog(NewReportTableLog newReportTableLog) {
        String id = DataManagerUtil.formatId(newReportTableLog.getDatasourceId(), newReportTableLog.getTable());
        Optional<ReportTableDO> tableDOOptional = reportTableRepository.findById(id);
        if (tableDOOptional.isPresent()) {
            ReportTableDO reportTableDO = tableDOOptional.get();
            ReportTableLogDO reportTableLogDO = new ReportTableLogDO();
            ReportTableLogDO.ReportTableLogPk pk = new ReportTableLogDO.ReportTableLogPk();
            pk.setDatasourceId(newReportTableLog.getDatasourceId());
            pk.setEndTime(newReportTableLog.getEndTime());
            pk.setReportType(reportTableDO.getReportType());
            pk.setTable(newReportTableLog.getTable());
            reportTableLogDO.setId(pk);
            reportTableLogDO.setReportTime(formatter.format(LocalDate.now()));
            return reportTableLogRepository.save(reportTableLogDO);
        }
        throw new TableNotFoundException(newReportTableLog.getDatasourceId(), newReportTableLog.getTable());
    }

}
