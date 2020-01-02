package net.htwater.sesame.dms.web.entity;

import lombok.Getter;
import lombok.Setter;
import net.htwater.sesame.dms.web.file.FileType;

import javax.validation.constraints.NotNull;

/**
 * @author Jokki
 */
@Getter
@Setter
public class ExportDataQuery {
    @NotNull
    private String sql;

    @NotNull
    private FileType type;

    private String table;
}
