package net.htwater.sesame.dms.web.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DateTimeException;
import java.time.Month;
import java.time.Year;

/**
 * @author Jokki
 */
public enum  ReportType {
    /**
     * 年报
     */
    YEAR {
        @Override
        public String endTime(TimeNumber timeNumber) {
            validYear(timeNumber.getYear());
            return timeNumber.getYear() + "-12-31";
        }
    },
    /**
     * 季报
     */
    SEASON {
        @Override
        public String endTime(TimeNumber timeNumber) {
            validYear(timeNumber.getYear());
            switch (timeNumber.getNumber()) {
                case 1:
                    return timeNumber.getYear() + "-03-31";
                case 2:
                    return timeNumber.getYear() + "-06-30";
                case 3:
                    return timeNumber.getYear() + "-09-30";
                case 4:
                    return timeNumber.getYear() + "-12-31";
                default:
                    throw new DateTimeException("Invalid value for season: " + timeNumber);
            }
        }

    },
    /**
     * 月报
     */
    MONTH {
        @Override
        public String endTime(TimeNumber timeNumber) {
            validYear(timeNumber.getYear());
            boolean isLeap = Year.of(timeNumber.getYear()).isLeap();
            return timeNumber.getYear() + "-" +
                    (timeNumber.getNumber() >= 10 ? "" + timeNumber.getNumber() : "0" + timeNumber.getNumber()) + "-" +
                    Month.of(timeNumber.getNumber()).length(isLeap);
        }
    };

    public abstract String endTime(TimeNumber timeNumber);

    private static void validYear(int year) {
        if (year <= 0) {
            throw new DateTimeException("Invalid value for year: " + year);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeNumber {
        private int year = 0;

        private int number = 0;
    }
}
