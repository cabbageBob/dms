package net.htwater.sesame.dms.web.service;

import core.meta.table.TableExtraMetadata;

/**
 * @author Jokki
 */
public interface TableExtraMetadataService {

    TableExtraMetadata save(TableExtraMetadata extraMetadata);

    TableExtraMetadata findById(String id);
}
