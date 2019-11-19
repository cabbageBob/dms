package net.htwater.sesame.dms.datasource;


import net.htwater.sesame.dms.datasource.exception.DataSourceNotFoundException;
import net.htwater.sesame.dms.datasource.switcher.DataSourceSwitcher;
import net.htwater.sesame.dms.datasource.switcher.DefaultDataSourceSwitcher;

/**
 * 用于操作动态数据源
 * @author Jokki
 */
public class DataSourceHolder {

    private DataSourceHolder() {
    }

    private static final DataSourceSwitcher DEFAULT_SWITCHER = new DefaultDataSourceSwitcher();
    /**
     * 动态数据源服务
     */
    static volatile DynamicDataSourceService dynamicDataSourceService;

    private static void checkDynamicDataSourceReady() {
        if (dynamicDataSourceService == null) {
            throw new UnsupportedOperationException("dataSourceService not ready");
        }
    }

    /**
     * @return 动态数据源切换器
     */
    public static DataSourceSwitcher switcher() {
        return DEFAULT_SWITCHER;
    }

    /**
     * 根据指定的数据源id获取动态数据源
     *
     * @param dataSourceId 数据源id
     * @return 动态数据源
     * @throws DataSourceNotFoundException 如果数据源不存在将抛出此异常
     */
    public static DynamicDataSource dataSource(String dataSourceId) {
        checkDynamicDataSourceReady();
        return dynamicDataSourceService.getDataSource(dataSourceId);
    }

    /**
     * @return 当前使用的数据源
     */
    public static DynamicDataSource currentDataSource() {
        String id = DEFAULT_SWITCHER.currentDataSourceId();
        checkDynamicDataSourceReady();
        return dynamicDataSourceService.getDataSource(id);
    }


    /**
     * 判断指定id的数据源是否存在
     *
     * @param id 数据源id {@link DynamicDataSource#getId()}
     * @return 数据源是否存在
     */
    public static boolean existing(String id) {
        try {
            checkDynamicDataSourceReady();
            return dynamicDataSourceService.getDataSource(id) != null;
        } catch (DataSourceNotFoundException e) {
            return false;
        }
    }
}
