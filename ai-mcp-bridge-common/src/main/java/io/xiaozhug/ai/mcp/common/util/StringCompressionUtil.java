package io.xiaozhug.ai.mcp.common.util;

import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringCompressionUtil {

    /**
     * 压缩字符串
     */
    @SneakyThrows
    public static String compress(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(str.getBytes("UTF-8"));
        }

        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }

    /**
     * 解压字符串
     */
    @SneakyThrows
    public static String decompress(String compressedStr) {
        if (compressedStr == null || compressedStr.isEmpty()) {
            return compressedStr;
        }

        byte[] compressed = Base64.getDecoder().decode(compressedStr);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, len);
            }

            return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
        }
    }
}