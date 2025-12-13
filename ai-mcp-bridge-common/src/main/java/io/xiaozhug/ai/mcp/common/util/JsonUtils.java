package io.xiaozhug.ai.mcp.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    public static String toJSONString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        }catch (Exception ignore) {
        }
        return null;
    }

    public static String toJSONStringUnsafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toBytes(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        }catch (Exception ignore) {
        }
        return null;
    }

    @SneakyThrows
    public static <T> T parseObject(String json, Class<T> clz){
        return objectMapper.readValue(json, clz);
    }

    public static <T> T parseObject(byte[] b, Class<T> clz) throws Exception {
        return objectMapper.readValue(b, clz);
    }

    public static <T> T parseObject(byte[] b, TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(b, typeReference);
    }

    public static ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }

    public static ObjectNode create() {
        return objectMapper.createObjectNode();
    }

    public static <T> T fromJson(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public static <T> T fromJson(String content, TypeReference<T> valueTypeRef) {
        try {
            return objectMapper.readValue(content, valueTypeRef);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public static  <T extends JsonNode> T valueToTree(Object fromValue) throws IllegalArgumentException {
        return objectMapper.valueToTree(fromValue);
    }

    public static JsonNode readTree(String json) throws Exception {
        return objectMapper.readTree(json);
    }

    public static void writeFormattedJson(Object data, String filePath) throws Exception {
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter(); // 启用格式化输出
        Path outputPath = Paths.get(filePath);
        Files.createDirectories(outputPath.getParent()); // 确保目录存在
        writer.writeValue(outputPath.toFile(), data); // 写入格式化的 JSON
    }

    public static void readJson(String filePath, Object data) throws Exception {
        Path inputPath = Paths.get(filePath);
        if (Files.exists(inputPath)) {
            objectMapper.readerForUpdating(data).readValue(inputPath.toFile());
        }
    }
}