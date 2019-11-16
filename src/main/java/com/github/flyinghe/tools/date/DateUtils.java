package com.github.flyinghe.tools.date;

import com.github.flyinghe.tools.Ognl;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by FlyingHe on 2019/11/16.
 */
public class DateUtils {
    public static final String[] pattern =
            new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S",
                    "yyyy.MM.dd", "yyyy.MM.dd HH:mm", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm:ss.S",
                    "yyyy/MM/dd", "yyyy/MM/dd HH:mm", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss.S"};

    /**
     * 将一个日期字符串(包括整型数字字符串)转化成一个日期类型
     *
     * @param dateStr      日期字符串(包括整型数字字符串)
     * @param datePatterns 日期匹配模式
     * @return 转化后的日期对象, 若失败则返回null
     * @throws Exception
     */
    public static Date strToDate(String dateStr, String... datePatterns) throws IOException {
        if (Ognl.isEmpty(datePatterns)) {
            datePatterns = pattern;
        }
        Date targetDate = null;
        if (StringUtils.isNotEmpty(dateStr)) {
            try {
                long longDate = Long.valueOf(dateStr.trim());
                targetDate = new Date(longDate);
            } catch (NumberFormatException e) {
                try {
                    targetDate =
                            org.apache.commons.lang3.time.DateUtils.parseDate(dateStr, datePatterns);
                } catch (ParseException pe) {
                    throw new IOException(String.format(
                            "'%s' can not convert to type 'java.util.Date',just support timestamp(type of long) and following date format(%s)",
                            dateStr,
                            StringUtils.join(datePatterns, ",")));
                }
            }
        }
        return targetDate;
    }


    /**
     * 将一个日期的时,分,秒,毫秒置零
     *
     * @param calendar 一个日期
     * @return 被转化后的日期
     * @see #dateReservedDay000(Date)
     */
    public static Date dateReservedDay000(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 将一个日期的时,分,秒,毫秒置零
     *
     * @param date 一个日期
     * @return 被转化后的日期
     * @see #dateReservedDay000(Calendar)
     */
    public static Date dateReservedDay000(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return dateReservedDay000(calendar);
    }

    /**
     * 将一个日期的时,分,秒,毫秒调整为最大值
     *
     * @param calendar 一个日期
     * @return 被转化后的日期
     * @see #dateReservedDay999(Date)
     */
    public static Date dateReservedDay999(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 将一个日期的时,分,秒,毫秒调整为最大值
     *
     * @param date 一个日期
     * @return 被转化后的日期
     * @see #dateReservedDay999(Calendar)
     */
    public static Date dateReservedDay999(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return dateReservedDay999(calendar);
    }

    /**
     * 将一个日期的日,时,分,秒,毫秒调整为最小值
     *
     * @param calendar 一个日期
     * @return 被转化后的日期
     * @see #dateReservedMonth000(Date)
     */
    public static Date dateReservedMonth000(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        return dateReservedDay000(calendar);
    }

    /**
     * 将一个日期的日,时,分,秒,毫秒调整为最小值
     *
     * @param date 一个日期
     * @return 被转化后的日期
     * @see #dateReservedMonth000(Calendar)
     */
    public static Date dateReservedMonth000(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return dateReservedMonth000(calendar);
    }

    /**
     * 将一个日期的日,时,分,秒,毫秒调整为最大值
     *
     * @param calendar 一个日期
     * @return 被转化后的日期
     * @see #dateReservedMonth999(Date)
     */
    public static Date dateReservedMonth999(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return dateReservedDay999(calendar);
    }

    /**
     * 将一个日期的日,时,分,秒,毫秒调整为最大值
     *
     * @param date 一个日期
     * @return 被转化后的日期
     * @see #dateReservedMonth999(Calendar)
     */
    public static Date dateReservedMonth999(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return dateReservedMonth999(calendar);
    }

    /**
     * 将指定年月的日期的日,时,分,秒,毫秒调整为最小值或者最大值
     *
     * @param year  指定年份
     * @param month 指定月份
     * @param is000 为true表示置为最小值,反之最大值
     * @return 被转化后的日期
     * @see #dateReservedMonth000(Date)
     * @see #dateReservedMonth000(Calendar)
     * @see #dateReservedMonth999(Date)
     * @see #dateReservedMonth999(Calendar)
     */
    public static Date dateReservedMonth(int year, int month, boolean is000) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        if (month <= 1) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
        } else if (month >= 12) {
            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        } else {
            calendar.set(Calendar.MONTH, month - 1);
        }
        return is000 ? dateReservedMonth000(calendar) : dateReservedMonth999(calendar);
    }

    /**
     * 获取指定年,季度的最初时刻
     *
     * @param year    年份
     * @param quarter 季度
     * @return 指定年, 季度的最初时刻日期对象
     */
    public static Date dateReservedQuarter000(int year, int quarter) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        if (quarter <= 1) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
        } else if (quarter == 2) {
            calendar.set(Calendar.MONTH, Calendar.APRIL);
        } else if (quarter == 3) {
            calendar.set(Calendar.MONTH, Calendar.JULY);
        } else {
            calendar.set(Calendar.MONTH, Calendar.OCTOBER);
        }
        return dateReservedMonth000(calendar);
    }

    /**
     * 获取指定年,季度的最末时刻
     *
     * @param year    年份
     * @param quarter 季度
     * @return 指定年, 季度的最末时刻日期对象
     */
    public static Date dateReservedQuarter999(int year, int quarter) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        if (quarter <= 1) {
            calendar.set(Calendar.MONTH, Calendar.MARCH);
        } else if (quarter == 2) {
            calendar.set(Calendar.MONTH, Calendar.JUNE);
        } else if (quarter == 3) {
            calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        } else {
            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        }
        return dateReservedMonth999(calendar);
    }

    /**
     * 将一个日期的月,日,时,分,秒,毫秒调整为最小值
     *
     * @param calendar 一个日期
     * @return 被转化后的日期
     * @see #dateReservedYear000(Date)
     */
    public static Date dateReservedYear000(Calendar calendar) {
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        return dateReservedDay000(calendar);
    }

    /**
     * 将一个日期的月,日,时,分,秒,毫秒调整为最小值
     *
     * @param date 一个日期
     * @return 被转化后的日期
     * @see #dateReservedYear000(Calendar)
     */
    public static Date dateReservedYear000(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return dateReservedYear000(calendar);
    }

    /**
     * 将一个日期的月,日,时,分,秒,毫秒调整为最大值
     *
     * @param calendar 一个日期
     * @return 被转化后的日期
     * @see #dateReservedYear999(Date)
     */
    public static Date dateReservedYear999(Calendar calendar) {
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);

        return dateReservedDay999(calendar);
    }

    /**
     * 将一个日期的月,日,时,分,秒,毫秒调整为最大值
     *
     * @param date 一个日期
     * @return 被转化后的日期
     * @see #dateReservedYear999(Calendar)
     */
    public static Date dateReservedYear999(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return dateReservedYear999(calendar);
    }

    /**
     * 将某一年对应的日期的月,日,时,分,秒,毫秒调整为最大值或者最小值
     *
     * @param year  年份
     * @param is000 true则调整为最小值,反之最大值
     * @return 被转化后的日期
     * @see #dateReservedYear000(Date)
     * @see #dateReservedYear000(Calendar)
     * @see #dateReservedYear999(Date)
     * @see #dateReservedYear999(Calendar)
     */
    public static Date dateReservedYear(int year, boolean is000) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);

        return is000 ? dateReservedYear000(calendar) : dateReservedYear999(calendar);
    }
}
