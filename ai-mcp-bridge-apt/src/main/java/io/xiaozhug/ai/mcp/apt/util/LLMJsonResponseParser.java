package io.xiaozhug.ai.mcp.apt.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 大模型JSON响应解析器，用于从JSON格式返回结果中提取内容
 *
 * @author xiaozhug
 */
public class LLMJsonResponseParser {
    // Jackson ObjectMapper用于解析JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 代码块提取正则（与之前保持一致）
    private static final Pattern JAVA_CODE_PATTERN = 
        Pattern.compile("```java\\s*(.*?)\\s*```", Pattern.DOTALL);
    private static final Pattern JSON_CODE_PATTERN =
            Pattern.compile("```json\\s*(.*?)\\s*```", Pattern.DOTALL);
    private static final Pattern GENERIC_CODE_PATTERN = 
        Pattern.compile("```\\s*(.*?)\\s*```", Pattern.DOTALL);
    
    /**
     * 从JSON响应中提取指定字段的内容
     * @param jsonResponse 大模型返回的JSON字符串
     * @param fieldPath 要提取的字段路径，如"choices.0.message.content"
     * @return 提取到的字段内容
     */
    public static Optional<String> extractFieldFromJson(String jsonResponse, String fieldPath) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty() || fieldPath == null) {
            return Optional.empty();
        }
        
        try {
            // 解析JSON
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            // 按路径查找字段
            JsonNode targetNode = rootNode;
            String[] pathSegments = fieldPath.split("\\.");
            
            for (String segment : pathSegments) {
                // 检查是否为数组索引
                if (segment.matches("\\d+")) {
                    int index = Integer.parseInt(segment);
                    if (targetNode.isArray() && targetNode.size() > index) {
                        targetNode = targetNode.get(index);
                    } else {
                        return Optional.empty();
                    }
                } else {
                    // 普通字段
                    targetNode = targetNode.get(segment);
                }
                
                if (targetNode == null) {
                    return Optional.empty();
                }
            }

            String text = targetNode.asText();
            Matcher jsonMatcher = JSON_CODE_PATTERN.matcher(text);
            if (jsonMatcher.find()) {
                return Optional.of(cleanCode(jsonMatcher.group(1)));
            }

            return Optional.ofNullable(text);
        } catch (JsonProcessingException e) {
            System.err.println("JSON解析失败: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 从JSON响应中提取Java代码（先提取内容字段，再提取代码）
     * @param jsonResponse 大模型返回的JSON字符串
     * @param contentFieldPath 内容字段路径，如"choices.0.message.content"
     * @return 提取到的Java代码
     */
    public static Optional<String> extractJavaCodeFromJson(String jsonResponse, String contentFieldPath) {
        // 先从JSON中提取内容字段
        Optional<String> contentOpt = extractFieldFromJson(jsonResponse, contentFieldPath);
        
        if (!contentOpt.isPresent()) {
            return Optional.empty();
        }
        
        // 再从内容中提取代码（复用之前的代码提取逻辑）
        String content = contentOpt.get();
        Matcher javaMatcher = JAVA_CODE_PATTERN.matcher(content);
        if (javaMatcher.find()) {
            return Optional.of(cleanCode(javaMatcher.group(1)));
        }

        Matcher genericMatcher = GENERIC_CODE_PATTERN.matcher(content);
        if (genericMatcher.find()) {
            return Optional.of(cleanCode(genericMatcher.group(1)));
        }
        
        return Optional.of(cleanCode(content));
    }
    
    /**
     * 清理代码内容
     */
    private static String cleanCode(String code) {
        String cleaned = code.trim();
        cleaned = cleaned.replaceAll("\\n\\s+\\n", "\n\n");
        return cleaned.replaceAll("(?m)^\\s{4,}", "  ");
    }
    
    /**
     * 验证提取的代码是否有效
     */
    public static boolean isCodeValid(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        
        return code.contains("class ") || 
               code.contains("public ") || 
               code.contains("import ") ||
               code.contains(";") ||
               code.contains("{") && code.contains("}");
    }
}