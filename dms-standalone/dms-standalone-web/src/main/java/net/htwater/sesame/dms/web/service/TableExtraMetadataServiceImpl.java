package net.htwater.sesame.dms.web.service;

import core.meta.table.TableExtraMetadata;
import net.htwater.sesame.dms.web.domain.TableExtraMetadataDO;
import net.htwater.sesame.dms.web.repository.TableExtraMetadataRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author Jokki
 */
@Service
public class TableExtraMetadataServiceImpl implements TableExtraMetadataService {

    private final TableExtraMetadataRepository repository;

    public TableExtraMetadataServiceImpl(TableExtraMetadataRepository repository) {
        this.repository = repository;
    }

    @Override
    public TableExtraMetadata save(TableExtraMetadata extraMetadata) {
        return convert(repository.save(convert(extraMetadata)));
    }

    @Override
    public TableExtraMetadata findById(String id) {
        return convert(repository.findById(id).orElse(null));
    }

    private TableExtraMetadata convert(TableExtraMetadataDO tableExtraMetadataDO) {
        TableExtraMetadata tableExtraMetadata = new TableExtraMetadata();
        if (tableExtraMetadataDO != null) {
            BeanUtils.copyProperties(tableExtraMetadataDO, tableExtraMetadata);
        }
        return tableExtraMetadata;
    }

    private TableExtraMetadataDO convert(TableExtraMetadata tableExtraMetadataDO) {
        TableExtraMetadataDO tableExtraMetadata = new TableExtraMetadataDO();
        if (tableExtraMetadataDO != null) {
            BeanUtils.copyProperties(tableExtraMetadataDO, tableExtraMetadata);
        }
        return tableExtraMetadata;
    }
}
