package net.htwater.sesame.dms.web.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Jokki
 */
@Data
@Document(collection = "datasource_config")
public class DataSourceConfig {

    @Id
    private String id;
    private String ip;
    private String port;
    private String dbname;
    private String name;
    private String username;
    private String password;
    private String dbtype;
    private String describe;
    private String databaseGenre;
}
