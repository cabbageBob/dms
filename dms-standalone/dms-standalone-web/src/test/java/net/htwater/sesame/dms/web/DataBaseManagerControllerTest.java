package net.htwater.sesame.dms.web;

import core.meta.ObjectType;
import core.sql.SqlExecuteResult;
import net.htwater.sesame.dms.web.controller.DataBaseManagerController;
import net.htwater.sesame.dms.web.controller.DataImportExportController;
import net.htwater.sesame.dms.web.controller.DataSourceConfigController;
import net.htwater.sesame.dms.web.dataSource.DataSourceHolder;
import net.htwater.sesame.dms.web.entity.BaseEntity;
import net.htwater.sesame.dms.web.entity.QueryResult;
import net.htwater.sesame.dms.web.entity.SqlExecuteQuery;
import net.htwater.sesame.dms.web.entity.altertable.AlterColumn;
import net.htwater.sesame.dms.web.entity.altertable.AlterTable;
import net.htwater.sesame.dms.web.file.ExcleParser;
import net.htwater.sesame.dms.web.service.DataImportExportService;
import net.htwater.sesame.dms.web.service.SimpleDataSourceConfigService;
import net.htwater.sesame.dms.web.service.SimpleDatabaseManagerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DataBaseManagerControllerTest {
    @Autowired
    DataBaseManagerController dataBaseManagerController;
    @Autowired
    DataImportExportController dataImportExportController;
    @Autowired
    ExcleParser excleParser;
    @Autowired
    DataImportExportService dataImportExportService;
    @Autowired
    SimpleDatabaseManagerService simpleDatabaseManagerService;
    @Autowired
    DataSourceConfigController dataSourceConfigController;
    @Autowired
    SimpleDataSourceConfigService simpleDataSourceConfigService;
    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    public void testAddDataSource(){
//        dataSourceConfigController.add(new InDBDynamicDataSourceConfig("3","192.168.100.1",
//                "1433","db_shanhong","htpms",
//                "htwater1,nbsl","MSSQL","山洪数据库")
//        );
       /* System.out.println(    dataSourceConfigController.add(new InDBDynamicDataSourceConfig("localhost",
                "3306","test2","测试2","root",
                "123456","MYSQL","测试数据库")
        ));*/
    }
    @Test
    public void testGetMetaData(){
//        List<Map<String,Map<ObjectType, List<String>>>> metaData = dataBaseManagerController.getMetaData();
//        System.out.println(metaData.size());
//        for (Map<String,Map<ObjectType, List<String>>> map : metaData){
//            System.out.println(map);
//        }
        Map<ObjectType, List> metaData =
                dataBaseManagerController.getMetaData("3");
        List list = metaData.get(ObjectType.TABLE);

    }

//    @Test
//    public void testShowTable(){
//        QueryResult queryResult = dataBaseManagerController.showTable("4","sys_user");
//        System.out.println(queryResult);
//    }

    @Test
    public void testExecute(){
        String selectSql = "select * from sys_user ";
        String insertSql = "insert into sys_user (username,password) values('chenqi','chenqi')";
        String updateSql = "update sys_user set username='dongba',password='dongba' where id=5";
        String delSql = "delete from sys_user where id=5";
        SqlExecuteResult list = dataBaseManagerController
                .execute(selectSql,new SqlExecuteQuery("4",1,5));
        /*for (SqlExecuteResult sqlExecuteResult : list){
            System.out.println(sqlExecuteResult.getResult());
        }*/
    }
    @Test
    public void testExcle(){
        String path = "C:\\Users\\10415\\Desktop\\业务\\API设计-雨情20180716.xlsx";
        try {
            InputStream is = new FileInputStream(new File(path));
            List<List<Object>> lists = ExcleParser.readXlsx(is, 0, null);
            for (List<Object> list:lists){
                System.out.println(list.size());
                System.out.println(list);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testImport(){
        /*Map<String,String> fieldMap = new HashMap<>();
        fieldMap.put("用户名","username");
        fieldMap.put("密码","password");
        fieldMap.put("角色","role");
        DataImportResponse result = dataImportExportController.importData(fieldMap,"2","2","sys_user","EXCEL", "");
        System.out.println(result.getResultCount());*/
    }

    @Test
    public void testBackup(){
        dataBaseManagerController.backup("root","3+/XuHWB5FQpr7JhTw0Xlw==","G:\\业务\\dbBackup",
                "test2","test2","4");
    }

    @Test
    public void testelasticsearch(){
//        simpleDatabaseManagerService.importDBTable();
        //System.out.println(simpleDatabaseManagerService.fuzzySearchDbTable("test2"));
//        simpleDatabaseManagerService.importDBTableById("4");
    }
    @Test
    public void testGetVersion(){
        DataSourceHolder.switcher().use("3");
//        System.out.println(JdbcUtil.getDBMajorVersion("SQLSERVER","192.168.100.1",
//                "1433","db_shanhong","htpms","htwater1,nbsl"));
//        System.out.println(simpleDatabaseManagerService.getColumnTypes("192.168.100.1",
//                "1433","db_shanhong","htpms","MSLEC5vXlJmuzpm1Q3VoGA=="));
//        System.out.println(simpleDatabaseManagerService.getColumnTypes("172.16.35.51",
//                "1521","orcl","USER_NJ","htwater"));

//        System.out.println(JdbcUtil.getDBMajorVersion("ORACLE","172.16.35.51",
//                "1521","orcl","USER_NJ","htwater"));
//        System.out.println(JdbcUtil.getDBVersion("MSSQL",
//                "192.168.100.1","1433","db_shanhong","htpms","htwater1,nbsl"));
    }
    @Test
    public void testQueryTplContent(){
        DataSourceHolder.switcher().use("3");
        System.out.println(simpleDatabaseManagerService.queryTplContent());
    }
    @Test
    public void testDataSourceInfoCache(){
//        List<DataSourceInfo> result = simpleDataSourceConfigService.getDataSourceInfo();
//        mongoTemplate.dropCollection(DataSourceInfo.class);
//        mongoTemplate.insertAll(result);

//        simpleDataSourceConfigService.getInfo();
//        System.out.println(mongoTemplate.findAll(DataSourceInfo.class));
    }
    @Test
    public void testAlterTableSqlScript(){
        DataSourceHolder.switcher().use("3");
        AlterColumn alterColumn = new AlterColumn();
        alterColumn.setName("id");
        alterColumn.setOldName("ids");
        alterColumn.setComment("用户idss");
        alterColumn.setDataType("decimal");
        alterColumn.setLength(13);
        alterColumn.setScale(2);
        alterColumn.setIsPrimary(1);
//        alterColumn.setIsPrimary(1);

        AlterColumn alterColumn2 = new AlterColumn();
        alterColumn2.setName("names");
        alterColumn2.setOldName("name");
        alterColumn2.setComment("用户名ss");
        alterColumn2.setDataType("varchar");
        alterColumn2.setNotNull(true);
        alterColumn2.setLength(13);
        alterColumn2.setIsPrimary(3);
        List<AlterColumn> list = new ArrayList<>();
        list.add(alterColumn);
        list.add(alterColumn2);

        AlterTable alterTable = new AlterTable();
        alterTable.setColumns(list);
        alterTable.setTableName("test4");
        alterTable.setOldTableName("test4");
        alterTable.setComment("test4444");
        BaseEntity script =simpleDatabaseManagerService.alterTableScript(alterTable);
        System.out.println(script.getResult());
    }
}
