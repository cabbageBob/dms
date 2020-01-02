package net.htwater.sesame.dms.web.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author Jokki
 */
@Data
@Document("datasource_status")
public class DataSourceStatus {

    private int status;

    @Id
    private String id;

    private Date updateAt;
}
