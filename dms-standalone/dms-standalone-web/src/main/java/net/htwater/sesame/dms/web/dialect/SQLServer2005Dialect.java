package net.htwater.sesame.dms.web.dialect;

public class SQLServer2005Dialect  extends SQLServerDialect{
    public SQLServer2005Dialect(){
        this.registerColumnType("bigint");
        this.registerColumnType( "bit");
        this.registerFunction("row_number");
    }
}
