package net.htwater.sesame.dms.web.service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.mongodb.client.result.UpdateResult;
import core.DatabaseManagerService;
import core.DatabaseType;
import core.dialect.AbstractDialect;
import core.dialect.Dialect;
import core.dialect.SqlServerDialect;
import core.exception.SqlExecuteException;
import core.meta.ObjectType;
import core.meta.table.ColumnMetadata;
import core.meta.table.TableMetadata;
import core.sql.SqlExecuteRequest;
import core.sql.SqlExecuteResult;
import core.sql.SqlInfo;
import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import net.htwater.sesame.dms.web.dataSource.DefaultJdbcExecutor;
import net.htwater.sesame.dms.web.dialect.TypeDataBase;
import net.htwater.sesame.dms.web.dialect.TypeDialect;
import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import net.htwater.sesame.dms.web.domain.TableCache;
import net.htwater.sesame.dms.web.entity.*;
import net.htwater.sesame.dms.web.entity.altertable.AlterTable;
import net.htwater.sesame.dms.web.reporter.DataSourceMetrics;
import net.htwater.sesame.dms.web.util.AESUtil;
import net.htwater.sesame.dms.web.util.DataManagerUtil;
import net.htwater.sesame.dms.web.util.JdbcUtil;
import net.htwater.sesame.dms.web.entity.CustomColumnClass.Value;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.hswebframework.ezorm.core.ObjectWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class SimpleDatabaseManagerService implements DatabaseManagerService{
    private final DefaultJdbcExecutor sqlExecutor;
    private final SimpleDataSourceConfigService simpleDataSourceConfigService;
    private final ProjectSettings filePath;
    private static ColumnMetadataWrapper wrapper = new ColumnMetadataWrapper();
    public Map<DatabaseType, Map<ObjectType, Dialect>> parserRepo = new HashMap<>();
    private final String PARAM_TABLE="table";
    private final CustomColumnClassService customColumnClassService;
    private final TableCacheService tableCacheService;
    private final DataSourceMetricsService dataSourceMetricsService;
    private final EventBus eventBus;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public SimpleDatabaseManagerService(DefaultJdbcExecutor sqlExecutor,
                                        SimpleDataSourceConfigService simpleDataSourceConfigService,
                                        ProjectSettings filePath,
                                        CustomColumnClassService customColumnClassService,
                                        TableCacheService tableCacheService,
                                        DataSourceMetricsService dataSourceMetricsService,
                                        EventBus eventBus, MongoTemplate mongoTemplate) {
        this.sqlExecutor = sqlExecutor;
        this.simpleDataSourceConfigService = simpleDataSourceConfigService;
        this.filePath = filePath;
        this.customColumnClassService = customColumnClassService;
        this.tableCacheService = tableCacheService;
        this.dataSourceMetricsService = dataSourceMetricsService;
        this.eventBus = eventBus;
        this.mongoTemplate = mongoTemplate;
    }

    public Map<DatabaseType, Map<ObjectType, Dialect>> getParserRepo() {
        return parserRepo;
    }

    public void registerMetaDataParser(DatabaseType databaseType, ObjectType objectType, Dialect parser) {
        parserRepo.computeIfAbsent(databaseType, t -> new HashMap<>())
                .put(objectType, parser);
    }

    private TypeDialect getTypeDialect(TypeDataBase typeDataBase, DatabaseMetaData metaData){
        return  typeDataBase.resolveDialect(metaData);
    }

    public Set<String> getColumnTypes(){
        String dbtype = DataSourceHolder.currentDatabaseType().name().toLowerCase();
        TypeDataBase typeDataBase = TypeDataBase.valueOf(dbtype.toUpperCase());
        DatabaseMetaData metaData = JdbcUtil.getDatabaseMetaData(DataSourceHolder.currentDataSource().getNative());
        TypeDialect typeDialect = getTypeDialect(typeDataBase,metaData);
        return typeDialect.getTypeNames();
    }

    public BaseEntity updateTableComment(AlterComment alterComment,String datasourceId){
        BaseEntity<Boolean> entity = new BaseEntity();
        entity.setResult(false);
        Dialect dialect = parserRepo.get(DataSourceHolder.currentDatabaseType()).get(ObjectType.TABLE);
        try {
            sqlExecutor.exec(dialect.setTableCommentSql(alterComment));
            tableCacheService.deleteById(DataManagerUtil.formatId(datasourceId, alterComment.getTable()));
            entity.setResult(true);
        } catch (SQLException e) {
            throw new SqlExecuteException(e.getMessage(),e,dialect.setTableCommentSql(alterComment));
        }
        return entity;
    }

    public BaseEntity updateColumnComment(AlterComment alterComment,String datasourceId){
        BaseEntity<Boolean> entity = new BaseEntity();
        entity.setResult(false);
        Dialect dialect = parserRepo.get(DataSourceHolder.currentDatabaseType()).get(ObjectType.TABLE);
        try {
            sqlExecutor.exec(dialect.setFieldCommentSql(alterComment));
            tableCacheService.deleteById(DataManagerUtil.formatId(datasourceId, alterComment.getTable()));
            entity.setResult(true);
        } catch (SQLException e) {
            throw new SqlExecuteException(e.getMessage(),e,dialect.setFieldCommentSql(alterComment));
        }
        return entity;
    }

    public List<String> getAllTableName(Dialect dialect) throws SQLException {
        return sqlExecutor.list(dialect.getSelectAllTableSql())
                .parallelStream()
                .map(map->map.get("name"))
                .map(String::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Map<ObjectType, List> getMetas() {
        return parserRepo
                .computeIfAbsent(DataSourceHolder.currentDatabaseType(), t -> Maps.newHashMap())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    try {
                        return parseAll((AbstractDialect)entry.getValue());
                    } catch (SQLException e) {
                        log.error("parse meta {} error", entry.getKey(), e);
                        return new ArrayList<>();
                    }
                }));
    }
    private List<TableMetadata> parseAll(AbstractDialect dialect) throws SQLException {
        String dsId = DataSourceHolder.switcher().currentDataSourceId();
        return sqlExecutor.list(dialect.getSelectAllTableSql())
                .parallelStream()
                .map(map -> map.get("name"))
                .map(String::valueOf)
                .map(tableName -> {
                    try {
                        DataSourceHolder.switcher().use(dsId);
                        return this.parse(tableName, dialect, dsId, true);
                    } finally {
                        DataSourceHolder.switcher().reset();
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public TableMetadata parse(String objectName, Dialect dialect, String datasourceId, boolean loadCustom) {
        Optional<TableCache> cache = tableCacheService.findById(DataManagerUtil.formatId(datasourceId, objectName));
        if (cache.isPresent()) {
            return convert(cache.get());
        }
        Map<String, Object> param = new HashMap<>(1);
        param.put(PARAM_TABLE, objectName);
        Map<String, Object> tableMetaMap = null;
        try {
            tableMetaMap = sqlExecutor.single(dialect.getSelectTableMetaSql(), param);
        } catch (SQLException e) {
            log.error("An error occurs while parsing table - ", objectName, e);
        }
        TableMetadata table = new TableMetadata();
        table.setName(objectName);
        if (tableMetaMap == null) {
            table.setComment("");
        }else {
            table.setComment((String) tableMetaMap.getOrDefault("comment", ""));
        }
        List<ColumnMetadata> columns = null;
        try {
            columns = sqlExecutor.list(dialect.getSelectTableColumnsSql(), param, wrapper);
        } catch (SQLException e) {
            log.error("An error occurs while parsing table - ", objectName, e);
        }
        if (loadCustom) {
            Map<String, List<CustomColumnClass>> customColumnClassMap =
                    customColumnClassService.findByDatasourceIdAndTable(DataSourceHolder.currentDataSource().getId(),
                            objectName)
                            .stream()
                            .collect(Collectors.groupingBy(CustomColumnClass::getColumn));
            classifyDataType(objectName, columns, dialect, customColumnClassMap);
        } else {
            classifyDataType(objectName, columns, dialect, null);
        }
        table.setColumns(columns);
        tableCacheService.save(convert(table, datasourceId));
        return table;
    }

    private TableCache convert(TableMetadata metadata, String datasourceId) {
        TableCache tableCache = new TableCache();
        tableCache.setId(DataManagerUtil.formatId(datasourceId, metadata.getName()));
        tableCache.setColumns(metadata.getColumns());
        tableCache.setComment(metadata.getComment());
        tableCache.setDatasourceId(datasourceId);
        tableCache.setName(metadata.getName());
        return tableCache;
    }

    private TableMetadata convert(TableCache cache) {
        TableMetadata tableMetadata = new TableMetadata();
        tableMetadata.setName(cache.getName());
        tableMetadata.setColumns(cache.getColumns());
        tableMetadata.setComment(cache.getComment());
        return tableMetadata;
    }

    public BaseEntity alterTableScript(AlterTable alterTable){
        Map<String, Object> param = new HashMap<>();
        param.put(PARAM_TABLE, alterTable.getOldTableName());
        Dialect dialect = parserRepo.get(DataSourceHolder.currentDatabaseType()).get(ObjectType.TABLE);
        TableMetadata tableMetadata = parse(alterTable.getOldTableName(),dialect,
                DataSourceHolder.currentDataSource().getId(), false);
        BaseEntity entity = new BaseEntity<String>();
        entity.setResult(dialect.alterTableScript(tableMetadata,alterTable));
        return entity;
    }


    @Override
    public List<SqlExecuteResult> execute(SqlExecuteRequest request,int currindex,int pagesize) {
        return request.getSql().stream().map(sqlinfo ->doExecute(sqlinfo,currindex,pagesize)).collect(Collectors.toList());
    }

    @Override
    public SqlExecuteResult executeSqlBatch(SqlExecuteRequest request) {
        List<String> sqlList = request.getSql().stream()
                .filter(sqlInfo -> Arrays.asList("INSERT", "UPDATE", "DELETE").contains(sqlInfo.getType().toUpperCase()))
                .map(SqlInfo::getSql)
                .collect(Collectors.toList());
        SqlExecuteResult result = new SqlExecuteResult();
        String message = "执行成功";
        Stopwatch stopwatch = null;
        try {
            stopwatch =  Stopwatch.createStarted();
            int executeResult = sqlExecutor.executeSQLBatch(sqlList);
            stopwatch.stop();
            message = String.format("执行成功, 影响行数: [%d]行, 耗时: %s", executeResult, stopwatch.toString());
            result.setResult(executeResult);
            result.setSuccess(true);
            request.getSql().stream().map(sqlInfo -> {
                if (sqlInfo.getType().toUpperCase().equals("INSERT")){
                    eventBus.post(new SaveRecordEntity(executeResult,RecordType.ADD));
                }else if (sqlInfo.getType().toUpperCase().equals("UPDATE")){
                    eventBus.post(new SaveRecordEntity(executeResult,RecordType.UPDATE));
                }
                return null;
            });
        } catch (SQLException e) {
            throw new SqlExecuteException(e.getMessage(), e, sqlList.toString());
        } finally {
            if (stopwatch != null && stopwatch.isRunning()) {
                stopwatch.stop();
            }
        }
        result.setMessage(message);
        return result;
    }


    @Override
    public Dialect getTableDialect() {
        return parserRepo.computeIfAbsent(DataSourceHolder.currentDatabaseType(), t ->
                Maps.newHashMap()).get(ObjectType.TABLE);
    }

    @Override
    public SqlExecuteResult doExecute(SqlInfo sqlInfo, int pageindex, int pagesize) {
        SqlExecuteResult result = new SqlExecuteResult();
        Object executeResult = null;
        Stopwatch stopwatch = null;
        String message = "执行成功";
        try {
            stopwatch =  Stopwatch.createUnstarted();
            switch (sqlInfo.getType().toUpperCase()) {
                case "SELECT":
                    Dialect dialect= parserRepo
                            .computeIfAbsent(DataSourceHolder.currentDatabaseType(), t -> new HashMap<>()).get(ObjectType.TABLE);
                    QueryResultWrapper wrapper = new QueryResultWrapper();
                    stopwatch.start();
                    sqlExecutor.list(dialect.doPage(sqlInfo.getSql(),pageindex,pagesize), wrapper);
                    stopwatch.stop();
                    QueryResult queryResult =  wrapper.getResult();
                    List<String> columns = queryResult.getColumns();
                    if (columns.contains("rownum_temp")){
                        columns.remove(0);
                        List<List<Object>> datas=queryResult.getData();
                        for (List<Object> data:datas){
                            data.remove(0);
                        }
                        queryResult.setColumns(columns);
                        queryResult.setData(datas);
                    }
                    executeResult = queryResult;
                    message = String.format("执行成功, 当前返回: [%d]行, 耗时: %s", queryResult.getData().size(),
                            stopwatch.toString());
                    eventBus.post(new SaveRecordEntity(queryResult.getData().size(),RecordType.SELECT));
                    break;
                case "INSERT":
                    stopwatch.start();
                    executeResult = sqlExecutor.update(sqlInfo.getSql());
                    stopwatch.stop();
                    message = String.format("执行成功, 影响行数: [%d]行, 耗时: %s", (int)executeResult,
                            stopwatch.toString());
                    eventBus.post(new SaveRecordEntity((Integer) executeResult,RecordType.ADD));
                    break;
                case "UPDATE":
                    stopwatch.start();
                    executeResult = sqlExecutor.update(sqlInfo.getSql());
                    stopwatch.stop();
                    message = String.format("执行成功, 影响行数: [%d]行, 耗时: %s", (int)executeResult,
                            stopwatch.toString());
                    eventBus.post(new SaveRecordEntity((Integer) executeResult,RecordType.UPDATE));
                    break;
                case "DELETE":
                    stopwatch.start();
                    executeResult = sqlExecutor.delete(sqlInfo.getSql());
                    stopwatch.stop();
                    message = String.format("执行成功, 影响行数: [%d]行, 耗时: %s", (int)executeResult,
                            stopwatch.toString());
                    //importDBTableById(id);
                    break;
                default:
                    sqlExecutor.exec(sqlInfo.getSql());
            }
            result.setSuccess(true);
        } catch (SQLException e) {
            throw new SqlExecuteException(e.getMessage(), e, sqlInfo.getSql());
        } finally {
            if (stopwatch != null && stopwatch.isRunning()) {
                stopwatch.stop();
            }
        }
        result.setMessage(message);
        result.setResult(executeResult);
        result.setSqlInfo(sqlInfo);

        return result;
    }

    public QueryResult showTable(String tablename){
        Map<String, Object> param = new HashMap<>();
        param.put(PARAM_TABLE, tablename);
        Map<ObjectType,Dialect> map = parserRepo.computeIfAbsent(DataSourceHolder.currentDatabaseType(),t ->new HashMap<>());
        Dialect dialect = map.get(ObjectType.TABLE);
        QueryResultWrapper wrapper = new QueryResultWrapper();
        try {
            sqlExecutor.list(dialect.showTable(),param,wrapper);
        } catch (SQLException e) {
            throw new SqlExecuteException(e.getMessage(), e,dialect.showTable());
        }
        return wrapper.getResult();
    }

    QueryResult getAllColumns(String tablename){
        Map<String, Object> param = new HashMap<>();
        param.put(PARAM_TABLE, tablename);
        QueryResultWrapper wrapper = new QueryResultWrapper();
        try {
            sqlExecutor.list("select * from ${table}",param,wrapper);
        } catch (SQLException e) {
            throw new SqlExecuteException(e.getMessage(), e, String.format("select * from %s", tablename));
        }
        return wrapper.getResult();
    }

    @Deprecated
    public ManagerServiceResult backup(String userName, String password, String savePath, String fileName,
                                     String databaseName){
        Map<String, Object> param = new HashMap<>();
        ManagerServiceResult result = new ManagerServiceResult();
        Dialect dialect = parserRepo.computeIfAbsent(DataSourceHolder.currentDatabaseType(),t ->new HashMap<>()).get(ObjectType.TABLE);
        File saveFile = new File(savePath);
        if (!saveFile.exists()){
            saveFile.mkdirs();
        }
        if (!savePath.endsWith(File.separator)){
            savePath += File.separator;
        }
        if (dialect instanceof SqlServerDialect){
            fileName +=".bak";
            param.put("database", databaseName);
            param.put("path",savePath+fileName);
            try {
                sqlExecutor.exec(dialect.getBackupSql(),param);
            } catch (SQLException e) {
               log.error(e.toString(),e);
               result.setResult(0);
            }
            result.setResult(1);
        }else {
            fileName += ".sql";
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mysqldump").append(" --user=").append(userName)
                    .append(" --password=").append(AESUtil.decrypt(password,AESUtil.ENCODE_KEY)).append(" --result-file=")
                    .append(savePath + fileName).append(" --default-character-set=utf8 ")
                    .append(databaseName);
            try {
                Process process = Runtime.getRuntime().exec(stringBuilder.toString());
                if (process.waitFor()==0){
                    result.setResult(1);
                }else {
                    result.setResult(0);
                }
            } catch (IOException e) {
                log.error(e.toString(),e);
            } catch (InterruptedException e) {
                log.error(e.toString(),e);
            }
        }
        return result;
    }

    public ManagerServiceResult testConnect(InDBDynamicDataSourceConfig config){
        ManagerServiceResult result = new ManagerServiceResult();
        boolean bool = JdbcUtil.testConncet(config.getDbtype(),config.getIp(),config.getPort(),
                config.getDbname(),config.getUsername(),config.getPassword());
        if (bool){
            result.setResult(1);
        }else {
            result.setResult(0);
        }
        return result;
    }

    public BaseEntity queryTplContent(){
        String dbType = DataSourceHolder.currentDatabaseType().name().toLowerCase();
        TypeDataBase typeDataBase = TypeDataBase.valueOf(dbType.toUpperCase());
        TypeDialect typeDialect = typeDataBase.getParent();
        BaseEntity baseEntity = new BaseEntity();
        baseEntity.setResult(typeDialect.getSqlScripts());
        return baseEntity;
    }

    static class ColumnMetadataWrapper implements ObjectWrapper<ColumnMetadata> {
        static Map<String, BiConsumer<ColumnMetadata, Object>> propertySetters = new HashMap<>();

        static {
            propertySetters.put("name", (columnMetadata, value) -> columnMetadata.setName(String.valueOf(value)));

        }

        @Override
        public Class<ColumnMetadata> getType() {
            return ColumnMetadata.class;
        }

        @Override
        public ColumnMetadata newInstance() {
            return new ColumnMetadata();
        }

        @Override
        public void wrapper(ColumnMetadata instance, int index, String attr, Object value) {
            try {
                BeanUtilsBean.getInstance().setProperty(instance, attr, value);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }

        @Override
        public boolean done(ColumnMetadata instance) {
            return true;
        }
    }

    private byte[] getBytes(File file, byte[] body) {
        try( InputStream inputStream = new FileInputStream(file)){
            body = new byte[inputStream.available()];
            inputStream.read(body);
            inputStream.close();
            file.delete();
        } catch (FileNotFoundException e) {
            log.error(e.toString(), e);
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
        return body;
    }



    private void classifyDataType(String tableName,List<ColumnMetadata> list,Dialect dialect, Map<String,
            List<CustomColumnClass>> columnClassMap){
        for (ColumnMetadata columnMetadata : list) {
            switch (columnMetadata.getDataType().toLowerCase()){
                case "varchar":
                case "char":
                case "nchar":
                case "nvarchar":
                case "text":
                case "tinytext":
                case "longtext":
                case "varchar2":
                case "long":
                case "clob":
                case "nclob":
                    columnMetadata.setTypeClassify(ColumnClass.STRING);
                    break;
                case "date":
                case "time":
                case "datetime":
                case "timestamp":
                    columnMetadata.setTypeClassify(ColumnClass.DATE);
                    break;
                case "numeric":
                case "decimal":
                case "float":
                case "double":
                case "int":
                case "integer":
                case "bigint":
                case "smallint":
                case "tinyint":
                case "number":
                case "real":
                    columnMetadata.setTypeClassify(ColumnClass.NUMBER);
                    break;
                case "binary":
                case "image":
                case "blob":
                case "varbinary":
                case "long row":
                    columnMetadata.setTypeClassify(ColumnClass.BINARY);
                    break;
                case "enum":
                    try {
                        Set<Value> valueList = Sets.newLinkedHashSet();
                        Map<String, Object> param = new HashMap<>();
                        param.put("table",tableName);
                        param.put("fieldName",columnMetadata.getName());
                        Map<String, Object> map = sqlExecutor.single(dialect.getEnumValuesSql(),param);
                        String columnType= (String)map.get("column_type");
                        String valueStr = columnType.substring(5,columnType.length()-1);
                        for (String s :valueStr.split(",")){
                            valueList.add(new Value(s.substring(1,s.length()-1), ""));
                        }
                        columnMetadata.setEnumValue(valueList);
                    } catch (SQLException e) {
                       log.error(e.toString());
                    }
                    columnMetadata.setTypeClassify(ColumnClass.ENUM);
                    break;
                case "bit":
                    columnMetadata.setTypeClassify(ColumnClass.ENUM);
                    Set<Value> valueList = Sets.newLinkedHashSet();
                    valueList.add(new Value(0, "是"));
                    valueList.add(new Value(1, "否"));
                    columnMetadata.setEnumValue(valueList);
                    break;
                default:
                    columnMetadata.setTypeClassify(ColumnClass.STRING);

            }
            if (columnClassMap != null && columnClassMap.get(columnMetadata.getName()) != null) {
                CustomColumnClass columnClass = columnClassMap.get(columnMetadata.getName()).get(0);
                if (columnClass.getColumnClass() != null) {
                    columnMetadata.setTypeClassify(columnClass.getColumnClass());
                    if (columnClass.getColumnClass() == ColumnClass.ENUM) {
                        columnMetadata.setEnumValue(columnClass.getValues());
                    }
                }
            }

        }
    }

    public List<SearchTableEntity> searchByQ(String q) {
        Map<String, DataSourceConfig> dataSourceConfigMap = Maps.newHashMap();
        simpleDataSourceConfigService.findAll()
                .forEach(config ->
                    dataSourceConfigMap.put(config.getId(), config)
                );
        return tableCacheService.findAllByQ(q)
                .stream()
                .filter(cache -> dataSourceConfigMap.containsKey(cache.getDatasourceId()))
                .map(cache -> convert(cache, dataSourceConfigMap))
                .collect(Collectors.toList());

    }

    public List<SearchTableEntity> searchByIds(List<String> ids) {
        Map<String, DataSourceConfig> dataSourceConfigMap = Maps.newHashMap();
        simpleDataSourceConfigService.findAll()
                .forEach(config ->
                        dataSourceConfigMap.put(config.getId(), config)
                );
        return tableCacheService.findAllByIds(ids)
                .stream()
                .filter(cache -> dataSourceConfigMap.containsKey(cache.getDatasourceId()))
                .map(cache -> convert(cache, dataSourceConfigMap))
                .collect(Collectors.toList());
    }

    @Async
    @Override
    public void monitorDatasource(String datasourceId, String dbName) {
        DataSourceHolder.switcher().use(datasourceId);
        Dialect dialect = parserRepo.computeIfAbsent(DataSourceHolder.currentDatabaseType(),
                t -> Maps.newHashMap()).get(ObjectType.TABLE);
        try {
            Map<String, Object> param = Maps.newHashMap();
            param.put("dbname", dbName);
            Map<String, Object> rowsMap = sqlExecutor.single(dialect.getRowsSql(), param);
            Map<String, Object> sizeMap = sqlExecutor.single(dialect.getMemorySql(), param);
            Map<String, Object> connsMap = sqlExecutor.single(dialect.getThreadsConnectedSql(),param);
            long rows = rowsMap.get("num_rows") != null ? Long.valueOf(rowsMap.get("num_rows").toString()) : 0;
            float size = sizeMap.get("memory") != null ?  Float.valueOf(sizeMap.get("memory").toString()) : 0;
            int conns = connsMap.get("conns") !=null ? Integer.valueOf(connsMap.get("conns").toString()) : 0;
            DataSourceMetrics metrics = DataSourceMetrics.builder().at(Instant.now().toEpochMilli())
                    .datasource(datasourceId)
                    .conns(conns)
                    .count(rows).size(size).build();
            dataSourceMetricsService.bulkAsync(metrics);
        } catch (SQLException e) {
            log.error("数据源{}监控失败：", datasourceId, e);
        }
    }

    private SearchTableEntity convert(TableCache cache, Map<String, DataSourceConfig> dataSourceConfigMap) {
        SearchTableEntity tableEntity = new SearchTableEntity();
        DataSourceConfig config = dataSourceConfigMap.get(cache.getDatasourceId());
        tableEntity.setId(cache.getId());
        tableEntity.setComment(cache.getComment());
        tableEntity.setDatasourceId(cache.getDatasourceId());
        tableEntity.setName(cache.getName());
        tableEntity.setDbName(config.getDbname());
        tableEntity.setDisplayDbName(config.getName());
        tableEntity.setIp(config.getIp());
        tableEntity.setPort(config.getPort());
        return tableEntity;
    }

}
