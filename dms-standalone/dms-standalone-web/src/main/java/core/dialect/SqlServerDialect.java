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

public class SqlServerDialect extends AbstractDialect {
    private static String TABLE_META_SQL = "SELECT \n" +
            "c.name as [name],\n" +
            "t.name as [dataType],\n" +
            "c.length as [length],\n" +
            "c.xscale as [scale],\n" +
            "c.xprec as [precision],\n" +
            "c.cdefault as [defaultValue],\n"+
            "ISNULL(pkey.ordinal_position, 0) AS[isPrimary], "+
            "case when c.isnullable=1 then 0 else  1 end as [notNull],\n" +
            "cast(p.value as varchar(500)) as [comment]\n" +
            "FROM syscolumns c\n" +
            "inner join  systypes t on c.xusertype = t.xusertype \n" +
            "left join sys.extended_properties p on c.id=p.major_id and c.colid=p.minor_id\n" +
            "LEFT JOIN (SELECT TABLE_NAME,COLUMN_NAME,ordinal_position \n" +
            "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE \n" +
            "WHERE TABLE_NAME= #{table}) pkey\n" +
            "ON c.name=pkey.COLUMN_name "+
            "WHERE c.id = object_id(#{table})";

    private static String TABLE_COMMENT_SQL = "select cast(p.value as varchar(500)) as [comment] from sys.extended_properties p " +
            " where p.major_id=object_id(#{table}) and p.minor_id=0";

    private static final String SET_TABLE_COMMENT_SQL ="BEGIN TRANSACTION\n" +
            "IF ((SELECT COUNT(*) FROM ::fn_listextendedproperty('MS_Description',\n" +
            "'SCHEMA', N'dbo',\n" +
            "'TABLE', N#{table}, NULL, NULL)) > 0)\n" +
            "  EXEC sp_updateextendedproperty\n" +
            "'MS_Description', N#{comment},\n" +
            "'SCHEMA', N'dbo',\n" +
            "'TABLE', N#{table}\n" +
            "ELSE\n" +
            "  EXEC sp_addextendedproperty\n" +
            "'MS_Description', N#{comment},\n" +
            "'SCHEMA', N'dbo',\n" +
            "'TABLE', N#{table}\n" +
            "COMMIT TRANSACTION";
    private static final String SET_FIELD_COMMENT_SQL ="BEGIN TRANSACTION\n" +
            "IF ((SELECT COUNT(*) FROM ::fn_listextendedproperty('MS_Description',\n" +
            "'SCHEMA', N'dbo',\n" +
            "'TABLE', N#{table}, " +
            "'COLUMN', N#{column})) > 0)\n" +
            "  EXEC sp_updateextendedproperty\n" +
            "'MS_Description', N#{comment},\n" +
            "'SCHEMA', N'dbo',\n" +
            "'TABLE', N#{table},\n" +
            "'COLUMN', N#{column}"+
            "ELSE\n" +
            "  EXEC sp_addextendedproperty\n" +
            "'MS_Description', N#{comment},\n" +
            "'SCHEMA', N'dbo',\n" +
            "'TABLE', N#{table},\n" +
            "'COLUMN', N#{column}"+
            "COMMIT TRANSACTION";

    private static final String TOP_COLUMNS ="select top "+COLUMNS_NUM+" * from ${table}";

    private static final String BACKUP_SQL = "backup database ${database} to disk=#{path}";


    private static final String THREADS_CONNECTED_SQL ="SELECT COUNT(1) conns  FROM [Master].[dbo].[SYSPROCESSES] WHERE [DBID] IN " +
            " (SELECT [DBID] FROM [Master].[dbo].[SYSDATABASES] WHERE NAME=#{dbname})";

    private static final String DBSIZE_SQL ="select top 1 convert(int,size)*(8192/1024)/1024 memory from sysfiles order by fileid";

    private static final String GET_ROWS_SQL="SELECT SUM(rows) as num_rows FROM sys.sysindexes i inner join sysObjects o on (o.id=i.id AND o.xType='U') where indid<2";

    private static final String CONNECT_DBSIZE_ROWS_SQL="SELECT a.connected_num,b.memory,c.num_rows FROM " +
            "(SELECT COUNT(1) AS connected_num FROM [Master].[dbo].[SYSPROCESSES] WHERE [DBID] IN " +
            " (SELECT [DBID] FROM [Master].[dbo].[SYSDATABASES] WHERE NAME=#{dbname})) AS a " +
            " JOIN (SELECT SUM(8 * dpages)/1024  AS memory FROM sys.sysindexes) b ON 1=1" +
            " JOIN (SELECT SUM(rows) as num_rows FROM sys.sysindexes) c on 1=1";

    public SqlServerDialect(SqlExecutor sqlExecutor) {
        super(sqlExecutor,DatabaseType.SQLSERVER );
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
        return "select name from sysobjects where xtype='U'";
    }

    @Override
    public String showTable() {
        return TOP_COLUMNS;
    }

    @Override
    public String getBackupSql() {
        return BACKUP_SQL;
    }

    @Override
    public String doPage(String sql,int pageIndex, int pageSize) {
        return "select top "+pageSize+"* from (SELECT ROW_number() OVER(ORDER BY CURRENT_TIMESTAMP) rownum_temp, * FROM (" +
                sql+") temp1 ) temp2 where rownum_temp >" +pageSize*(pageIndex-1);
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
        return ScriptUtil.sqlServerAlterTableScript(tableMetadata,alterTable);
    }

    @Override
    public String openQuote() {
        return "[";
    }

    @Override
    public String closeQuote() {
        return "]";
    }

    @Override
    public String getEnumValuesSql() {
        return null;
    }

    @Override
    public String setTableCommentSql(AlterComment alterComment) {
        StringBuilder builder = new StringBuilder("BEGIN TRANSACTION\nIF ((SELECT COUNT(*) FROM ::fn_listextendedproperty" +
                "('MS_Description',\n'SCHEMA', N'dbo',\n'TABLE', N'");
        builder.append(alterComment.getTable());
        builder.append("', NULL, NULL)) > 0)\nEXEC sp_updateextendedproperty\n'MS_Description', N'");
        builder.append(alterComment.getComment());
        builder.append("',\n'SCHEMA', N'dbo',\n'TABLE', N'");
        builder.append(alterComment.getTable());
        builder.append("'\nELSE\n");
        builder.append("EXEC sp_addextendedproperty\n'MS_Description', N'");
        builder.append(alterComment.getComment());
        builder.append("',\n'SCHEMA', N'dbo',\n'TABLE', N'");
        builder.append(alterComment.getTable());
        builder.append("'\nCOMMIT TRANSACTION");
        return builder.toString();
    }

    @Override
    public String setFieldCommentSql(AlterComment alterComment) {
        StringBuilder builder = new StringBuilder("BEGIN TRANSACTION\nIF ((SELECT COUNT(*) FROM ::fn_listextendedproperty" +
                "('MS_Description',\n'SCHEMA', N'dbo',\n'TABLE', N'");
        builder.append(alterComment.getTable());
        builder.append("', 'COLUMN', N'");
        builder.append(alterComment.getColumn());
        builder.append("'");
        builder.append(")) > 0)\nEXEC sp_updateextendedproperty\n'MS_Description', N'");
        builder.append(alterComment.getComment());
        builder.append("',\n'SCHEMA', N'dbo',\n'TABLE', N'");
        builder.append(alterComment.getTable());
        builder.append("',\n'COLUMN', N'");
        builder.append(alterComment.getColumn());
        builder.append("'\nELSE\n");
        builder.append("EXEC sp_addextendedproperty\n'MS_Description', N'");
        builder.append(alterComment.getComment());
        builder.append("',\n'SCHEMA', N'dbo',\n'TABLE', N'");
        builder.append(alterComment.getTable());
        builder.append("',\n'COLUMN', N'");
        builder.append(alterComment.getColumn());
        builder.append("'\nCOMMIT TRANSACTION");
        return builder.toString();
    }

    @Override
    public BatchSql getAddOrUpdateSql(String table, Map<String, Integer> fields, Map<String, Integer> whereFields,
                                      Map<String, ColumnClass> columnTypeMap) {
        StringBuilder ifSql = new StringBuilder("if not exists (select ");
        StringBuilder insertSql = new StringBuilder(" INSERT INTO ");
        StringBuilder insertValueSql = new StringBuilder(" VALUES( ");
        StringBuilder updateSql = new StringBuilder(" else UPDATE ");
        StringBuilder whereSql = new StringBuilder(" WHERE ");
        insertSql.append(quote(table)).append("(");
        updateSql.append(quote(table)).append(" set ");
        int i = 0, j = 0;
        List<IndexAndColumnClass> indexList = Lists.newArrayList();
        List<IndexAndColumnClass> valueIndexList = Lists.newArrayList();
        List<IndexAndColumnClass> whereIndexList = Lists.newArrayList();
        for (Map.Entry<String, Integer> entry : fields.entrySet()) {
            String field = entry.getKey();
            ColumnClass columnClass = columnTypeMap.get(field);
            if (columnClass == null) {
                throw new IllegalArgumentException("字段不存在: " + field);
            }
            ifSql.append(quote(field));
            insertSql.append(quote(field));
            insertValueSql.append("? ");
            updateSql.append(quote(field)).append("=? ");
            if (i != fields.size() - 1) {
                ifSql.append(",");
                insertSql.append(",");
                insertValueSql.append(",");
                updateSql.append(",");
            }
            IndexAndColumnClass indexAndColumnClass =
                    new IndexAndColumnClass(entry.getValue(), columnClass);
            valueIndexList.add(indexAndColumnClass);
            i++;
        }
        for (Map.Entry<String, Integer> entry : whereFields.entrySet()) {
            String field = entry.getKey();
            whereSql.append(quote(field)).append("=? ");
            if (j != whereFields.size()) {
                whereSql.append(" and ");
            }
            ColumnClass columnClass = columnTypeMap.get(field);
            IndexAndColumnClass indexAndColumnClass =
                    new IndexAndColumnClass(entry.getValue(), columnClass);
            whereIndexList.add(indexAndColumnClass);
            j++;
        }
        ifSql.append(" from ").append(quote(table)).append(whereSql.toString()).append(")");
        insertSql.append(")");
        insertValueSql.append(")");
        updateSql.append(whereSql.toString());
        String sql = ifSql.toString() + insertSql.toString() + insertValueSql.toString() +
                updateSql.toString();
        indexList.addAll(whereIndexList);
        indexList.addAll(valueIndexList);
        indexList.addAll(valueIndexList);
        indexList.addAll(whereIndexList);
        return new BatchSql(sql, indexList);
    }

}
