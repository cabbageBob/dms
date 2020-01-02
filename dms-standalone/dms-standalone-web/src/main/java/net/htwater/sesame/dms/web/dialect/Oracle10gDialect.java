package net.htwater.sesame.dms.web.dialect;

public class Oracle10gDialect extends Oracle9iDialect {
    Oracle10gDialect(){
        this.registerColumnType("10");
    }
}
