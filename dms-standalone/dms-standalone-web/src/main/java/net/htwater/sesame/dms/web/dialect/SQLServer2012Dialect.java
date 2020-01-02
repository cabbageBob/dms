package net.htwater.sesame.dms.web.dialect;

public class SQLServer2012Dialect extends SQLServer2008Dialect {
    public SQLServer2012Dialect(){
        this.registerColumnType( "2012");
    }

}
