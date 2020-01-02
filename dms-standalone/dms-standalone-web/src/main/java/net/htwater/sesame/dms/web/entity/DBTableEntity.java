package net.htwater.sesame.dms.web.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DBTableEntity {
    private String dbname;
    /**
     * 数据库中文名
     */
    private String name;
    private String tbname;
    private String id;
    private String ip;
    private String port;
    /**
     * 数据源描述
     */
    private String describe;
}
