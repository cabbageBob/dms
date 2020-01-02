package net.htwater.sesame.dms.web.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class QueryResult {
    private List<String> columns;

    private List<List<Object>> data=new ArrayList<>();
}
