package net.htwater.sesame.dms.web.repository;

import net.htwater.sesame.dms.web.entity.TableColumnCount;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Jokki
 */
@Repository
public interface TableCacheRepositoryCustom {
    List<TableColumnCount> tableColumnCount();
}
