package net.htwater.sesame.dms.service.service;

import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.core.DatabaseManagerService;
import net.htwater.sesame.dms.core.dialect.Dialect;
import net.htwater.sesame.dms.core.meta.BaseMetadata;
import net.htwater.sesame.dms.core.meta.ObjectType;
import net.htwater.sesame.dms.core.meta.table.TableMetadata;
import net.htwater.sesame.dms.core.sql.SqlExecuteRequest;
import net.htwater.sesame.dms.core.sql.SqlExecuteResult;
import net.htwater.sesame.dms.core.sql.SqlExecutor;
import net.htwater.sesame.dms.datasource.DataSourceHolder;
import net.htwater.sesame.dms.datasource.DatabaseType;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SimpleDatabaseManagerService implements DatabaseManagerService{
    private SqlExecutor sqlExecutor;
    private Map<DatabaseType, Map<ObjectType, Dialect<? extends BaseMetadata>>> parserRepo = new HashMap<>();
    public Map<ObjectType, List<? extends BaseMetadata>> getMetas() {
        return parserRepo
                .computeIfAbsent(DataSourceHolder.currentDataSource().getType(), t -> new HashMap<>())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    try {
                        return entry.getValue().parseAll();
                    } catch (SQLException e) {
                        log.error("parse meta {} error", entry.getKey(), e);
                        return new ArrayList<>();
                    }
                }));;
    }
    public List<TableMetadata> parseAll() throws SQLException {
        String dsId = DataSourceHolder.switcher().currentDataSourceId();
        return sqlExecutor.list(getSelectAllTableSql())
                .parallelStream()
                .map(map -> map.get("name"))
                .map(String::valueOf)
                .map(tableName -> {
                    try {
                        DataSourceHolder.switcher().use(dsId);
                        return this.parse(tableName);
                    } finally {
                        DataSourceHolder.switcher().reset();
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    @Override
    public List<SqlExecuteResult> execute(SqlExecuteRequest request) {
        return null;
    }

    public <M extends BaseMetadata> void registerMetaDataParser(DatabaseType databaseType, ObjectType objectType, Dialect<M> parser) {
        parserRepo.computeIfAbsent(databaseType, t -> new HashMap<>())
                .put(objectType, parser);
    }
}
