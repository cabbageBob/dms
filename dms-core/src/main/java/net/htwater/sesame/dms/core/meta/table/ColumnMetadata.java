package net.htwater.sesame.dms.core.meta.table;

import lombok.Data;

import java.sql.JDBCType;

/**
 * @author Jokki
 */
@Data
public class ColumnMetadata {

    /**
     * 列名
     */
    private String name;

    /**
     * 备注
     */
    private String comment;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 长度
     */
    private int length;

    /**
     * 精度
     */
    private int precision;

    /**
     * 小数点位数
     */
    private int scale;

    /**
     * 是否不能为空
     */
    private boolean notNull;

    /**
     * JDBCType
     */
    private JDBCType jdbcType;
}
