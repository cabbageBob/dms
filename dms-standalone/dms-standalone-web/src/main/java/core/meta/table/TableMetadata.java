package core.meta.table;

import core.meta.BaseMetadata;
import lombok.*;

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

    private TableExtraMetadata extra;

//    private List<IndexMetadata> indexs;
}
