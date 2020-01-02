package core.meta;

import lombok.Getter;
import lombok.Setter;

/**
 * 数据库的基础元数据
 * @author Jokki
 */
@Getter
@Setter
public abstract class BaseMetadata {

    protected String name;

    protected ObjectType type;
}
