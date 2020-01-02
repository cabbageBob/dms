package net.htwater.sesame.dms.web.entity.altertable;

import lombok.Data;

@Data
public class AlterColumn {
    /**
     * 列名
     */
    private String name;

    private String oldName;

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
     * 是否为主键
     */
    private int isPrimary;

}
