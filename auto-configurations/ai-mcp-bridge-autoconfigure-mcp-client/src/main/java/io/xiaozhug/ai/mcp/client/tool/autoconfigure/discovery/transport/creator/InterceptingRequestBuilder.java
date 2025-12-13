package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator;

import org.springframework.util.MultiValueMap;

import java.net.http.HttpRequest;

/**
 * 拦截请求构建器
 *
 * @author xiaozhug
 */
public class InterceptingRequestBuilder {
    private final HttpRequest.Builder delegate;

    public InterceptingRequestBuilder() {
        this.delegate = HttpRequest.newBuilder();
    }

    public HttpRequest.Builder build() {
        try {
            MultiValueMap<String, String> headers = RequestHeaderContextHolder.getHeaders();
            if (headers != null) {
                for (String key : headers.keySet()) {
                    for (String value : headers.get(key)) {
                        delegate.header(key, value);
                    }
                }
            }

        } finally {
            RequestHeaderContextHolder.resetHeaders();
        }

        return delegate;
    }
}