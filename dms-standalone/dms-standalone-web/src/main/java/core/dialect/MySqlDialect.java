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

public class MySqlDialect extends AbstractDialect {
    private static final String TABLE_META_SQL = " select " +
            "column_name as `name`, " +
            "data_type as `dataType`, " +
            "character_maximum_length as `length`, " +
            "numeric_precision as `precision`, " +
            "numeric_scale as `scale`, " +
            "column_default as `defaultValue`,"+
            "column_comment as `comment`, " +
            "CASE WHEN column_key ='PRI' THEN 1 ELSE 0 END AS 'isPrimary', "+
            "case when is_nullable='YES' then 0 else 1 end as 'notNull' " +
            "from information_schema.columns where table_schema=database() and table_name=#{table}";

    private  static final String TABLE_COMMENT_SQL = " select " +
            " table_comment as `comment` " +
            " from information_schema.tables where table_schema=database() and table_name=#{table}";

    private static final String SET_TABLE_COMMENT_SQL ="alter table `${dbname}`.`${table}`  comment #{comment}" ;

    private static final String SET_FIELD_COMMENT_SQL = "alter table `${dbname}`.`${table}`  modify column ${column} ${type} " +
            "comment #{comment}";

    private static final String ALL_TABLE_SQL = "select table_name as `name` from information_schema.`TABLES` " +
            "where table_schema=database()";

    private static final String TOP_COLUMNS = "select * from ${table} limit 0,100";

    private static final String THREADS_CONNECTED_SQL ="SELECT COUNT(1) conns FROM information_schema.`PROCESSLIST` " +
            "where DB=#{dbname}";

    private static final String DBSIZE_SQL ="select round(sum(data_length/1024/1024)+sum(INDEX_LENGTH/1024/1024),2) as memory" +
            " from information_schema.tables where table_schema=#{dbname}";

    private static final String GET_ROWS_SQL="SELECT  SUM(table_rows) num_rows" +
            " from information_schema.`TABLES` where table_schema=#{dbname}";

    private static final String GET_ENUM_VALUES_SQL="select column_type FROM information_schema.`COLUMNS` WHERE " +
            "table_name= #{table} and column_name =#{fieldName}";

//    private static final String TABLE_INDEX_SQL = "show index from ${table}";


    public MySqlDialect(SqlExecutor sqlExecutor) {
        super(sqlExecutor,DatabaseType.MYSQL);
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
        return sql+" limit " + pageSize * (pageIndex-1) + "," + pageSize;
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
        return ScriptUtil.mySqlAlterTableScript(tableMetadata,alterTable);
    }

    @Override
    public String openQuote() {
        return "`";
    }

    @Override
    public String closeQuote() {
        return "`";
    }

    @Override
    public String getEnumValuesSql() {
        return GET_ENUM_VALUES_SQL;
    }

    @Override
    public String setTableCommentSql(AlterComment alterComment) {
        StringBuilder sql = new StringBuilder("alter table `");
        sql.append(alterComment.getTable());
        sql.append("` comment '");
        sql.append(alterComment.getComment());
        sql.append("'");
        return sql.toString();
    }

    @Override
    public String setFieldCommentSql(AlterComment alterComment) {
        String genre = alterComment.getGenre();
        int length = alterComment.getLength();
        int scale = alterComment.getScale();
        String type;
        if (length==0){
                type="";
        }else {
            if (scale==0){
                type="("+length+")";
            }else {
                type = "("+length+","+scale+")";
            }
        }
        StringBuilder sql = new StringBuilder("alter table `");
        sql.append(alterComment.getTable());
        sql.append("` modify column ");
        sql.append(alterComment.getColumn());
        sql.append(" ");
        sql.append(genre);
        sql.append(type);
        sql.append(" comment '");
        sql.append(alterComment.getComment());
        sql.append("'");
        return sql.toString();
    }

    @Override
    public BatchSql getAddOrUpdateSql(String table, Map<String, Integer> fields, Map<String, Integer> whereFields,
                                      Map<String, ColumnClass> columnTypeMap) {
        StringBuilder preSql = new StringBuilder("INSERT INTO ");
        StringBuilder postSql = new StringBuilder("VALUES (");
        StringBuilder updateSql = new StringBuilder("UPDATE");
        preSql.append(quote(table)).append("(");
        int i = 0;
        List<IndexAndColumnClass> indexList = Lists.newArrayList();
        List<IndexAndColumnClass> updateIndexList = Lists.newArrayList();
        for (Map.Entry<String, Integer> entry : fields.entrySet()) {
            String field = entry.getKey();
            ColumnClass columnClass = columnTypeMap.get(field);
            if (columnClass == null) {
                throw new IllegalArgumentException("字段不存在: " + field);
            }
            IndexAndColumnClass indexAndColumnClass =
                    new IndexAndColumnClass(entry.getValue(), columnClass);
            preSql.append(quote(field));
            postSql.append("?");
            updateSql.append(quote(field))
                    .append("=?");
            if (i != fields.size() - 1) {
                preSql.append(",");
                postSql.append(",");
                updateSql.append(",");
            }
            indexList.add(indexAndColumnClass);
            updateIndexList.add(indexAndColumnClass);
            i++;
        }
        indexList.addAll(updateIndexList);
        preSql.append(")");
        postSql.append(")");
        String sql = preSql.toString() + postSql.toString() + "ON DUPLICATE KEY " + updateSql.toString();
        return new BatchSql(sql, indexList);
    }


}
