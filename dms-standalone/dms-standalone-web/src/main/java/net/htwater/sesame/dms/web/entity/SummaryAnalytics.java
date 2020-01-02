package net.htwater.sesame.dms.web.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Jokki
 */
@Getter
@Setter
public class SummaryAnalytics {
    private double allSize;

    private long allCount;

    private long allConns;

    private int datasourceNum;

    private int allTableNum;

    private int allFieldNum;

    private List<AnalyticDetail> details;

    @Getter
    @Setter
    public static class AnalyticDetail {
        private String id;

        private String dbName;

        private String name;

        private String ip;

        private String port;

        private int tableNum;

        private int fieldNum;

        private double size;

        private long count;

        private String databaseGenre;

        private int conns;
    }
}
