package net.htwater.sesame.dms.datasource.switcher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Jokki
 */
public class DefaultDataSourceSwitcherTest {

    private DataSourceSwitcher switcher = new DefaultDataSourceSwitcher();

    @Test
    public void testChangeSwitcher() {
        switcher.use("test");
        assertEquals("test", switcher.currentDataSourceId());
        switcher.use("test1");
        assertEquals("test1", switcher.currentDataSourceId());
        switcher.useLast();
        assertEquals("test", switcher.currentDataSourceId());
    }

}