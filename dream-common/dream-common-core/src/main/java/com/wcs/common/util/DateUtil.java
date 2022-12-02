package com.wcs.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {

    public static final String localDateStr = "yyyyMMdd";

    /**
     * 获取现在时间差
     * @param date 时间
     * @param days 差值 天数
     */
    public static boolean compareNowDate(Date date,int days){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,days);
        return System.currentTimeMillis()-calendar.getTimeInMillis()>0?true:false;
    }

    /**
     * 获取现在时间差
     * @param date 时间
     * @param days 差值 天数
     */
    public static boolean compareNowDate(LocalDate date, int days){
        return LocalDate.now().compareTo(date.plusDays(days))>0?true:false;
    }

    /**
     * 转换时间
     */
    public static LocalDate transformLocalDate(String str,String format){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(str,dateTimeFormatter);
    }

    /**
     * 获取时间字符串
     * @param date 日期
     * @param days 天数
     * @param sign true 正 false 负
     */
    public static List<String> getDateStr(LocalDate date,int days,boolean sign){
        List<String>result = new ArrayList<>(days+1);
        result.add(date.toString());
        for (int i = 0;i<days;i++){
            result.add(sign?date.plusDays(i).toString():date.minusDays(i).toString());
        }
        return result;
    }
}
