package net.htwater.sesame.dms.service.entity;

import lombok.Data;

@Data
public class DataSourceConfigEntity  extends BaseEntity{
    String instance_id;

    String instance_name;

    String ip;

    String port;

    String account;

    String password;

    String dbtype;

}
