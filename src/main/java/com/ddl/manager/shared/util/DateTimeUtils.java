package com.ddl.manager.shared.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * @author zhenghaipei
 * @since 2025-12-13
 */
public class DateTimeUtils {

    /** 标准日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final DateTimeFormatter STANDARD_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 日期格式：yyyy-MM-dd */
    public static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 时间格式：HH:mm:ss */
    public static final DateTimeFormatter TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /** 紧凑格式：yyyyMMddHHmmss */
    public static final DateTimeFormatter COMPACT_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 格式化日期时间为标准格式（yyyy-MM-dd HH:mm:ss）
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串，如果为null返回"未设置"
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, STANDARD_FORMATTER, "未设置");
    }

    /**
     * 格式化日期时间为标准格式（yyyy-MM-dd HH:mm:ss）
     *
     * @param dateTime 日期时间
     * @param defaultValue null时的默认值
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String defaultValue) {
        return format(dateTime, STANDARD_FORMATTER, defaultValue);
    }

    /**
     * 格式化日期时间
     *
     * @param dateTime 日期时间
     * @param formatter 格式化器
     * @param defaultValue null时的默认值
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter, String defaultValue) {
        if (dateTime == null) {
            return defaultValue;
        }
        return dateTime.format(formatter);
    }

    /**
     * 格式化日期（yyyy-MM-dd）
     *
     * @param dateTime 日期时间
     * @return 格式化后的日期字符串，如果为null返回"未设置"
     */
    public static String formatDate(LocalDateTime dateTime) {
        return format(dateTime, DATE_FORMATTER, "未设置");
    }

    /**
     * 格式化时间（HH:mm:ss）
     *
     * @param dateTime 日期时间
     * @return 格式化后的时间字符串，如果为null返回"未设置"
     */
    public static String formatTime(LocalDateTime dateTime) {
        return format(dateTime, TIME_FORMATTER, "未设置");
    }

    /**
     * 格式化日期时间为紧凑格式（yyyyMMddHHmmss）
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串，如果为null返回空字符串
     */
    public static String formatCompact(LocalDateTime dateTime) {
        return format(dateTime, COMPACT_FORMATTER, "");
    }

    /**
     * 解析标准格式的日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return LocalDateTime，如果解析失败返回null
     */
    public static LocalDateTime parse(String dateTimeStr) {
        return parse(dateTimeStr, STANDARD_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @param formatter 格式化器
     * @return LocalDateTime，如果解析失败返回null
     */
    public static LocalDateTime parse(String dateTimeStr, DateTimeFormatter formatter) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断日期时间是否为null或未设置
     *
     * @param dateTime 日期时间
     * @return true-为null
     */
    public static boolean isNullOrEmpty(LocalDateTime dateTime) {
        return dateTime == null;
    }
}




