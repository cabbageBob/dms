package net.htwater.sesame.dms.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import net.htwater.sesame.dms.web.entity.*;
import net.htwater.sesame.dms.web.file.FileParser;
import net.htwater.sesame.dms.web.service.DataImportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;


@Controller
@RequestMapping("/database")
@Api(tags = "数据录入与导出")
public class DataImportExportController {
    @Autowired
    ProjectSettings filePath;
    @Autowired
    DataImportExportService dataImportExportService;

    private FileParser fileParser;

    @PostMapping("/upload")
    @ResponseBody
    @ApiOperation(value = "上传文件并返回字段名")
    public FileUploadResponse upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam("fileType") String fileType){
        return dataImportExportService.upload(file,fileType);
    }
    @PostMapping("/import/{dataSourceId}")
    @ResponseBody
    @ApiOperation(value = "解析文件并将数据导入对应数据库")
    public DataImportResponse importData(@PathVariable String dataSourceId, @RequestBody DataImportRequest request){
        DataSourceHolder.switcher().use(dataSourceId);
        return dataImportExportService.importData(request);
    }

    @PostMapping("/execScript/{dataSourceId}")
    @ResponseBody
    @ApiOperation(value = "执行sql脚本")
    public FileUploadResponse execScript(@RequestParam("file") MultipartFile file, @PathVariable String dataSourceId){
        DataSourceHolder.switcher().use(dataSourceId);
        return dataImportExportService.execScript(file);
    }

    @GetMapping(value = "/export/excel/{dataSourceId}/{tableName}", produces = "application/octet-stream;charset=UTF-8")
    @ApiOperation(value = "导出excel文件")
    public ResponseEntity<byte[]> exportExcel(@PathVariable String tableName, @PathVariable String dataSourceId){
        DataSourceHolder.switcher().use(dataSourceId);
        return dataImportExportService.exportTableToExcel(tableName, dataSourceId);
    }
    @GetMapping(value = "/export/csv/{dataSourceId}/{tableName}", produces = "application/octet-stream;charset=UTF-8")
    @ApiOperation(value = "导出csv文件")
    public ResponseEntity<byte[]> exportCSV(@PathVariable String tableName, @PathVariable String dataSourceId){
        DataSourceHolder.switcher().use(dataSourceId);
        return dataImportExportService.exportCSV(tableName, dataSourceId);
    }

    @PostMapping(value = "/export/select/{dataSourceId}", produces = "application/octet-stream;charset=UTF-8")
    @ApiOperation(value = "导出查询结果到Excel")
    public ResponseEntity<byte[]> exportSelectData(@Valid ExportDataQuery query,
                                                   @PathVariable String dataSourceId){
        DataSourceHolder.switcher().use(dataSourceId);
        switch (query.getType()) {
            case CSV:
                return dataImportExportService.exportSelectDataToCsv(query.getSql(), query.getTable());
            case EXCEL:
                return dataImportExportService.exportSelectDataToExcel(query.getSql(), query.getTable());
            default:
                return dataImportExportService.exportSelectDataToCsv(query.getSql(), query.getTable());
        }
    }

    @PostMapping(value = "/export/doc/{datasourceId}")
    @ApiOperation("生成数据库说明书文档")
    public ResponseEntity<byte[]> exportDatabaseDicDoc(@PathVariable String datasourceId,
                                                       @Valid @RequestBody ExportDictionaryRequest request){
        DataSourceHolder.switcher().use(datasourceId);
        return dataImportExportService.exportDatabaseDicDoc(request.getDbName(), request.getTables());
    }

    @GetMapping(value = "/export/viewinfo")
    @ResponseBody
    public ViewInfo getViewInfo(){
        return dataImportExportService.getUserViewRecord();
    }
}
