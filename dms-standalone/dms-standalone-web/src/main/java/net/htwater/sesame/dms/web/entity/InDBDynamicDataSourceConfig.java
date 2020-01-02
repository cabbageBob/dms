package net.htwater.sesame.dms.web.entity;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InDBDynamicDataSourceConfig extends DynamicDataSourceConfig {

    private static final long serialVersionUID = -1194234628206484357L;
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

    public InDBDynamicDataSourceConfig(String ip, String port, String dbname, String name, String username, String password, String dbtype, String describe) {
        this.ip = ip;
        this.port = port;
        this.dbname = dbname;
        this.name = name;
        this.username = username;
        this.password = password;
        this.dbtype = dbtype;
        this.describe = describe;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof InDBDynamicDataSourceConfig) {
            return o.hashCode() == hashCode();
        }
        return super.equals(o);
    }
}
