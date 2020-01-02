package net.htwater.sesame.dms.web.controller;

import core.meta.ObjectType;
import core.sql.SqlExecuteRequest;
import core.sql.SqlExecuteResult;
import core.sql.SqlInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import net.htwater.sesame.dms.web.entity.*;
import net.htwater.sesame.dms.web.entity.altertable.AlterTable;
import net.htwater.sesame.dms.web.service.ReportTableService;
import net.htwater.sesame.dms.web.service.SimpleDatabaseManagerService;
import net.htwater.sesame.dms.web.service.TableCacheService;
import net.htwater.sesame.dms.web.util.Sqls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/database/manager")
@Api(tags = "数据库维护", value = "数据库维护")
public class DataBaseManagerController {

    @Autowired
    private SimpleDatabaseManagerService databaseManagerService;

    @Autowired
    private TableCacheService tableCacheService;

    @Autowired
    private ReportTableService reportTableService;

    @GetMapping("/metas/{datasourceId}")
    @ApiOperation("获取指定数据源的元数据")
    public Map<ObjectType, List> getMetaData(@PathVariable @ApiParam("数据源ID") String datasourceId){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.getMetas();
    }

    @PostMapping(value = "/execute/{datasourceId}")
    @ApiOperation(value = "执行SQL")
    public SqlExecuteResult execute(@PathVariable String datasourceId, SqlExecuteQuery query){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.execute(SqlExecuteRequest
                .builder()
                .sql(parseSql(query.getSqlLines(), datasourceId))
                .build(), query.getPageIndex(), query.getPageSize()).get(0);
    }


    @PostMapping(value = "/postChange/{datasourceId}")
    @ApiOperation(value = "提交修改")
    public SqlExecuteResult postChange(@PathVariable String datasourceId, SqlExecuteQuery query){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.executeSqlBatch(SqlExecuteRequest
                .builder()
                .sql(parseSql(query.getSqlLines(), datasourceId))
                .build());
    }

    @Deprecated
    @PostMapping(value = "/backup/{datasourceId}",consumes = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "数据库备份")
    public ManagerServiceResult backup(String userName, String password, String savePath, String fileName,
                                     String databaseName,@PathVariable String datasourceId){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.backup(userName,password,savePath,fileName,databaseName);
    }

    @PostMapping(value = "/comment/table/{datasourceId}")
    public BaseEntity<Boolean> updateTableComment(@RequestBody AlterComment alterComment,@PathVariable String datasourceId){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.updateTableComment(alterComment,datasourceId);
    }

    @PostMapping(value = "/comment/column/{datasourceId}")
    public BaseEntity<Boolean> updateColumnComment(@RequestBody AlterComment alterComment,@PathVariable String datasourceId){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.updateColumnComment(alterComment,datasourceId);
    }

    @PostMapping(value = "/testConnect")
    @ApiOperation(value = "测试连接")
    public ManagerServiceResult testConnect(InDBDynamicDataSourceConfig config){
        return databaseManagerService.testConnect(config);
    }

    @PostMapping(value = "/dataType/{datasourceId}")
    @ApiOperation(value = "获取数据库支持的数据类型")
    public Set<String> getDataType(@PathVariable String datasourceId){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.getColumnTypes();
    }

    @GetMapping(value = "/sqlScripts/{datasourceId}")
    @ApiOperation(value = "获取常用sql模板")
    public BaseEntity queryTplContentById(@PathVariable String datasourceId){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.queryTplContent();
    }

    @PostMapping(value = "/showScript/{datasourceId}")
    @ApiOperation(value = "返回sql模板")
    public BaseEntity showScript(@PathVariable String datasourceId, AlterTable alterTable){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.alterTableScript(alterTable);
    }

    @PostMapping(value = "/refreshcache/{datasourceId}")
    @ApiOperation(value = "刷新数据字典缓存")
    public ResponseEntity refreshCache(@PathVariable String datasourceId) {
        tableCacheService.deleteByDatasourceId(datasourceId);
        DataSourceHolder.switcher().use(datasourceId);
        databaseManagerService.getMetas();
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/search")
    @ApiOperation(value = "模糊查询表")
    public List<SearchTableEntity> searchByQ(@NotNull @RequestBody SearchQuery query){
        if (query.getQ() != null) {
            return databaseManagerService.searchByQ(query.getQ());
        } else {
            return databaseManagerService.searchByIds(query.getIds());
        }
    }

    @PostMapping(value = "/searchFields")
    @ApiOperation(value = "模糊查询字段")
    public List<SearchFieldEntity> searchFieldsByQ(@NotNull @RequestBody SearchQuery query){
        return tableCacheService.searchFieldsByQ(query.getQ());
    }

    private List<SqlInfo> parseSql(String sqlText, String datasourceId) {

        List<String> sqlList = Sqls.parse(sqlText);
        return sqlList.stream().map(sql -> {
            SqlInfo sqlInfo = new SqlInfo();
            sqlInfo.setSql(sql);
            sqlInfo.setDatasourceId(datasourceId);
            sqlInfo.setType(sql.split("[ ]")[0].toLowerCase());
            return sqlInfo;
        }).collect(Collectors.toList());
    }
}
