package net.htwater.sesame.dms.web.entity;

import lombok.Data;

/**
 * @author Jokki
 */
@Data
public class SearchTableEntity {

    private String id;

    private String datasourceId;

    private String dbName;

    private String ip;

    private String port;

    private String displayDbName;

    private String name;

    private String comment;
}
