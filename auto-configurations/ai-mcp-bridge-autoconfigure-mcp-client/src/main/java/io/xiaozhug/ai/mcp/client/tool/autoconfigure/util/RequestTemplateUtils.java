package io.xiaozhug.ai.mcp.client.tool.autoconfigure.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 请求模板工具类
 *
 * @author xiaozhug
 */
public class RequestTemplateUtils {

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{\\s*(\\.[\\w]+(?:\\.[\\w]+)*)\\s*\\}\\}");
    public static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String processTemplateString(String template, Map<String, Object> params) {
        Map<String, Object> args = (Map<String, Object>) params.get("args");
        String extendedData = (String) params.get("extendedData");
        ToolCallback.logger.debug("[processTemplateString] template: {} args: {} extendedData: {}", template, args, extendedData);
        if (template == null || template.isEmpty()) {
            return "";
        }
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            // 获取完整路径，如 .args.name 或 .data.key1.key2
            String fullPath = matcher.group(1);
            String replacement = resolvePathValue(fullPath, args, extendedData);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        String finalResult = result.toString();
        ToolCallback.logger.debug("[processTemplateString] final result: {}", finalResult);

        return finalResult;
    }

    /**
     * 根据路径解析值
     * @param fullPath 完整路径，如 .args.name 或 .data.key1.key2
     * @param args 参数数据映射
     * @param extendedData 扩展数据（JSON字符串）
     * @return 解析后的值
     */
    private static String resolvePathValue(String fullPath, Map<String, Object> args, String extendedData) {
        if (fullPath == null || fullPath.isEmpty()) {
            return "";
        }
        // 移除开头的点号
        if (fullPath.startsWith(".")) {
            fullPath = fullPath.substring(1);
        }

        String[] pathParts = fullPath.split("\\.");
        if (pathParts.length == 0) {
            return "";
        }

        // 确定数据源
        Object dataSource;
        if (pathParts[0].equals("args")) {
            // 从args中取值
            dataSource = args;
            // 如果只有args，没有具体字段名
            if (pathParts.length == 1) {
                if (args != null && args.size() == 1) {
                    return String.valueOf(args.values().iterator().next());
                }
                else if (args != null && !args.isEmpty()) {
                    return args.toString();
                }
                else {
                    return "";
                }
            }
        }
        else {
            // 从extendedData中取值
            // 首先将extendedData字符串解析为JSON对象
            try {
                if (StringUtils.hasText(extendedData)) {
                    dataSource = objectMapper.readValue(extendedData, Map.class);
                }
                else {
                    dataSource = null;
                }
            }
            catch (Exception e) {
                ToolCallback.logger.warn("[resolvePathValue] Failed to parse extendedData as JSON: {}", e.getMessage());
                // 如果解析失败，将extendedData作为普通字符串处理
                if (pathParts.length == 1 && fullPath.equals("extendedData")) {
                    return extendedData != null ? extendedData : "";
                }
                return "";
            }

            // 特殊处理直接访问extendedData的情况
            if (pathParts.length == 1 && fullPath.equals("extendedData")) {
                return extendedData != null ? extendedData : "";
            }
        }

        // 如果数据源为空
        if (dataSource == null) {
            return "";
        }
        // 处理嵌套路径
        Object currentValue = dataSource;
        int startIndex = pathParts[0].equals("args") ? 1 : 0;
        // 如果是args，从索引1开始；否则从索引0开始

        for (int i = startIndex; i < pathParts.length; i++) {
            String key = pathParts[i];
            if (currentValue instanceof Map) {
                Map<String, Object> currentMap = (Map<String, Object>) currentValue;
                currentValue = currentMap.get(key);
            }
            else {
                ToolCallback.logger.warn("[resolvePathValue] Cannot access key '{}' from non-map value", key);
                return "";
            }

            if (currentValue == null) {
                ToolCallback.logger.warn("[resolvePathValue] Key '{}' not found in nested path", key);
                return "";
            }
        }
        return currentValue.toString();
    }

    /**
     * 从完整URL中提取路径部分
     * @param url 完整的URL
     * @return 路径部分，如果解析失败则返回原URL
     */
    public static String extractPathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        try {
            java.net.URI uri = java.net.URI.create(url);
            String path = uri.getPath();
            String query = uri.getQuery();

            if (path == null) {
                path = "";
            }

            if (query != null && !query.isEmpty()) {
                return path + "?" + query;
            }

            return path;
        }
        catch (Exception e) {
            ToolCallback.logger.warn("[extractPathFromUrl] Failed to parse URL: {}", e.getMessage());
            return url; // 解析失败时返回原URL
        }
    }

    /**
     * 从URL中提取主机信息
     * @param url 完整的URL
     * @return 主机信息（host:port格式），如果没有则返回null
     */
    public static String extractHostFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // 使用URI类解析URL
            java.net.URI uri = java.net.URI.create(url);
            String host = uri.getHost();
            int port = uri.getPort();

            if (host != null && !host.isEmpty()) {
                if (port != -1) {
                    return host + ":" + port;
                }
                return host;
            }
        }
        catch (Exception e) {
            ToolCallback.logger.warn("[extractHostFromUrl] Failed to parse URL: {}", e.getMessage());
        }

        return null;
    }

    public static Duration getTimeoutDuration() {

        return Duration.ofSeconds(5); // 默认超时时间
    }

    public static RequestTemplateInfo extractRequestTemplateInfo(String inputSchema) {
        McpSchema.JsonSchema jsonSchema = JsonUtils.fromJson(inputSchema, McpSchema.JsonSchema.class);
        return JsonUtils.fromJson(JsonUtils.toJSONString(jsonSchema.defs().get("requestTemplateInfo")), RequestTemplateInfo.class);
    }
}
