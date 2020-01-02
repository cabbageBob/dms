package net.htwater.sesame.dms.web.dialect;

public class MySQL8Dialect  extends MySQL57Dialect{
    MySQL8Dialect(){
        this.registerKeyword("CUME_DIST");
        this.registerKeyword("DENSE_RANK");
        this.registerKeyword("EMPTY");
        this.registerKeyword("EXCEPT");
        this.registerKeyword("FIRST_VALUE");
        this.registerKeyword("GROUPS");
        this.registerKeyword("JSON_TABLE");
        this.registerKeyword("LAG");
        this.registerKeyword("LAST_VALUE");
        this.registerKeyword("LEAD");
        this.registerKeyword("NTH_VALUE");
        this.registerKeyword("NTILE");
        this.registerKeyword("PERSIST");
        this.registerKeyword("PERCENT_RANK");
        this.registerKeyword("PERSIST_ONLY");
        this.registerKeyword("RANK");
        this.registerKeyword("ROW_NUMBER");
    }
}
