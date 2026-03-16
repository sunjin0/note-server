package com.note.noteserver.util;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * JSON 工具类
 * 提供安全的 JSON 转换方法
 */
@Slf4j
public class JsonUtil {

    /**
     * 将字符串列表转换为 JSON 数组字符串
     *
     * @param list 字符串列表
     * @return JSON 数组字符串，如 ["item1","item2"]
     */
    public static String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            json.append("\"").append(escapeJson(list.get(i))).append("\"");
            if (i < list.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    /**
     * 将对象列表转换为 JSON 数组字符串
     *
     * @param items 对象列表
     * @param converter 对象到 JSON 字符串的转换器
     * @return JSON 数组字符串
     */
    public static <T> String toJsonArray(List<T> items, JsonConverter<T> converter) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            json.append(converter.convert(items.get(i)));
            if (i < items.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    /**
     * 将 Map 转换为 JSON 对象字符串
     *
     * @param map 键值对
     * @return JSON 对象字符串
     */
    public static String toJsonObject(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder json = new StringBuilder("{");
        int index = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(escapeJson(entry.getKey())).append("\":");
            json.append(valueToJson(entry.getValue()));
            if (index < map.size() - 1) {
                json.append(",");
            }
            index++;
        }
        json.append("}");
        return json.toString();
    }
    /**
     * 将字符串转换对象
     * @param json JSON 字符串
     * @param clazz 目标对象类
     * @param <T> 目标对象类型
     * @return 转换后的对象
     */
    public static <T> T jsonToObject(String json, Class<T> clazz) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, clazz);
        } catch (Exception e) {
            log.error("Error converting JSON to object", e);
            return null;
        }
    }
    /**
     * 创建简单的 JSON 对象
     *
     * @param key 键
     * @param value 值
     * @return JSON 对象字符串
     */
    public static String createJsonObject(String key, String value) {
        return "{\"" + escapeJson(key) + "\":\"" + escapeJson(value) + "\"}";
    }

    /**
     * 创建包含多个字段的 JSON 对象
     *
     * @param fields 字段键值对
     * @return JSON 对象字符串
     */
    public static String createJsonObject(Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return "{}";
        }
        StringBuilder json = new StringBuilder("{");
        int index = 0;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            json.append("\"").append(escapeJson(entry.getKey())).append("\":");
            if (entry.getValue() == null) {
                json.append("null");
            } else {
                json.append("\"").append(escapeJson(entry.getValue())).append("\"");
            }
            if (index < fields.size() - 1) {
                json.append(",");
            }
            index++;
        }
        json.append("}");
        return json.toString();
    }

    /**
     * 将值转换为 JSON 格式
     */
    private static String valueToJson(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof List) {
            return toJsonArray((List<?>) value, Object::toString);
        } else if (value instanceof Map) {
            return toJsonObject((Map<String, Object>) value);
        } else {
            return "\"" + escapeJson(value.toString()) + "\"";
        }
    }

    /**
     * 转义 JSON 特殊字符
     *
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    public static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        String hex = Integer.toHexString(c);
                        result.append("\\u");
                        for (int i = 0; i < 4 - hex.length(); i++) {
                            result.append('0');
                        }
                        result.append(hex);
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.toString();
    }

    /**
     * JSON 转换器接口
     *
     * @param <T> 源类型
     */
    @FunctionalInterface
    public interface JsonConverter<T> {
        String convert(T item);
    }
}
