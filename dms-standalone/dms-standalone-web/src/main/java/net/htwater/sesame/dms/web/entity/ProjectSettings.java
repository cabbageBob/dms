package net.htwater.sesame.dms.web.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "project")
@Getter
@Setter
public class ProjectSettings {
    private String uploadPath;
    private String docPath;

    private int importSize = 1000;

    private int exportSize = 1000;

    private int poolSize = 10;


}
