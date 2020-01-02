package net.htwater.sesame.dms.web.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import net.htwater.sesame.dms.web.entity.UpdatedDataSourceConfig;
import net.htwater.sesame.dms.web.entity.cache.DataSourceInfo;
import net.htwater.sesame.dms.web.service.DataSourceConfigService;
import net.htwater.sesame.dms.web.service.DataSourceMetaAsyncTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("datasource/config")
@Api(value = "数据源配置",tags = "动态数据源-数据源配置")
@Slf4j
public class DataSourceConfigController {
    @Autowired
    private DataSourceConfigService dataSourceConfigService;

    @Autowired
    private DataSourceMetaAsyncTask dataSourceMetaAsyncTask;

    @GetMapping
    @ApiOperation("获取所有数据源信息")
    public List<DataSourceConfig> getDatasources(){
       return dataSourceConfigService.findAll();
    }

    @GetMapping("/info")
    @ApiOperation("获取所有数据源信息(包含连接状态)")
    public List<DataSourceInfo> getDatasourceInfo(){
        return dataSourceConfigService.getInfo();
    }

    @GetMapping("/{id}")
    @ApiOperation("通过id获取数据源信息")
    public DataSourceConfig getDataSourceById(@PathVariable String id){
        return  dataSourceConfigService.findById(id);
    }
    @PostMapping("/add")
    @ApiOperation("添加数据源")
    public DataSourceConfig add(DataSourceConfig config){
        DataSourceConfig newConfig = dataSourceConfigService.add(config);
        DataSourceHolder.switcher().use(newConfig.getId());
        //初始化元数据
        dataSourceMetaAsyncTask.initMetas(config.getId());
        return newConfig;
    }

    @PutMapping("/{id}")
    @ApiOperation("修改数据源")
    public DataSourceConfig update(@Valid UpdatedDataSourceConfig config){
        return dataSourceConfigService.update(config);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除数据源")
    public boolean remove(@PathVariable("id") String dataSourceId){
        return dataSourceConfigService.remove(dataSourceId);
    }
}
