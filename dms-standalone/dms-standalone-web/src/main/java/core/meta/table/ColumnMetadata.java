package core.meta.table;

import lombok.Data;
import net.htwater.sesame.dms.web.entity.ColumnClass;
import net.htwater.sesame.dms.web.entity.CustomColumnClass;

import java.util.Set;

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
     * 是否为主键
     */
    private int isPrimary;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 分类类型
     */
    private ColumnClass typeClassify;

    /**
     * 枚举类型的值
     */
    private Set<CustomColumnClass.Value> enumValue;
}
