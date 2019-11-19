package net.htwater.sesame.dms.core.dialect;


public class SqlServerDialect extends AbstractDialect {
    private static String TABLE_META_SQL = "SELECT \n" +
            "c.name as [name],\n" +
            "t.name as [dataType],\n" +
            "c.length as [length],\n" +
            "c.xscale as [scale],\n" +
            "c.xprec as [precision],\n" +
            "case when c.isnullable=1 then 0 else  1 end as [notNull],\n" +
            "cast(p.value as varchar(500)) as [comment]\n" +
            "FROM syscolumns c\n" +
            "inner join  systypes t on c.xusertype = t.xusertype \n" +
            "left join sys.extended_properties p on c.id=p.major_id and c.colid=p.minor_id\n" +
            "WHERE c.id = object_id(#{table})";

    private static String TABLE_COMMENT_SQL = "select cast(p.value as varchar(500)) as [comment] from sys.extended_properties p " +
            " where p.major_id=object_id(#{table}) and p.minor_id=0";


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
//    public static final SqlServerDialect INSTANCE = new SqlServerDialect();
//    private SqlServerDialect(){}
//    @Override
//    public String allTablesSql(String dbname) {
//        return "select x.name AS tbname,isnull(cast(y.[value] as varchar(5000)),'-') AS label, "
//                +"cast(((select COUNT(1)+0.0 from sys.extended_properties a left join sys.sysobjects b on a.major_id=b.id where class=1 and a.name='MS_Description' and b.name=x.name)*100 "
//                +"/ "
//                +"( "
//                +"(select COUNT(1)+1 from syscolumns a "
//                +"inner join sysobjects d on a.id=d.id and d.xtype='U' and d.name<>'dtproperties' where d.name=x.name) "
//                +")) as numeric(5,0)) sta "
//                +"from sys.tables x left join sys.extended_properties y on (x.object_id = y.major_id AND y.minor_id = 0 and y.name='MS_Description') "
//                +"where x.name<>'dtproperties'";
//    }
//
//    @Override
//    public String tableFieldsSql(String dbname, String tbname) {
//        return "SELECT a.name field, "
//                +"COLUMNPROPERTY( a.id,a.name,'IsIdentity') isidentity, "
//                +"(case when (SELECT count(*) FROM sysobjects  "
//                +"WHERE (name in (SELECT name FROM sysindexes  "
//                +"WHERE (id = a.id) AND (indid in  "
//                +"(SELECT indid FROM sysindexkeys  "
//                +"WHERE (id = a.id) AND (colid in  "
//                +"(SELECT colid FROM syscolumns WHERE (id = a.id) AND (name = a.name)))))))  "
//                +"AND (xtype = 'PK'))>0 then 1 else 0 end) iskey, "
//                +"b.name type,a.length,  "
//                +"a.isnullable,  "
//                +"e.text defaultvalue,"
//                +"isnull(cast(g.[value] as varchar(5000)),'-') label "
//                +"FROM  syscolumns a "
//                +"left join systypes b on a.xtype=b.xusertype  "
//                +"inner join sysobjects d on a.id=d.id and d.xtype='U' and d.name<>'dtproperties' "
//                +"left join syscomments e on a.cdefault=e.id  "
//                +"left join sys.extended_properties g on a.id=g.major_id AND a.colid=g.minor_id "
//                +"left join sys.extended_properties f on d.id=f.class and f.minor_id=0 "
//                +"where b.name is not null "
//                +"and d.name= '"
//                +tbname
//                +"' "
//                +"order by a.id,a.colorder";
//    }
//
//    @Override
//    public String dbCountSql(String dbname) {
//        return null;
//    }
//
//
//    @Override
//    public String alterTableCommentSql(String tbname, String remark) {
//        return "IF ((SELECT COUNT(*) from sys.fn_listextendedproperty('MS_Description',  "
//                +"'SCHEMA', N'dbo',  "
//                +"'TABLE', N'"+tbname+"',  "
//                +"NULL, NULL)) > 0)  "
//                +"EXEC sp_updateextendedproperty @name = N'MS_Description', @value = '"+remark+"' "
//                +", @level0type = 'SCHEMA', @level0name = N'dbo' "
//                +", @level1type = 'TABLE', @level1name = N'"+tbname+"' "
//                +"ELSE "
//                +"EXEC sp_addextendedproperty @name = N'MS_Description', @value = '"+remark+"' "
//                +", @level0type = 'SCHEMA', @level0name = N'dbo' "
//                +", @level1type = 'TABLE', @level1name = N'"+tbname+"'";
//    }
//
//    @Override
//    public String alterFieldCommentSql(String tbname, String field, String remark, String type, String length) {
//        return "IF ((SELECT COUNT(*) from sys.fn_listextendedproperty('MS_Description',  "
//                +"'SCHEMA', N'dbo',  "
//                +"'TABLE', N'"+tbname+"',  "
//                +"'COLUMN', N'"+field+"')) > 0)  "
//                +"EXEC sp_updateextendedproperty @name = N'MS_Description', @value = '"+remark+"' "
//                +", @level0type = 'SCHEMA', @level0name = N'dbo' "
//                +", @level1type = 'TABLE', @level1name = N'"+tbname+"' "
//                +", @level2type = 'COLUMN', @level2name = N'"+field+"' "
//                +"ELSE "
//                +"EXEC sp_addextendedproperty @name = N'MS_Description', @value = '"+remark+"' "
//                +", @level0type = 'SCHEMA', @level0name = N'dbo' "
//                +", @level1type = 'TABLE', @level1name = N'"+tbname+"' "
//                +", @level2type = 'COLUMN', @level2name = N'"+field+"'";
//
//    }
//
//    @Override
//    public String driverClass() {
//        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//    }
//
//    @Override
//    public String jdbcUrl() {
//        return "jdbc:sqlserver://%s:%s;databaseName=%s";
//    }
//
//    @Override
//    public String defaultPort() {
//        return "1433";
//    }
}
