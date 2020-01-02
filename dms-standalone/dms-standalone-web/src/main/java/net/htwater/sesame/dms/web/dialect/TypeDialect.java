package net.htwater.sesame.dms.web.dialect;

import lombok.Getter;

import java.util.*;

@Getter
public abstract class TypeDialect {
    Set<String > TypeNames = new HashSet<>();
    Set<String> FunctionNames =  new HashSet<>();
    private final Set<String> keywords = new HashSet();
    List<Map<String,String>> SqlScripts = new ArrayList<>();

    public TypeDialect(){
        this.registerColumnType("binary");
        this.registerColumnType("tinyint");
        this.registerColumnType("numeric");
        this.registerColumnType( "smallint");
        this.registerColumnType("int");
        this.registerColumnType( "char");
        this.registerColumnType( "varchar");
        this.registerColumnType("float");
        this.registerColumnType("double precision");
        this.registerColumnType("datetime");
        this.registerColumnType("bit");
        this.registerColumnType("boolean");
        this.registerColumnType("integer");
        this.registerColumnType("bigint");
        this.registerColumnType("real");
        this.registerColumnType( "date");
        this.registerColumnType( "time");
        this.registerColumnType("timestamp");
        this.registerColumnType("bit varying");
        this.registerColumnType("blob");
        this.registerColumnType( "clob");
        this.registerColumnType("nchar");
        this.registerColumnType( "nvarchar");
        this.registerColumnType("nclob");
        this.registerFunction("substring");
        this.registerFunction("locate");
        this.registerFunction("trim");
        this.registerFunction("length");
        this.registerFunction("bit_length");
        this.registerFunction("coalesce");
        this.registerFunction("nullif");
        this.registerFunction("abs");
        this.registerFunction("mod");
        this.registerFunction("sqrt");
        this.registerFunction("upper");
        this.registerFunction("lower");
        this.registerFunction("cast");
        this.registerFunction("extract");
        this.registerFunction("second");
        this.registerFunction("minute");
        this.registerFunction("hour");
        this.registerFunction("day");
        this.registerFunction("month");
        this.registerFunction("year");
        this.registerFunction("str");
        this.registerSqlScript("SELECT","SELECT col_name,...\n FROM table_name\n WHERE where_condition\n GROUP BY col_name,... \n HAVING where_condition\n ORDER BY col_name,...");
        this.registerSqlScript("INSERT","INSERT INTO table_name(col_name,...) values(expr,...)");
        this.registerSqlScript("UPDATE","UPDATE table_name SET col_name=expr,... WHERE where_condition");
        this.registerSqlScript("DELETE","DELETE FROM table_name WHERE where_condition ");
    }
    public void registerColumnType(String columnType){
        this.TypeNames.add(columnType);
    }
    public void registerFunction(String function){
        this.FunctionNames.add(function);
    }
    public void registerKeyword(String keyword){this.keywords.add(keyword);}
    private Set<TypeDataBase> supportType = new HashSet<>();
    public void registerSqlScript(String type,String script){
        Map map = new HashMap();
        map.put(type,script);
        this.SqlScripts.add(map);}
}
