package net.htwater.sesame.dms.web.dialect;

public class SQLServerDialect extends TypeDialect {

    public SQLServerDialect() {
        this.registerColumnType( "image");
        this.registerColumnType("varbinary");
        this.registerColumnType("text");
        this.registerColumnType( "bit");
        this.registerColumnType("binary");
        this.registerColumnType("tinyint");
        this.registerColumnType("smallint");
        this.registerColumnType( "int");
        this.registerColumnType("char");
        this.registerColumnType("varchar");
        this.registerColumnType( "float");
        this.registerColumnType("double precision");
        this.registerColumnType( "datetime");
        this.registerColumnType( "numeric");
        this.registerFunction("second");
        this.registerFunction("minute");
        this.registerFunction("hour");
        this.registerFunction("extract");
        this.registerFunction("mod");
        this.registerFunction("bit_length");
        this.registerKeyword("top");
        this.registerKeyword("key");

        this.registerFunction("ascii");
        this.registerFunction("char");
        this.registerFunction("len");
        this.registerFunction("lower");
        this.registerFunction("upper");
        this.registerFunction("str");
        this.registerFunction("ltrim");
        this.registerFunction("rtrim");
        this.registerFunction("reverse");
        this.registerFunction("space");
        this.registerFunction("user");
        this.registerFunction("current_timestamp");
        this.registerFunction("current_time");
        this.registerFunction("current_date");
        this.registerFunction("getdate");
        this.registerFunction("getutcdate");
        this.registerFunction("day");
        this.registerFunction("month");
        this.registerFunction("year");
        this.registerFunction("datename");
        this.registerFunction("abs");
        this.registerFunction("sign");
        this.registerFunction("acos");
        this.registerFunction("asin");
        this.registerFunction("atan");
        this.registerFunction("cos");
        this.registerFunction("cot");
        this.registerFunction("exp");
        this.registerFunction("log");
        this.registerFunction("log10");
        this.registerFunction("sin");
        this.registerFunction("sqrt");
        this.registerFunction("tan");
        this.registerFunction("pi");
        this.registerFunction("square");
        this.registerFunction("rand");
        this.registerFunction("radians");
        this.registerFunction("degrees");
        this.registerFunction("round");
        this.registerFunction("ceiling");
        this.registerFunction("floor");
        this.registerFunction("isnull");
        this.registerFunction("concat");
        this.registerFunction("length");
        this.registerFunction("trim");
        this.registerFunction("locate");
    }

}
