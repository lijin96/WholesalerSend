package com.example.wholesalersend.utils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @ClassName: NumberUtils
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2026/3/10 14:56
 */
public class NumberUtils {

    /**
     * 数字格式化工具类
     */

    /**
     * 转换为字符串，整数时去掉小数部分
     */
    public static String toString(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof Integer || value instanceof Long) {
            return value.toString();
        }

        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                // 整数
                return String.valueOf(((Number) value).longValue());
            } else {
                // 小数
                return String.valueOf(d);
            }
        }

        if (value instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) value;
            bd = bd.stripTrailingZeros();
            return bd.toPlainString();
        }

        return value.toString();
    }

    /**
     * 强制转换为整数字符串
     */
    public static String toIntString(Object value) {
        if (value == null) {
            return "0";
        }

        try {
            if (value instanceof Number) {
                return String.valueOf(((Number) value).intValue());
            }

            // 尝试解析
            String str = value.toString();
            if (str.contains(".")) {
                return str.substring(0, str.indexOf('.'));
            }
            return str;
        } catch (Exception e) {
            return value.toString();
        }
    }

    /**
     * 智能格式化数字
     */
    public static String smartFormat(Object value) {
        if (value == null) {
            return "";
        }

        String str = value.toString();

        // 如果是数字形式
        if (str.matches("-?\\d+(\\.\\d+)?")) {
            try {
                BigDecimal bd = new BigDecimal(str);
                bd = bd.stripTrailingZeros();
                return bd.toPlainString();
            } catch (Exception e) {
                return str;
            }
        }

        return str;
    }

    /**
     * 从Map中安全获取整数字符串
     */
    public static String getIntString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return "0";
        }

        Object value = map.get(key);
        return toIntString(value);
    }

    /**
     * 从Map中安全获取格式化字符串
     */
    public static String getFormattedString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return "";
        }

        Object value = map.get(key);
        return smartFormat(value);
    }
}