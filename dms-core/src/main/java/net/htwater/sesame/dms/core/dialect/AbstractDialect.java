package net.htwater.sesame.dms.core.dialect;


/**
 * 特定关系型数据库的SQL方言
 * @author Jokki
 */
public abstract class AbstractDialect implements Dialect {


    public abstract String getSelectTableColumnsSql();

    public abstract String getSelectTableMetaSql();

    public abstract String getSelectAllTableSql();



}
