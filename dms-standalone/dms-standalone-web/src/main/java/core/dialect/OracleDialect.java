package core.dialect;

import com.google.common.collect.Lists;
import core.DatabaseType;
import core.meta.table.TableMetadata;
import net.htwater.sesame.dms.web.entity.AlterComment;
import net.htwater.sesame.dms.web.entity.BatchSql;
import net.htwater.sesame.dms.web.entity.ColumnClass;
import net.htwater.sesame.dms.web.entity.IndexAndColumnClass;
import net.htwater.sesame.dms.web.entity.altertable.AlterTable;
import net.htwater.sesame.dms.web.util.ScriptUtil;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;

import java.util.List;
import java.util.Map;

public class OracleDialect extends AbstractDialect {
    private static final  String TABLE_META_SQL = "select cols.column_name as \"name\"" +
            ",cols.table_name as \"tableName\"" +
            ",cols.data_type as \"dataType\"" +
            ",cols.data_length as \"dataLength\"" +
            ",cols.data_precision as \"precision\"" +
            ",cols.data_scale as \"scale\"" +
            ",cols.data_default as \"defaultValue\""+
            ",acc.comments as \"comment\"" +
            ",COALESCE(p.position,0) AS \"isPrimary\""+
            ",case when cols.nullable='Y' then 0 else 1 end as \"notNull\"" +
            ",cols.column_id from user_tab_columns cols " +
            "left join all_col_comments acc on acc.column_name=cols.column_name and acc.table_name=cols.table_name " +
            "left join (select  * from   user_cons_columns " +
            "where   constraint_name = (select constraint_name from user_constraints " +
            "where   table_name = #{table}  and constraint_type ='P')) p " +
            "ON cols.column_name=p.column_name AND cols.table_name=p.table_name "+
            "where cols.table_name=upper(#{table}) " +
            "order by cols.column_id ";

    private static final  String TABLE_COMMENT_SQL = "select comments as \"comment\" from user_tab_comments " +
            "where table_type='TABLE' and table_name=upper(#{table})";

    private static final String SET_TABLE_COMMENT_SQL ="COMMENT ON TABLE \"${dbname}\".\"${table}\" IS #{comment}";

    private static final String SET_FIELD_COMMENT_SQL ="COMMENT ON COLUMN \"${dbname}\".\"${table}\".\"${column}\" IS #{comment}";

    private static final  String ALL_TABLE_SQL = "select table_name as \"name\" from user_tab_comments where table_type='TABLE'";

    private static final String TOP_COLUMNS = "select * from ${table} where rownum < 101";

    private static final String THREADS_CONNECTED_SQL ="select count(*) conns from v$session WHERE SCHEMANAME=#{dbname}";

    private static final String DBSIZE_SQL ="select round(sum(sum(bytes)/1024/1024)) as \"memory\" from dba_data_files group by tablespace_name";

    private static final String GET_ROWS_SQL="select SUM(num_rows) \"num_rows\" from user_tables ";
    public OracleDialect(SqlExecutor sqlExecutor) {
        super(sqlExecutor, DatabaseType.ORACLE);
    }

    @Override
    public String getSelectTableColumnsSql() {
        return TABLE_META_SQL;
    }

    @Override
    public String getSelectTableMetaSql() {
        return TABLE_COMMENT_SQL;
    }

    @Override
    public String getSelectAllTableSql() {
        return ALL_TABLE_SQL;
    }

    @Override
    public String showTable() {
        return TOP_COLUMNS;
    }

    @Override
    public String getBackupSql() {
        return null;
    }

    @Override
    public String doPage(String sql, int pageIndex, int pageSize) {
        return "SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM (" + sql + ") row_ )" +
                "WHERE rownum_ <= " + pageSize * pageIndex + " AND rownum_ > " + pageSize * (pageIndex-1);
    }

    @Override
    public String getThreadsConnectedSql() {
        return THREADS_CONNECTED_SQL;
    }

    @Override
    public String getMemorySql() {
        return DBSIZE_SQL;
    }

    @Override
    public String getRowsSql() {
        return GET_ROWS_SQL;
    }

    @Override
    public String alterTableScript(TableMetadata tableMetadata, AlterTable alterTable) {
        return ScriptUtil.oracleAlterTableScript(tableMetadata,alterTable);
    }

    @Override
    public String openQuote() {
        return "\"";
    }

    @Override
    public String closeQuote() {
        return "\"";
    }

    @Override
    public String getEnumValuesSql() {
        return null;
    }

    @Override
    public String setTableCommentSql(AlterComment alterComment) {
        StringBuilder builder = new StringBuilder("comment on table \"");
        builder.append(alterComment.getTable());
        builder.append(" is '");
        builder.append(alterComment.getComment());
        builder.append("'");
        return builder.toString();
    }

    @Override
    public String setFieldCommentSql(AlterComment alterComment) {
        StringBuilder builder = new StringBuilder("comment on table \"");
        builder.append(alterComment.getTable());
        builder.append("\".\"");
        builder.append(alterComment.getColumn());
        builder.append("\" is '");
        builder.append(alterComment.getComment());
        builder.append("'");
        return builder.toString();
    }

    @Override
    public BatchSql getAddOrUpdateSql(String table, Map<String, Integer> fields, Map<String, Integer> whereFields,
                                      Map<String, ColumnClass> columnTypeMap) {
        StringBuilder mergeSql = new StringBuilder("MERGE INTO");
        StringBuilder onSql = new StringBuilder(" ON (");
        StringBuilder insertSql = new StringBuilder(" WHEN NOT MATCHED THEN INSERT (");
        StringBuilder insertValueSql = new StringBuilder(" VALUES( ");
        StringBuilder updateSql = new StringBuilder(" WHEN MATCHED THEN UPDATE set");
        int i = 0, j = 0;
        List<IndexAndColumnClass> indexList = Lists.newArrayList();
        List<IndexAndColumnClass> valueIndexList = Lists.newArrayList();
        List<IndexAndColumnClass> whereIndexList = Lists.newArrayList();
        mergeSql.append(quote(table)).append(" T1 USING (SELECT ");
        for (Map.Entry<String, Integer> entry : whereFields.entrySet()) {
            String field = entry.getKey();
            mergeSql.append("? AS ").append(quote(field));
            onSql.append(" T1.").append(quote(field)).append("=T2.").append(quote(field));
            if (j != whereFields.size()) {
                mergeSql.append(",");
                onSql.append(" and ");
            }
            ColumnClass columnClass = columnTypeMap.get(field);
            if (columnClass == null) {
                throw new IllegalArgumentException("字段不存在: " + field);
            }
            IndexAndColumnClass indexAndColumnClass =
                    new IndexAndColumnClass(entry.getValue(), columnClass);
            whereIndexList.add(indexAndColumnClass);
            j++;
        }
        for (Map.Entry<String, Integer> entry : fields.entrySet()) {
            String field = entry.getKey();
            ColumnClass columnClass = columnTypeMap.get(field);
            if (columnClass == null) {
                throw new IllegalArgumentException("字段不存在: " + field);
            }
            insertSql.append(quote(field));
            insertValueSql.append("? ");
            updateSql.append(quote(field)).append("=? ");
            if (i != fields.size() - 1) {
                insertSql.append(",");
                insertValueSql.append(",");
                updateSql.append(",");
            }
            IndexAndColumnClass indexAndColumnClass =
                    new IndexAndColumnClass(entry.getValue(), columnClass);
            valueIndexList.add(indexAndColumnClass);
            i++;
        }
        mergeSql.append(" from dual)T2 ");
        onSql.append(")");
        insertSql.append(")");
        insertValueSql.append(")");
        String sql = mergeSql.toString() + onSql.toString() + updateSql.toString() +
                insertSql.toString() + insertValueSql.toString();
        indexList.addAll(whereIndexList);
        indexList.addAll(valueIndexList);
        indexList.addAll(valueIndexList);
        return new BatchSql(sql, indexList);
    }
}
