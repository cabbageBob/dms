package core.meta.table;

import lombok.Data;

@Data
public class IndexMetadata {
    /**
     * 索引名称
     */
    private String key_name;

    /**
     * 列名
     */
    private String column_name;

}
