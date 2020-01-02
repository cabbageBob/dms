package core.meta.table;

import lombok.Data;

/**
 * @author Jokki
 */
@Data
public class TableExtraMetadata {

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
