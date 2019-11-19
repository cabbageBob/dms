package net.htwater.sesame.dms.core.meta;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 数据库的基础元数据
 * @author Jokki
 */
@Getter
@Setter
public abstract class BaseMetadata implements Serializable {

    protected String name;

    protected ObjectType type;
}
