package io.xiaozhug.ai.mcp.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringCompression {

    public static void main(String[] args) {
        String original = "这是一个需要压缩的长字符串示例。这是一个需要压缩的长字符串示例。" +
                          "这是一个需要压缩的长字符串示例。这是一个需要压缩的长字符串示例。";
        
        System.out.println("原始字符串长度: " + original.length());
        
        try {
            // 压缩字符串
            String compressed = compressString(original);
            System.out.println("压缩后字符串长度: " + compressed.length());

            // 解压字符串
            String decompressed = decompressString(compressed);
            System.out.println("解压后字符串长度: " + decompressed.length());
            System.out.println("解压结果正确: " + original.equals(decompressed));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 使用GZIP压缩字符串
     */
    public static String compressString(String str) throws IOException {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        
        gzipOutputStream.write(str.getBytes("UTF-8"));
        gzipOutputStream.close();
        
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }
    
    /**
     * 使用GZIP解压字符串
     */
    public static String decompressString(String compressedStr) throws IOException {
        if (compressedStr == null || compressedStr.isEmpty()) {
            return compressedStr;
        }
        
        byte[] compressed = Base64.getDecoder().decode(compressedStr);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        
        while ((len = gzipInputStream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        
        gzipInputStream.close();
        byteArrayOutputStream.close();
        
        return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
    }
}