package net.htwater.sesame.dms.web.service;

import net.htwater.sesame.dms.web.entity.CustomColumnClass;

import java.util.Set;

/**
 * @author Jokki
 */
public interface CustomColumnClassService {

    CustomColumnClass save(CustomColumnClass customColumnClass);

    CustomColumnClass findById(String id);

    Set<CustomColumnClass> findByDatasourceIdAndTable(String datasourceId, String table);
}
