package net.htwater.sesame.dms.web;

import core.dialect.MySqlDialect;
import core.dialect.OracleDialect;
import core.dialect.SqlServerDialect;
import net.htwater.sesame.dms.web.dataSource.DefaultJdbcExecutor;
import net.htwater.sesame.dms.web.domain.DataSourceConfig;
import net.htwater.sesame.dms.web.entity.AlterComment;
import net.htwater.sesame.dms.web.entity.UpdatedDataSourceConfig;
import net.htwater.sesame.dms.web.repository.DataImportExportRepository;
import net.htwater.sesame.dms.web.service.DataSourceConfigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatasourceConfigServiceTest {

    @Autowired
    private DataSourceConfigService dataSourceConfigService;

    @Autowired
    DataImportExportRepository dataImportExportRepository;


    @Test
    public void testInsertDatasourceConfig(){
        List<DataSourceConfig> dataSourceConfigs = dataSourceConfigService.findAll();
        List<UpdatedDataSourceConfig> updatedDataSourceConfigs = dataSourceConfigs.stream()
                .map(dataSourceConfig -> {
                    UpdatedDataSourceConfig config = new UpdatedDataSourceConfig();
                    BeanUtils.copyProperties(dataSourceConfig,config);
                    System.out.println(config);
                    return config;
        }).collect(Collectors.toList());

        for (int i=0;i<=updatedDataSourceConfigs.size()-1;i++){
            if (i<2){
                updatedDataSourceConfigs.get(i).setDatabaseGenre("BASE");
            }
            else if (i<4&2<=i){
                updatedDataSourceConfigs.get(i).setDatabaseGenre("SUBJECT");
            }
            else if (i>=4&i<6){
                updatedDataSourceConfigs.get(i).setDatabaseGenre("PARTAKE");
            }else {
                updatedDataSourceConfigs.get(i).setDatabaseGenre("MEDIA");
            }
            dataSourceConfigService.update(updatedDataSourceConfigs.get(i));
        }
    }
    @Test
    public void testRecord(){
        //上周一开始时
        LocalDateTime lastMonday = LocalDate.now().plusWeeks(-1).with(DayOfWeek.MONDAY).atStartOfDay();
        long lastMondayMilli =  toMilli(lastMonday);
        //上周日结束时,同时也是本周一开始时
        LocalDateTime lastSunday =LocalDate.now().plusWeeks(-1).with(DayOfWeek.MONDAY).plusDays(8).atStartOfDay();
        long lastSundayMilli = toMilli(lastSunday);
        LocalDateTime now = LocalDateTime.now();
        long nowMilli = toMilli(now);
//        RecordEntity recordEntity1 = new RecordEntity(null,5673,nowMilli,RecordType.SELECT);
//        RecordEntity recordEntity2 = new RecordEntity(null,97,nowMilli,RecordType.UPDATE);
//        List<RecordEntity> list = new ArrayList<>();
//        list.add(recordEntity1);
//        list.add(recordEntity2);
//        dataImportExportRepository.insert(list);
        System.out.println(dataImportExportRepository.findRecordEntityByTimeBetween(lastMondayMilli,nowMilli).size());
    }
    private long toMilli(LocalDateTime time){
        return LocalDateTime.from(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @Test
    public void testUpdateComment(){
        AlterComment alterComment = new AlterComment();
        alterComment.setColumn("test");
        alterComment.setComment("123");
        alterComment.setGenre("date");
        alterComment.setLength(0);
        alterComment.setScale(0);
        alterComment.setTable("sys_user");
        MySqlDialect mySQLDialect = new MySqlDialect(new DefaultJdbcExecutor());
        System.out.println(mySQLDialect.setTableCommentSql(alterComment));
        System.out.println(mySQLDialect.setFieldCommentSql(alterComment));
        SqlServerDialect sqlServerDialect = new SqlServerDialect(new DefaultJdbcExecutor());
        OracleDialect oracleDialect = new OracleDialect(new DefaultJdbcExecutor());
        System.out.println(sqlServerDialect.setTableCommentSql(alterComment));
        System.out.println();
        System.out.println(sqlServerDialect.setFieldCommentSql(alterComment));
        System.out.println();
        System.out.println(oracleDialect.setTableCommentSql(alterComment));
        System.out.println();
        System.out.println(oracleDialect.setFieldCommentSql(alterComment));
    }
}
