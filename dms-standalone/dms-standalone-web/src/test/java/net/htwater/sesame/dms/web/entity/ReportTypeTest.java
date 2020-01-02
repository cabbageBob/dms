package net.htwater.sesame.dms.web.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jokki
 */
public class ReportTypeTest {
    @Test
    public void endTime() throws Exception {
        String date = ReportType.MONTH.endTime(new ReportType.TimeNumber(2020, 2));
        Assert.assertEquals(date, "2020-02-29");
        date = ReportType.SEASON.endTime(new ReportType.TimeNumber(2019, 1));
        Assert.assertEquals(date, "2019-03-31");
    }

}