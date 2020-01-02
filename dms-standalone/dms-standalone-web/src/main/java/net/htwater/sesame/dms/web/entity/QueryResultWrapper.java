package net.htwater.sesame.dms.web.entity;

import net.htwater.sesame.dms.web.util.Constants;
import oracle.sql.BLOB;
import oracle.sql.CLOB;
import org.hswebframework.ezorm.core.ObjectWrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueryResultWrapper implements ObjectWrapper<QueryResult> {
    private QueryResult result = new QueryResult();

    private List<Object> temp = new ArrayList<>();

    @Override
    public void setUp(List<String> columns) {
        result.setColumns(columns);
    }

    @Override
    public Class<QueryResult> getType() {
        return QueryResult.class;
    }

    @Override
    public QueryResult newInstance() {
        return result;
    }

    @Override
    public void wrapper(QueryResult instance, int index, String attr, Object value) {
        if (value instanceof Date) {
            temp.add(Constants.DEFAULT_DATE_FORMAT.format((Date) value));
        } else if (value instanceof byte[]) {
            temp.add(String.format("(BLOB) %d bytes", ((byte[]) value).length));
        } else if (value instanceof BLOB) {
            temp.add(String.format("(BLOB) %.2f KB", (float)((BLOB) value).getPrefetchedDataSize() / 1024));
        } else if (value instanceof CLOB) {
            temp.add(String.format("(CLOB) %.2f KB", (float)((CLOB) value).getPrefetchedDataSize() / 1024));
        } else if (value instanceof Boolean) {
            temp.add((boolean) value ? 1 : 0);
        } else {
            temp.add(value);
        }
    }

    @Override
    public boolean done(QueryResult instance) {
        instance.getData().add(new ArrayList<>(temp));
        temp.clear();
        return false;
    }

    public QueryResult getResult() {
        return result;
    }
}
