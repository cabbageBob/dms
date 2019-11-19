package net.htwater.sesame.dms.service.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.htwater.sesame.dms.service.config.InDBDynamicDataSourceConfig;
import net.htwater.sesame.dms.service.service.SimpleDataSourceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("datasource/config")
@Api(value = "数据源配置",tags = "动态数据源-数据源配置")
public class DataSourceConfigController {
    @Autowired
    private SimpleDataSourceConfigService dataSourceConfigService;

    @GetMapping
    @ApiOperation("获取所有数据源信息")
    public List<InDBDynamicDataSourceConfig> getDatasources(){
       return dataSourceConfigService.findAll();
    }

    @GetMapping("/{id}")
    @ApiOperation("通过id获取数据源信息")
    public InDBDynamicDataSourceConfig getDataSourceById(@PathVariable String id){
        return  dataSourceConfigService.findById(id);
    }
    @PostMapping("/add")
    @ApiOperation("添加数据源")
    public boolean add(InDBDynamicDataSourceConfig config){
        return dataSourceConfigService.add(config);
    }
    @DeleteMapping("id")
    @ApiOperation("删除数据源")
    public boolean remove(@PathVariable String dataSourceId){
        return dataSourceConfigService.remove(dataSourceId);
    }
}
