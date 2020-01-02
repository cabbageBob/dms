package net.htwater.sesame.dms.web.controller;

import com.google.common.collect.Sets;
import core.meta.table.TableExtraMetadata;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.htwater.sesame.dms.web.entity.ColumnClass;
import net.htwater.sesame.dms.web.entity.CustomColumnClass;
import net.htwater.sesame.dms.web.service.CustomColumnClassService;
import net.htwater.sesame.dms.web.service.TableExtraMetadataService;
import net.htwater.sesame.dms.web.util.DataManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author Jokki
 */
@RestController
@Api(tags = "数据自定义管理")
@RequestMapping("/database/manager")
public class CustomDataManagerController {

    private final TableExtraMetadataService tableExtraMetadataService;

    private final CustomColumnClassService customColumnClassService;

    @Autowired
    public CustomDataManagerController(TableExtraMetadataService tableExtraMetadataService,
                                       CustomColumnClassService customColumnClassService) {
        this.tableExtraMetadataService = tableExtraMetadataService;
        this.customColumnClassService = customColumnClassService;
    }

    @GetMapping(value = "/metas/extra/{datasourceId}/{table}")
    @ApiOperation(value = "获取表自定义的元数据")
    public TableExtraMetadata getExtraMetadata(@PathVariable String datasourceId, @PathVariable String table) {
        return tableExtraMetadataService.findById(DataManagerUtil.formatId(datasourceId, table));
    }

    @PostMapping(value = "metas/extra/{datasourceId}/{table}")
    @ApiOperation(value = "保存表自定义的元数据")
    public TableExtraMetadata saveExtraMetadata(@PathVariable String datasourceId, @PathVariable String table,
                                               @NotNull @RequestBody TableExtraMetadata metadata) {
        metadata.setId(DataManagerUtil.formatId(datasourceId, table));
        return tableExtraMetadataService.save(metadata);
    }

    @GetMapping(value = "columnclass")
    public Set<ColumnClass> getAllClasses() {
        return Sets.newHashSet(ColumnClass.values());
    }

    @GetMapping(value = "columnclass/{datasourceId}/{table}/{column}")
    @ApiOperation(value = "获取字段的自定义类型")
    public CustomColumnClass getColumnClass(@PathVariable String datasourceId, @PathVariable String table,
                                            @PathVariable String column) {
        return customColumnClassService.findById(DataManagerUtil.formatId(datasourceId,
                table,
                column));
    }

    @PostMapping(value = "columnclass/{datasourceId}/{table}/{column}")
    @ApiOperation(value = "保存字段的自定义类型")
    public CustomColumnClass saveColumnClass(@PathVariable String datasourceId, @PathVariable String table,
                                             @PathVariable String column,
                                             @NotNull @RequestBody CustomColumnClass customColumnClass) {
        if (customColumnClass.getColumnClass() != ColumnClass.ENUM) {
            customColumnClass.setValues(null);
        }
        if (StringUtils.isEmpty(customColumnClass.getColumn())) {
            customColumnClass.setColumn(column);
        }
        if (StringUtils.isEmpty(customColumnClass.getTable())) {
            customColumnClass.setTable(table);
        }
        if (StringUtils.isEmpty(customColumnClass.getDatasourceId())) {
            customColumnClass.setDatasourceId(datasourceId);
        }
        customColumnClass.setId(DataManagerUtil.formatId(datasourceId, table, column));
        return customColumnClassService.save(customColumnClass);
    }

}
