package net.htwater.sesame.dms.web.switcher;

import lombok.extern.slf4j.Slf4j;
import net.htwater.sesame.dms.web.util.ThreadLocalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 默认的动态数据源切换器,基于ThreadLocal,queue
 *
 * @author jokki
 */
@Slf4j
public class DefaultDataSourceSwitcher implements DataSourceSwitcher {

    /**
     * 默认数据源标识
     */
    private static final String DEFAULT_DATASOURCE_ID = DataSourceSwitcher.class.getName() + "_default_";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Deque<String> getUsedHistoryQueue() {
        // 从ThreadLocal中获取一个使用记录
        return ThreadLocalUtils.get(DefaultDataSourceSwitcher.class.getName() + "_queue", LinkedList::new);
    }

    @Override
    public void useLast() {
        // 没有上一次了
        if (getUsedHistoryQueue().isEmpty()) {
            return;
        }
        //移除队尾,则当前的队尾则为上一次的数据源
        getUsedHistoryQueue().removeLast();
        if (logger.isDebugEnabled()) {
            String current = currentDataSourceId();
            if (null != current) {
                logger.debug("try use last data source : {}", currentDataSourceId());
            } else {
                logger.debug("try use last default data source");
            }
        }
    }

    @Override
    public void use(String dataSourceId) {
        //添加到队尾
        getUsedHistoryQueue().addLast(dataSourceId);
        if (logger.isDebugEnabled()) {
            logger.debug("try use data source : {}", dataSourceId);
        }
    }

    @Override
    public void useDefault() {
        getUsedHistoryQueue().addLast(DEFAULT_DATASOURCE_ID);
        if (logger.isDebugEnabled()) {
            logger.debug("try use default data source");
        }
    }

    @Override
    public String currentDataSourceId() {
        if (getUsedHistoryQueue().isEmpty()) {
            return null;
        }

        String activeId = getUsedHistoryQueue().getLast();
        if (DEFAULT_DATASOURCE_ID.equals(activeId)) {
            return null;
        }
        return activeId;
    }

    @Override
    public void reset() {
        getUsedHistoryQueue().clear();
        if (logger.isDebugEnabled()) {
            logger.debug("reset data source used history");
        }
    }
}
