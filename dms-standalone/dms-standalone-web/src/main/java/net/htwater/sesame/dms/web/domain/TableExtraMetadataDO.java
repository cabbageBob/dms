package net.htwater.sesame.dms.web.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Jokki
 */
@Data
@Document("table_extra_metadata")
public class TableExtraMetadataDO {

    @Id
    private String id;

    /**
     * 数据维护人员
     */
    private String manager;

    /**
     * 来源
     */
    private String source;

    /**
     * 更新频率
     */
    private String frequency;

    /**
     * 采集方式
     */
    private String collectMode;

    /**
     * 更新时间
     */
    private String updateTm;
}
