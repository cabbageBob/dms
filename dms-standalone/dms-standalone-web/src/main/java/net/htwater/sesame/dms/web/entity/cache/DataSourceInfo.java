package net.htwater.sesame.dms.web.entity.cache;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document(collection = "datasourceInfoCache")
public class DataSourceInfo {
    private String id;
    private String ip;
    private String port;
    @ApiParam("数据库名")
    private String dbname;
    private String name;
    private String username;
    @ApiParam("密码")
    private String password;
    @ApiParam("数据库类型")
    private String dbtype;
    private String describe;
    @ApiParam("连接状态")
    private int status;
}
