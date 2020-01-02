package net.htwater.sesame.dms.web.entity;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author Jokki
 */
@Getter
@Setter
public class ExportDictionaryRequest {

    @NotNull
    private String dbName;

    @NotNull
    private Set<String> tables;
}
