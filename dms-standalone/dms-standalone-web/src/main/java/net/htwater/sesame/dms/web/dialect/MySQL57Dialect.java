package net.htwater.sesame.dms.web.dialect;

public class MySQL57Dialect extends MySQL55Dialect {
    MySQL57Dialect(){
        this.registerColumnType("json");
    }
}
