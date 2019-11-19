package net.htwater.sesame.dms.service.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfig;
import net.htwater.sesame.dms.datasource.config.DynamicDataSourceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/datasource")
@Api(tags = "开发人员工具-数据源", value = "数据源")
public class DataSourceController {
    @Autowired
    private DynamicDataSourceConfigRepository<? extends DynamicDataSourceConfig> repository;

    @ApiOperation("获取全部数据源信息")
    public List<? extends DynamicDataSourceConfig> getAllConfig(){
        return repository.findAll();
    }
}
