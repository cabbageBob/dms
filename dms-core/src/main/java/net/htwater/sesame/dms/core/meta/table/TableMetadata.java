package net.htwater.sesame.dms.core.meta.table;

import lombok.*;
import net.htwater.sesame.dms.core.meta.BaseMetadata;

import java.util.List;

/**
 * @author Jokki
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableMetadata extends BaseMetadata {
    private String comment;

    private List<ColumnMetadata> columns;
}
