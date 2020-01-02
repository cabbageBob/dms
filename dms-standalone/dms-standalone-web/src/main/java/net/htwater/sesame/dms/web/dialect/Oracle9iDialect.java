package net.htwater.sesame.dms.web.dialect;

public class Oracle9iDialect  extends Oracle8iDialect{
    Oracle9iDialect(){
        this.registerColumnType("char");
        this.registerColumnType("varchar2");
        this.registerColumnType("long");
        this.registerColumnType( "date");
        this.registerColumnType("timestamp");
    }
}
