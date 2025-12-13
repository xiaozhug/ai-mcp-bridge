package io.xiaozhug.ai.mcp.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MetadataUtils {

    // 上传时对数据进行 Base64 编码
    public static String encodeToBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    // 下载时对数据进行 Base64 解码
    public static String decodeFromBase64(String base64Data) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}