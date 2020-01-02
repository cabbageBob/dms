package net.htwater.sesame.dms.web.service;

import net.htwater.sesame.dms.web.domain.CustomColumnClassDO;
import net.htwater.sesame.dms.web.entity.CustomColumnClass;
import net.htwater.sesame.dms.web.repository.CustomColumnClassRepository;
import net.htwater.sesame.dms.web.util.DataManagerUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jokki
 */
@Service
public class CustomColumnClassServiceImpl implements CustomColumnClassService {

    private final CustomColumnClassRepository customColumnClassRepository;

    private final TableCacheService tableCacheService;

    @Autowired
    public CustomColumnClassServiceImpl(CustomColumnClassRepository customColumnClassRepository,
                                        TableCacheService tableCacheService) {
        this.customColumnClassRepository = customColumnClassRepository;
        this.tableCacheService = tableCacheService;
    }

    @Override
    public CustomColumnClass save(CustomColumnClass customColumnClass) {
        CustomColumnClass columnClass = convert(customColumnClassRepository.save(convert(customColumnClass)));
        tableCacheService.deleteById(DataManagerUtil.formatId(columnClass.getDatasourceId(), columnClass.getTable()));
        return columnClass;
    }

    @Override
    public CustomColumnClass findById(String id) {
        return convert(customColumnClassRepository.findById(id).orElse(null));
    }

    @Override
    public Set<CustomColumnClass> findByDatasourceIdAndTable(String datasourceId, String table) {
        return customColumnClassRepository.findByDatasourceIdAndAndTable(datasourceId, table)
                .stream().map(this::convert)
                .collect(Collectors.toSet());
    }

    private CustomColumnClassDO convert(CustomColumnClass tableExtraMetadataDO) {
        CustomColumnClassDO tableExtraMetadata = new CustomColumnClassDO();
        if (tableExtraMetadataDO != null) {
            BeanUtils.copyProperties(tableExtraMetadataDO, tableExtraMetadata);
        }
        return tableExtraMetadata;
    }

    private CustomColumnClass convert(CustomColumnClassDO tableExtraMetadataDO) {
        CustomColumnClass tableExtraMetadata = new CustomColumnClass();
        if (tableExtraMetadataDO != null) {
            BeanUtils.copyProperties(tableExtraMetadataDO, tableExtraMetadata);
        }
        return tableExtraMetadata;
    }
}
