package core.dialect;

import core.DatabaseType;
import core.meta.table.TableMetadata;
import net.htwater.sesame.dms.web.entity.AlterComment;
import net.htwater.sesame.dms.web.entity.BatchSql;
import net.htwater.sesame.dms.web.entity.ColumnClass;
import net.htwater.sesame.dms.web.entity.altertable.AlterTable;

import java.util.Map;

/**
 * 特定关系型数据库的SQL方言
 * @author Jokki
 */
public interface Dialect {

    boolean isSupport(DatabaseType type);

    Dialect get();

    String getSelectTableColumnsSql();

    String getSelectTableMetaSql();

    String getSelectAllTableSql();

//    String getSelectTableIndexSql();

    /**
     * 获取数据库前100条数据
     * @return
     */
    String showTable();

    String getBackupSql();

    /**
     * 分页查询
     * @param sql
     * @param pageIndex
     * @param pageSize
     * @return
     */
    String doPage(String sql,int pageIndex, int pageSize);

    /**
     * 获取当前数据库连接数
     * @return
     */
    String getThreadsConnectedSql();

    //数据库大小

    String getMemorySql();

    /**
     * 数据总行数
     * @return
     */
    String getRowsSql();

    /**
     * 修改表操作脚本
     * @param tableMetadata
     * @param alterTable
     * @return
     */
    String alterTableScript(TableMetadata tableMetadata, AlterTable alterTable);

    /**
     * 特殊字段符号
     * @return
     */
    String openQuote();
    String closeQuote();

    String getEnumValuesSql();

    String setTableCommentSql(AlterComment alterComment);

    String setFieldCommentSql(AlterComment alterComment);

    /**
     * 构建插入或更新的sql
     * @param table 表名
     * @param fields 插入或更新的字段
     * @param whereFields 条件的字段
     * @param columnTypeMap 字段分类
     * @return 构建的sql
     */
    BatchSql getAddOrUpdateSql(String table, Map<String, Integer> fields, Map<String, Integer> whereFields,
                               Map<String, ColumnClass> columnTypeMap);

}
