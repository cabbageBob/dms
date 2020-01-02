package net.htwater.sesame.dms.web.util;

import com.google.common.collect.Lists;
import net.htwater.sesame.dms.web.entity.ColumnClass;
import net.htwater.sesame.dms.web.entity.IndexAndColumnClass;
import org.elasticsearch.common.Strings;
import org.hswebframework.utils.DateTimeUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jokki
 */
public class DataManagerUtil {

    private DataManagerUtil() {
    }

    private static final String SPLIT_LABEL = ":";


    /**
     * 构建ID
     * @param datasourceId 数据源ID
     * @param table 表名
     * @return 构建的id
     */
    public static String formatId(String datasourceId, String table) {
        return datasourceId + SPLIT_LABEL + table;
    }


    /**
     * @param datasourceId 数据源ID
     * @param table 表名
     * @param column 字段名
     * @return 构建的id
     */
    public static String formatId(String datasourceId, String table, String column) {
        return datasourceId + SPLIT_LABEL + table + SPLIT_LABEL + column;
    }

    public static List<List<Object>> buildColumns(List<IndexAndColumnClass> indexList, List<List<Object>> dataList,
                                                  String timeFormat) {
        List<List<Object>> objects = Lists.newArrayList();
        for (List<Object> data : dataList) {
            objects.add(indexList.stream()
                    .map(indexAndColumnClass -> {
                        Object object = data.get(indexAndColumnClass.getIndex());
                        if(indexAndColumnClass.getColumnClass() == ColumnClass.DATE) {
                            object = object == null ? null : DateTimeUtils.formatDateString(object.toString(),
                                    timeFormat);
                        } else if (indexAndColumnClass.getColumnClass() == ColumnClass.NUMBER) {
                            if (object != null && Strings.isEmpty(object.toString())) {
                                object = null;
                            }
                        }
                        return object;
                    })
                    .collect(Collectors.toList()));
        }
        return objects;
    }
}
