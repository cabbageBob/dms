package net.htwater.sesame.dms.web.dialect;

public class SQLServer2008Dialect extends SQLServer2005Dialect {
    public SQLServer2008Dialect(){
        this.registerColumnType("date");
        this.registerColumnType( "time");
        this.registerColumnType("datetime2");
        this.registerColumnType("nvarchar");
        this.registerFunction("current_timestamp");
    }
}
