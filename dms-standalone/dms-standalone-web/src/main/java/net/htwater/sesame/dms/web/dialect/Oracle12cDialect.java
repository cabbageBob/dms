package net.htwater.sesame.dms.web.dialect;

public class Oracle12cDialect extends Oracle10gDialect {
    Oracle12cDialect(){
        this.registerColumnType("12");
    }
}
