package net.htwater.sesame.dms.web.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import core.dialect.Dialect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.htwater.sesame.dms.web.util.DataManagerUtil;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jokki
 */
@Getter
@Setter
public class DataImportRequest {

    @NotNull
    private Map<String, SourceField> fieldMap = Maps.newLinkedHashMap();

    @NotNull
    private String fileId;

    @NotNull
    private String table;

    @NotNull
    private String fileType;

    @NotNull
    private String timeFormat;

    @NotNull
    private ImportStrategy strategy;

    @Min(1)
    private int dataRow = 2;

    @Min(-1)
    private int lastDataRow = -1;

    public enum ImportStrategy {
        /**
         * 追加
         */
        ADD {
            @Override
            public BatchSqlArgs build(Dialect dialect, Map<String, ColumnClass> columnTypeMap, List<List<Object>> dataList,
                                      DataImportRequest request) {
                StringBuilder preSql = new StringBuilder("insert into ");
                StringBuilder postSql = new StringBuilder(" values(");
                preSql.append(quote(dialect, request.getTable()));
                preSql.append("(");
                int i = 0;
                Set<Map.Entry<String, DataImportRequest.SourceField>> entries = request.getFieldMap().entrySet();
                List<IndexAndColumnClass> indexList = Lists.newArrayList();
                for (Map.Entry<String, DataImportRequest.SourceField> entry : entries) {
                    ColumnClass columnClass = columnTypeMap.get(entry.getKey());
                    if (columnClass != ColumnClass.BINARY) {
                        preSql.append(quote(dialect, entry.getKey()));
                        postSql.append("?");
                        if (columnClass == null) {
                            throw new IllegalArgumentException("字段不存在: " + entry.getKey());
                        }
                        IndexAndColumnClass indexAndColumnClass =
                                new IndexAndColumnClass(entry.getValue().getIndex(), columnClass);
                        if (i != entries.size() - 1) {
                            preSql.append(",");
                            postSql.append(",");
                        }
                        indexList.add(indexAndColumnClass);
                        i++;
                    }
                }
                preSql.append(")");
                postSql.append(")");
                return new BatchSqlArgs(preSql.toString() + postSql.toString(),
                        DataManagerUtil.buildColumns(indexList, dataList, request.getTimeFormat()));
            }
        },
        /**
         * 更新
         */
        UPDATE {
            @Override
            public BatchSqlArgs build(Dialect dialect, Map<String, ColumnClass> columnTypeMap, List<List<Object>> dataList,
                                      DataImportRequest request) {
                StringBuilder preSql = new StringBuilder("update ");
                StringBuilder whereSql = new StringBuilder(" where ");
                preSql.append(quote(dialect, request.getTable()));
                preSql.append(" set ");
                //需要导入的字段的下标
                int i = 0;
                Set<Map.Entry<String, SourceField>> entries = request.getFieldMap().entrySet();
                List<IndexAndColumnClass> indexList = Lists.newArrayList();
                List<IndexAndColumnClass> whereIndexList = Lists.newArrayList();
                for (Map.Entry<String, DataImportRequest.SourceField> entry : entries) {
                    ColumnClass columnClass = columnTypeMap.get(entry.getKey());
                    if (columnClass != ColumnClass.BINARY) {
                        preSql.append(quote(dialect, entry.getKey()))
                                .append("=?");
                        if (columnClass == null) {
                            throw new IllegalArgumentException("字段不存在: " + entry.getKey());
                        }
                        IndexAndColumnClass indexAndColumnClass =
                                new IndexAndColumnClass(entry.getValue().getIndex(), columnClass);
                        if (entry.getValue().isKey()) {
                            whereSql.append(quote(dialect, entry.getKey()))
                                    .append("=")
                                    .append("? ")
                                    .append("and ");
                            whereIndexList.add(indexAndColumnClass);
                        }
                        indexList.add(indexAndColumnClass);
                        if (i != entries.size() - 1) {
                            preSql.append(",");
                        }
                        i++;
                    }
                }
                if (CollectionUtils.isEmpty(whereIndexList)) {
                    throw new IllegalArgumentException("主键不存在");
                }
                indexList.addAll(whereIndexList);
                return new BatchSqlArgs(preSql.toString() + whereSql.substring(0, whereSql.length() - 4),
                        DataManagerUtil.buildColumns(indexList, dataList, request.getTimeFormat()));
            }
        },
        /**
         * 追加或更新
         */
        ADDORUPDATE {
            @Override
            public BatchSqlArgs build(Dialect dialect, Map<String, ColumnClass> columnTypeMap, List<List<Object>> dataList,
                                      DataImportRequest request) {
                Map<String, Integer> fields = Maps.newLinkedHashMap();
                Map<String, Integer> whereFields = Maps.newLinkedHashMap();
                request.getFieldMap().forEach((name, field) -> {
                    if (columnTypeMap.get(name) != ColumnClass.BINARY) {
                        fields.put(name, field.getIndex());
                        if (field.isKey()) {
                            whereFields.put(name, field.getIndex());
                        }
                    }
                });
                if (CollectionUtils.isEmpty(whereFields)) {
                    throw new IllegalArgumentException("主键不存在或者不合法");
                }
                BatchSql batchSql = dialect.getAddOrUpdateSql(request.getTable(), fields, whereFields, columnTypeMap);

                return new BatchSqlArgs(batchSql.getSql(),
                        DataManagerUtil.buildColumns(batchSql.getObjects(), dataList, request.getTimeFormat()));
            }
        };

        public abstract BatchSqlArgs build(Dialect dialect, Map<String, ColumnClass> columnTypeMap,
                                           List<List<Object>> dataList, DataImportRequest request);

        private static String quote(Dialect dialect, String name){
            return dialect.openQuote() + name + dialect.closeQuote();
        }
    }

    @Getter
    @Setter
    public static class SourceField {
        private int index;

        private boolean key;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class BatchSqlArgs {
        private String sql;

        private List<List<Object>> objects;
    }
}
