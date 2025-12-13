package io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.RequestTemplateParser;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.http.HttpFetchProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.util.RequestTemplateUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaozhug
 */
public class RestTemplateMcpClientTransport implements RestMcpClientTransport{

    private final RestTemplate restTemplate;

    public RestTemplateMcpClientTransport(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public McpToolSpecification getMcpToolSpecification(HttpFetchProperties httpFetchProperties, String baseUrl) {
        McpToolSpecification mcpToolSpecification = null;
        try {
            mcpToolSpecification = restTemplate.getForObject(baseUrl, McpToolSpecification.class);
        } catch (Exception e) {
            ToolCallback.logger.error("[HttpFetchToolCallbackProvider] Request failed: {}", e.getMessage(), e);
        }

        return mcpToolSpecification;
    }

    @Override
    public String call(String baseUrl, RequestTemplateInfo info, Map<String, Object> args, @Nullable Map<String, Object> context) {

        String url = info.url;
        String method = info.method;

        // 处理URL中的路径参数
        String processingUrl = RequestTemplateParser.addPathVariables(url, info, args);
        Map<String, Object> params = new HashMap<>();
        params.put("args", args);
        params.put("extendedData", "");
        Object h = context.get("headers");
        params.put("headers", h == null ? new LinkedMultiValueMap<>() : h);

        String processedUrl = RequestTemplateUtils.processTemplateString(processingUrl, params);
        ToolCallback.logger.info("[buildAndExecuteRequest] original url template: {} processed url: {}", url, processedUrl);

        String hostFromUrl = RequestTemplateUtils.extractHostFromUrl(processedUrl);
        String pathOnlyUrl = RequestTemplateUtils.extractPathFromUrl(processedUrl);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        // 构建请求头
        RequestTemplateParser.addHeaders(info, params, args, RequestTemplateUtils::processTemplateString, headers::add);
        if (hostFromUrl != null && !hostFromUrl.isEmpty()) {
            headers.add("Host", hostFromUrl);
        }

        // 构建请求实体
        Object requestBody = RequestTemplateParser.addRequestBody(headers, info, params, args, RequestTemplateUtils::processTemplateString, (mediaType, body) -> {
            headers.put("Content-Type", Arrays.asList(mediaType.toString()));
            return body;
        }, RequestTemplateUtils.objectMapper, ToolCallback.logger);

        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        // 输出最终请求信息
        String fullUrl = baseUrl.endsWith("/") && pathOnlyUrl.startsWith("/") ? baseUrl + pathOnlyUrl.substring(1)
                : baseUrl + pathOnlyUrl;
        ToolCallback.logger.info("[buildAndExecuteRequest] final request: method={} url={} args={}", method, fullUrl, args);

        try {
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.valueOf(method.toUpperCase()), requestEntity, String.class);
            ToolCallback.logger.info("[buildAndExecuteRequest] received responseBody: {}", response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            ToolCallback.logger.error("[buildAndExecuteRequest] Client error: {}", e.getMessage(), e);
            throw new RuntimeException("Client error: " + e.getStatusCode(), e);
        } catch (HttpServerErrorException e) {
            ToolCallback.logger.error("[buildAndExecuteRequest] Server error: {}", e.getMessage(), e);
            throw new RuntimeException("Server error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            ToolCallback.logger.error("[buildAndExecuteRequest] Request failed: {}", e.getMessage(), e);
            throw new RuntimeException("HTTP request failed: " + e.getMessage(), e);
        }
    }
}
