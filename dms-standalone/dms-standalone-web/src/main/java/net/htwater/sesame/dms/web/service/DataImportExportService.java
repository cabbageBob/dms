package net.htwater.sesame.dms.web.service;

import com.csvreader.CsvWriter;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import core.dialect.Dialect;
import core.exception.SqlExecuteException;
import core.meta.ObjectType;
import core.meta.table.TableMetadata;
import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import net.htwater.sesame.dms.web.dataSource.DefaultJdbcExecutor;
import net.htwater.sesame.dms.web.domain.FileInfo;
import net.htwater.sesame.dms.web.entity.*;
import net.htwater.sesame.dms.web.file.FileParser;
import net.htwater.sesame.dms.web.file.FileType;
import net.htwater.sesame.dms.web.repository.DataImportExportRepository;
import net.htwater.sesame.dms.web.repository.FileInfoRepository;
import net.htwater.sesame.dms.web.util.Constants;
import net.htwater.sesame.dms.web.util.FreemarkerUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataImportExportService {
    private final
    FileInfoRepository fileInfoRepository;
    private final DataImportExportRepository dataImportExportRepository;
    private final
    ProjectSettings settings;
    private final DefaultJdbcExecutor sqlExecutor;
    private final
    SimpleDatabaseManagerService simpleDatabaseManagerService;
    private final EventBus eventBus;

    private static final String EXCEL_SUFFIX=".xlsx";

    private static final String CSV_SUFFIX=".csv";

    Map<FileType,FileParser> fileTypeMap = new HashMap<>();

    @Autowired
    public DataImportExportService(FileInfoRepository fileInfoRepository,
                                   DataImportExportRepository dataImportExportRepository,
                                   ProjectSettings settings,
                                   DefaultJdbcExecutor sqlExecutor,
                                   SimpleDatabaseManagerService simpleDatabaseManagerService,
                                   EventBus eventBus) {
        this.fileInfoRepository = fileInfoRepository;
        this.dataImportExportRepository = dataImportExportRepository;
        this.settings = settings;
        this.eventBus = eventBus;
        String absolutePath = "";
        try {
            File path = new File(ResourceUtils.getURL("").getPath());
            absolutePath = path.getAbsolutePath();
        } catch (FileNotFoundException e) {
            log.error("初始化临时文件夹失败");
        }
        String uploadPath = absolutePath + settings.getUploadPath();
        String docPath = absolutePath + settings.getDocPath();
        File upload = new File(uploadPath);
        File doc = new File(docPath);
        if (!upload.exists()) {
            log.debug("初始化上传文件夹");
            upload.mkdirs();
        }
        if (!doc.exists()) {
            log.debug("初始化文档文件夹");
            doc.mkdirs();
        }
        this.settings.setUploadPath(uploadPath);
        this.settings.setDocPath(docPath);
        this.sqlExecutor = sqlExecutor;
        this.simpleDatabaseManagerService = simpleDatabaseManagerService;
    }

    public void fileTypeRegister(FileType fileType,FileParser fileParser){
        fileTypeMap.put(fileType,fileParser);
    }

    public FileUploadResponse upload(MultipartFile file, String filetype){
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        if (file.isEmpty()){
            fileUploadResponse.setMsg("上传失败，请选择文件");
            return fileUploadResponse;
        }

        String fileName = Instant.now().toEpochMilli() + "_" + file.getOriginalFilename();
        //文件存放目录
        String filePath = this.settings.getUploadPath();
        //文件绝对路径
        String desc =filePath+fileName;

        File fileDest = new File(desc);
        try {
            fileUploadResponse.setFields(getFields(file.getInputStream(),filetype,fileName));
        } catch (IOException e) {
            log.error(e.toString(),e);
            fileUploadResponse.setMsg("获取字段失败");
            return fileUploadResponse;
        }
        try {
            file.transferTo(fileDest);
            log.info("文件"+fileName+"保存至："+filePath);
        } catch (IOException e) {
            log.error(e.toString(),e);
            fileUploadResponse.setMsg("上传失败，服务器异常");
            return fileUploadResponse;
        }
        String uid = UUID.randomUUID().toString();
        FileInfo fileInfo = new FileInfo(uid, fileName, desc);
        fileUploadResponse.setFileId(insertUploadFile(fileInfo));
        fileUploadResponse.setMsg("上传成功");
        return fileUploadResponse;
    }

    /**
     * 保存文件上传记录
     * @param fileInfo
     * @return 插入记录的id
     */
    public String insertUploadFile(FileInfo fileInfo){
        FileInfo newFileInfo = fileInfoRepository.save(fileInfo);
        return newFileInfo.getId();
    }

    /**
     * 从文件中获取字段名
     * @param is
     * @param filetype
     * @param fileName
     * @return
     * @throws IOException
     */
    public List<Object> getFields(InputStream is, String filetype,String fileName) throws IOException {
        FileParser parser = fileTypeMap.get(FileType.valueOf(filetype));
        return parser.getFields(is,fileName);
    }

    public DataImportResponse importData(DataImportRequest request) {
        FileParser parser = fileTypeMap.get(FileType.valueOf(request.getFileType()));
        Optional<FileInfo> fileInfoOptional = fileInfoRepository.findById(request.getFileId());
        DataImportResponse result = new DataImportResponse();
        Dialect dialect = simpleDatabaseManagerService.getTableDialect();
        if (fileInfoOptional.isPresent()) {
            FileInfo fileInfo = fileInfoOptional.get();
            File file = new File(fileInfo.getFilepath());
            //执行的条数
            int resultCount = 0;
            try {
                InputStream inputStream = new FileInputStream(file);
                //解析文件
                List<List<Object>> dataList = parser.parseFile(inputStream, fileInfo.getFilename());
                //从文件中读取字段名
                List<Object> fields = dataList.get(0);
                //移除不需要的行
                int i = 0;
                for (Iterator<List<Object>> it = dataList.iterator();it.hasNext();) {
                    it.next();
                    boolean isBiggerThanLast = request.getLastDataRow() > 0 && i > request.getLastDataRow() - 1;
                    if (i < request.getDataRow() - 1 || isBiggerThanLast) {
                        it.remove();
                    }
                    i++;
                }
                Map<String, ColumnClass> columnTypeMap = getColumnTypeMap(request.getTable());
                DataImportRequest.BatchSqlArgs batchSqlArgs = request.getStrategy()
                        .build(dialect, columnTypeMap, dataList, request);
                resultCount = sqlExecutor.executeSQLBatch(batchSqlArgs.getSql(), batchSqlArgs.getObjects());
                result.setMessage(String.format("成功导入%d条记录", resultCount));
                result.setResultCount(resultCount);
                long now =LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                RecordEntity recordEntity;
                if (request.getStrategy().equals(DataImportRequest.ImportStrategy.ADD)){
                    eventBus.post(new SaveRecordEntity(resultCount,RecordType.ADD));
                }else {
                    eventBus.post(new SaveRecordEntity(resultCount,RecordType.UPDATE));
                }
            } catch (FileNotFoundException e) {
                log.error(e.toString(),e);
                result.setMessage(String.format("文件[%s]不存在", request.getFileId()));
            } catch (SQLException e) {
                throw new SqlExecuteException(e.getMessage(), e, "导入失败");
            }
            return result;
        }
        result.setMessage(String.format("文件[%s]不存在", request.getFileId()));
        return result;
    }

    private String quote(Dialect dialect,String name){
        return dialect.openQuote()+name+dialect.closeQuote();
    }

    private List<Integer> getIndex(Map<String,String> fieldMap,List<Object> fields) {
        List<Integer> indexList = new ArrayList<>();
        Set entries = fieldMap.entrySet();
        for (Object entry1 : entries) {
            Map.Entry entry = (Map.Entry) entry1;
            String key = (String) entry.getKey();
            System.out.println(key.equals(fields.get(0)));
            int index = fields.indexOf(key);
            //需要保存的字段的下标
            indexList.add(index);
        }
        return indexList;
    }

    public FileUploadResponse execScript(MultipartFile file){
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        if (file.isEmpty()){
            fileUploadResponse.setMsg("上传失败，请选择文件");
            return fileUploadResponse;
        }
        String fileName = file.getOriginalFilename();
        //文件存放目录
        String filePath = this.settings.getUploadPath();
        //文件绝对路径
        String desc =filePath+fileName;

        File fileDest = new File(desc);
        try {
            file.transferTo(fileDest);
            log.info("文件"+fileName+"保存至："+filePath);
        } catch (IOException e) {
            log.error(e.toString(),e);
            fileUploadResponse.setMsg("获取文件失败");
            return fileUploadResponse;
        }
        fileInfoRepository.save(new FileInfo(UUID.randomUUID().toString(), fileName,desc));

        Connection conn = sqlExecutor.getConnection();
        ScriptUtils.executeSqlScript(conn,new EncodedResource(new FileSystemResource(fileDest)));
        try {
            sqlExecutor.releaseConnection(conn);
        } catch (SQLException e) {
            log.error(e.toString(),e);
        }
        fileUploadResponse.setMsg("运行sql文件完成");
        return fileUploadResponse;
    }

    public ResponseEntity<byte[]> exportTableToExcel(String tableName, String datasourceId) {
        QueryResult queryResult = simpleDatabaseManagerService.getAllColumns(tableName);
        if (queryResult == null) {
            return null;
        }
        byte[] body = null;
        body = exportToExcel(queryResult,tableName);
        log.info(String.format("export excel for table[%s] of datasource[%s]", tableName, datasourceId));
        String fileName = tableName + EXCEL_SUFFIX;
        eventBus.post(new SaveRecordEntity(queryResult.getData().size(),RecordType.SELECT));
        return getResponseEntity(fileName,body);
    }

    public  ResponseEntity<byte[]> exportCSV(String tableName, String datasourceId){
        QueryResult queryResult = simpleDatabaseManagerService.getAllColumns(tableName);
        if (queryResult == null) {
            return null;
        }
        byte[] body = exportToCsv(queryResult);
        log.info(String.format("export csv for table[%s] of datasource[%s]", tableName, datasourceId));
        String fileName = tableName + CSV_SUFFIX;
        eventBus.post(new SaveRecordEntity(queryResult.getData().size(),RecordType.SELECT));
        return getResponseEntity(fileName,body);

    }

    public ResponseEntity<byte[]> exportSelectDataToExcel(String sql, String table){
        QueryResultWrapper wrapper = new QueryResultWrapper();
        QueryResult queryResult = doPage(sql, wrapper);
        if (queryResult==null){
            return null;
        }
        queryResult.setColumns(processColumns(table, queryResult.getColumns()));
        byte[] body;
        body = exportToExcel(queryResult,null);
        String timeStr= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = timeStr + EXCEL_SUFFIX;
        eventBus.post(new SaveRecordEntity(queryResult.getData().size(),RecordType.SELECT));
        return getResponseEntity(fileName,body);
    }

    public ResponseEntity<byte[]> exportSelectDataToCsv(String sql, String table){
        QueryResultWrapper wrapper = new QueryResultWrapper();
        QueryResult queryResult = doPage(sql, wrapper);
        if (queryResult==null){
            return null;
        }
        queryResult.setColumns(processColumns(table, queryResult.getColumns()));
        byte[] body;
        body = exportToCsv(queryResult);
        String timeStr= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = timeStr + CSV_SUFFIX;
        eventBus.post(new SaveRecordEntity(queryResult.getData().size(),RecordType.SELECT));
        return getResponseEntity(fileName, body);
    }


    public ViewInfo getUserViewRecord(){
        ViewInfo viewInfo = new ViewInfo();
        //上周一开始时
        LocalDateTime lastMonday = LocalDate.now().plusWeeks(-1).with(DayOfWeek.MONDAY).atStartOfDay();
        long lastMondayMilli =  toMilli(lastMonday);
        //上周日结束时,同时也是本周一开始时
        LocalDateTime lastSunday =LocalDate.now().plusWeeks(-1).with(DayOfWeek.MONDAY).plusDays(7).atStartOfDay();
        long lastSundayMilli = toMilli(lastSunday);

        //当前日期
        LocalDateTime now = LocalDateTime.now();
        long nowMilli = toMilli(now);

        List<RecordEntity>recordEntities = dataImportExportRepository
                .findRecordEntityByTimeBetween(lastMondayMilli,nowMilli);

        //上传量
        List<RecordEntity> uploadNum = recordEntities.stream()
                .filter(recordEntity -> recordEntity.getType().name().equals(RecordType.ADD.name())).collect(Collectors.toList());
        List<RecordEntity> thisWeekUpload=uploadNum.stream()
                .filter(recordEntity -> recordEntity.getTime()>=lastSundayMilli).collect(Collectors.toList());
        viewInfo.setUploadNum(thisWeekUpload.size());
        viewInfo.setLastUploadNum(uploadNum.size()-thisWeekUpload.size());
        viewInfo.setUploadRecords(getSevenDayRecords(uploadNum));

        //下载量
        List<RecordEntity> downNum = recordEntities.stream()
                .filter(recordEntity -> recordEntity.getType().name().equals(RecordType.SELECT.name())).collect(Collectors.toList());
        List<RecordEntity> thisDownNum=downNum.stream()
                .filter(recordEntity -> recordEntity.getTime()>=lastSundayMilli).collect(Collectors.toList());
        viewInfo.setDownloadNum(thisDownNum.size());
        viewInfo.setLastDownloadNum(downNum.size()-thisDownNum.size());
        viewInfo.setDownloadRecords(getSevenDayRecords(downNum));

        //更新量
        List<RecordEntity> updateNum = recordEntities.stream()
                .filter(recordEntity -> recordEntity.getType().name().equals(RecordType.UPDATE.name())).collect(Collectors.toList());
        List<RecordEntity> thisUpdateNum=updateNum.stream()
                .filter(recordEntity -> recordEntity.getTime()>=lastSundayMilli).collect(Collectors.toList());
        viewInfo.setUpdateNum(thisUpdateNum.size());
        viewInfo.setLastUpdateNum(updateNum.size()-thisUpdateNum.size());
        viewInfo.setUpdateRecords(getSevenDayRecords(updateNum));
        return viewInfo;
    }

    private List<Map<Long,Long>> getSevenDayRecords(List<RecordEntity> entities){
        List<Map<Long,Long>> records = new ArrayList<>();
        for (long i =1;i<8;i++){
            Map<Long,Long> map = new HashMap<>();
            LocalDateTime day = LocalDate.now().plusDays(-i).atStartOfDay();
            LocalDateTime plusOneDay = LocalDate.now().plusDays(-i+1).atStartOfDay();
            long dayMilli = toMilli(day);
            long  plusOneDayMilli = toMilli(plusOneDay);
            map.put(dayMilli, (long) entities.stream().filter(recordEntity ->
                    recordEntity.getTime()>=dayMilli&&recordEntity.getTime()<=plusOneDayMilli)
                    .collect(Collectors.toList()).size()
            );
            records.add(map);
        }
        return records;
    }

    private long toMilli(LocalDateTime time){
        return LocalDateTime.from(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private QueryResult doPage(String sql, QueryResultWrapper wrapper) {
        try {
            Dialect dialect = simpleDatabaseManagerService.getTableDialect();
            sqlExecutor.list(dialect.doPage(sql, 1, settings.getExportSize()), wrapper);
            return wrapper.getResult();
        } catch (SQLException e) {
            throw new SqlExecuteException(e.getMessage(), e, sql);
        }
    }

    private List<String> processColumns(String table, List<String> columns) {
        if (table != null) {
            Map<String, String> columnNameMap = Maps.newHashMap();
            TableMetadata tableMetadata = simpleDatabaseManagerService
                    .parse(table, simpleDatabaseManagerService.getTableDialect(),
                            DataSourceHolder.currentDataSource().getId(), false);
            tableMetadata.getColumns()
                    .forEach(columnMetadata ->
                            columnNameMap.put(columnMetadata.getName(),
                                    columnMetadata.getComment())
                    );
            return columns.stream()
                    .map(name -> {
                        String columnName = columnNameMap.get(name);
                        if (!StringUtils.isEmpty(columnName)) {
                            return columnName;
                        }
                        return name;
                    })
                    .collect(Collectors.toList());
        }
        return columns;
    }

    private Map<String, ColumnClass> getColumnTypeMap(String table) {
        Map<String, ColumnClass> columnNameMap = Maps.newHashMap();
        TableMetadata tableMetadata = simpleDatabaseManagerService
                .parse(table, simpleDatabaseManagerService.getTableDialect(),
                        DataSourceHolder.currentDataSource().getId(), false);
        tableMetadata.getColumns()
                .forEach(columnMetadata ->
                        columnNameMap.put(columnMetadata.getName(),
                                columnMetadata.getTypeClassify())
                );
        return columnNameMap;
    }

    private byte[] exportToExcel(QueryResult queryResult,String sheetName){
        List<String> fields = queryResult.getColumns();
        List<List<Object>> fieldDatas = queryResult.getData();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet;
        byte[] body = null;
        File file = null;
        try {
            if (sheetName==null){
                sheet = workbook.createSheet();
            }else {
                sheet = workbook.createSheet(sheetName);
            }
            Row headRow = sheet.createRow(0);
            for (int i = 0; i < fields.size(); i++) {
                Cell headCell = headRow.createCell(i);
                headCell.setCellValue(fields.get(i));
            }
            for (int m = 0; m < fieldDatas.size(); m++) {
                Row dataRow = sheet.createRow(m + 1);
                List<Object> data = fieldDatas.get(m);
                for (int n = 0; n < data.size(); n++) {
                    dataRow.createCell(n).setCellValue(setVal(data.get(n)));
                }
            }
            try {
                file = File.createTempFile( String.valueOf(System.currentTimeMillis()),EXCEL_SUFFIX,
                        new File(settings.getUploadPath()));
            } catch (IOException e) {
                log.error(e.toString(), e);
            }
            assert file != null;
            try(OutputStream outputStream = new FileOutputStream(file)){
                workbook.write(outputStream);
            } catch (IOException e) {
                log.error(e.toString(), e);
            }
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return getBytes(file, body);
    }

    private byte[] exportToCsv(QueryResult queryResult) {
        List<String> fields = queryResult.getColumns();
        List<List<Object>> fieldDatas = queryResult.getData();
        File csvFile = null;
        try {
            csvFile = File.createTempFile(UUID.randomUUID().toString(),".csv", new File(settings.getUploadPath()));
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
        assert csvFile != null;
        try(OutputStream outputStream = new FileOutputStream(csvFile)){
            CsvWriter csvWriter = new CsvWriter(outputStream, ',',Charset.forName("utf-8"));
            for (String field :fields){
                csvWriter.write(field);
            }
            csvWriter.endRecord();
            for (List<Object> datas :fieldDatas){
                for (Object data:datas){
                    csvWriter.write(setVal(data));
                }
                csvWriter.endRecord();
            }
            csvWriter.close();
        }catch (IOException e) {
            log.error(e.toString(), e);
        }
        byte[] body = null;
        return getBytes(csvFile, body);
    }

    private ResponseEntity<byte[]> getResponseEntity(String fileName,byte[] body){
        HttpHeaders httpHeaders = new HttpHeaders();
        try {
            httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment;filename=" + new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
            return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + fileName);
        return new ResponseEntity<>(body,httpHeaders,HttpStatus.OK);
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

    @SuppressWarnings("unchecked")
    public ResponseEntity<byte[]> exportDatabaseDicDoc(String dbname, Set<String> tableNames){
        Map<String,Object> dicMap = Maps.newHashMap();
        List<TableMetadata> tables = ((List<TableMetadata>) simpleDatabaseManagerService.getMetas().get(ObjectType.TABLE))
                .stream()
                .filter(table -> tableNames.contains(table.getName()))
                .collect(Collectors.toList());
        dicMap.put("tables", tables);
        String fileName = dbname+"数据库说明书.doc";
        File file = FreemarkerUtil.createDoc("数据库说明书模板.xml",dicMap,fileName, settings.getDocPath());
        byte[] body = null;
        body=getBytes(file, body);
        HttpHeaders httpHeaders = new HttpHeaders();
        try {
            httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + fileName);
        return new ResponseEntity<>(body,httpHeaders,HttpStatus.OK);
    }

    private static String setVal(Object o){
        if (o instanceof java.util.Date){
            return Constants.DEFAULT_DATE_FORMAT.format(o);
        } else if (o == null) {
            return null;
        }
        return String.valueOf(o);

    }
}
