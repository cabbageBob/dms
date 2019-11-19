package net.htwater.sesame.dms.service.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.htwater.sesame.dms.core.meta.BaseMetadata;
import net.htwater.sesame.dms.core.meta.ObjectType;
import net.htwater.sesame.dms.datasource.DataSourceHolder;
import net.htwater.sesame.dms.service.service.SimpleDatabaseManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/database/manager")
@Api(tags = "开发人员工具-数据库维护", value = "数据库维护")
public class DataBaseManagerController {

    @Autowired
    private SimpleDatabaseManagerService databaseManagerService;

    @GetMapping("/metas/{datasourceId}")
    @ApiOperation("获取指定数据源的元数据")
    public Map<ObjectType, List<? extends BaseMetadata>> getMetaData(@PathVariable @ApiParam("数据源ID") String datasourceId){
        DataSourceHolder.switcher().use(datasourceId);
        return databaseManagerService.getMetas();
    }
}
