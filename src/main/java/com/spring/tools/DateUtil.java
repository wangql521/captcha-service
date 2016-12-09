package com.spring.tools;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by wangqingli1 on 2016/12/8.
 */
public class DateUtil implements Serializable {

    /**
     * 日期相减(返回秒值)
     * @param date Date
     * @param date1 Date
     * @return int
     * @author
     */
    public static Long diffDateTime(Date date, Date date1) {
        return (Long) ((getMillis(date) - getMillis(date1))/1000);
    }
    public static long getMillis(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.getTimeInMillis();
    }
    /**
     * 获取 指定日期 后 指定毫秒后的 Date
     *
     * @param date
     * @param millSecond
     * @return
     */
    public static Date getDateAddMillSecond(Date date, int millSecond) {
        Calendar cal = Calendar.getInstance();
        if (null != date) {// 没有 就取当前时间
            cal.setTime(date);
        }
        cal.add(Calendar.MILLISECOND, millSecond);
        return cal.getTime();
    }
}
