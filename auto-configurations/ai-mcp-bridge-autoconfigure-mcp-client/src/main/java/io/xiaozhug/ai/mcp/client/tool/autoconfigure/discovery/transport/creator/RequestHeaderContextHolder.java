package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 请求头上下文持有者
 *
 * @author xiaozhug
 */
public class RequestHeaderContextHolder {

    private static final ThreadLocal<MultiValueMap<String, String>> headersHolder = new ThreadLocal<>();

    public static void setHeaders(MultiValueMap<String, String> headers) {
        headersHolder.set(headers);
    }

    public static MultiValueMap<String, String> getHeaders() {
        return headersHolder.get() == null ? new LinkedMultiValueMap<>() : headersHolder.get();
    }

    public static void resetHeaders() {
        headersHolder.remove();
    }
}
