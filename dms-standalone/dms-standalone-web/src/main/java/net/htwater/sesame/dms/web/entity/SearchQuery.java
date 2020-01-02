package net.htwater.sesame.dms.web.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Jokki
 */
@Getter
@Setter
public class SearchQuery {

    private String q;

    private List<String> ids;
}
