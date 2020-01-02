package net.htwater.sesame.dms.web.service;

import net.htwater.sesame.dms.web.domain.TableCache;
import net.htwater.sesame.dms.web.entity.SearchFieldEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author Jokki
 */
public interface TableCacheService {

    List<TableCache> findAllByDatasourceId(String datasourceId);

    TableCache save(TableCache cache);

    Optional<TableCache> findById(String id);

    void deleteById(String id);

    void deleteByDatasourceId(String datasourceId);

    void deleteAll();

    List<TableCache> findAllByIds(List<String> ids);

    List<TableCache> findAllByQ(String q);

    List<SearchFieldEntity> searchFieldsByQ(String q);
}
