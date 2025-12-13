package io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.RequestTemplateParser;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.http.HttpFetchProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.util.RequestTemplateUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaozhug
 */
public class WebClientMcpClientTransport implements RestMcpClientTransport{

    private final WebClient.Builder webClientBuilder;

    public WebClientMcpClientTransport(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public McpToolSpecification getMcpToolSpecification(HttpFetchProperties httpFetchProperties, String baseUrl) {
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        return webClient.method(HttpMethod.GET).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new RuntimeException("Client error: " + response.statusCode())))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new RuntimeException("Server error: " + response.statusCode())))
                .bodyToMono(McpToolSpecification.class)
                .timeout(Duration.ofSeconds(httpFetchProperties.getTimeoutDuration())) // 使用配置的超时时间
                .doOnNext(responseBody -> ToolCallback.logger.info("[HttpFetchToolCallbackProvider] received responseBody: {}", responseBody))
                .onErrorResume(e -> {
                    ToolCallback.logger.error("[HttpFetchToolCallbackProvider] Request failed: {}", e.getMessage(), e);
                    return Mono.empty();
                }).onErrorResume(e -> {
                    ToolCallback.logger.error("Failed to execute tool request: {}", e.getMessage(), e);
                    return Mono.empty();
                }).block();
    }

    @Override
    public String call(String baseUrl, RequestTemplateInfo info, Map<String, Object> args, @Nullable Map<String, Object> context) {
        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        String url = info.url;
        String method = info.method;
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());

        // 处理URL中的路径参数
        String processingUrl = RequestTemplateParser.addPathVariables(url, info, args);
        Map<String, Object> params = new HashMap<>();
        params.put("args", args);
        params.put("extendedData", "");
        Object h = context !=null ? context.get("headers") : null;
        params.put("headers", h == null ? new LinkedMultiValueMap<>() : h);

        String processedUrl = RequestTemplateUtils.processTemplateString(processingUrl, params);
        ToolCallback.logger.info("[buildAndExecuteRequest] original url template: {} processed url: {}", url, processedUrl);

        String hostFromUrl = RequestTemplateUtils.extractHostFromUrl(processedUrl);
        String pathOnlyUrl = RequestTemplateUtils.extractPathFromUrl(processedUrl);
        // 构建请求
        WebClient.RequestBodySpec requestBodySpec = client.method(httpMethod)
                .uri(builder -> RequestTemplateParser.buildUri(builder, pathOnlyUrl, info, args));

        // 添加请求头
        MultiValueMap<String, String> headers = RequestTemplateParser.addHeaders(info, params, args,
                RequestTemplateUtils::processTemplateString, requestBodySpec::header);

        if (hostFromUrl != null && !hostFromUrl.isEmpty()) {
            requestBodySpec.header("Host", hostFromUrl);
            headers.add("Host", hostFromUrl);
        }

        // 处理请求体
        RequestTemplateParser.addRequestBody(headers, info, params, args, RequestTemplateUtils::processTemplateString, (mediaType, body) -> {
            return requestBodySpec.contentType(org.springframework.http.MediaType.APPLICATION_JSON).bodyValue(body);
        }, RequestTemplateUtils.objectMapper, ToolCallback.logger);


        // 输出最终请求信息
        String fullUrl = baseUrl.endsWith("/") && pathOnlyUrl.startsWith("/") ? baseUrl + pathOnlyUrl.substring(1)
                : baseUrl + pathOnlyUrl;
        ToolCallback.logger.info("[buildAndExecuteRequest] final request: method={} url={} args={}", method, fullUrl, args);

        return requestBodySpec.retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new RuntimeException("Client error: " + response.statusCode())))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("Server error: " + response.statusCode())))
                .bodyToMono(String.class)
                .timeout(RequestTemplateUtils.getTimeoutDuration()) // 使用配置的超时时间
                .doOnNext(responseBody -> ToolCallback.logger.info("[buildAndExecuteRequest] received responseBody: {}", responseBody))
                .onErrorResume(e -> {
                    ToolCallback.logger.error("[buildAndExecuteRequest] Request failed: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("HTTP request failed: " + e.getMessage(), e));
                }).onErrorResume(e -> {
                    ToolCallback.logger.error("Failed to execute tool request: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Tool execution failed: " + e.getMessage(), e));
                }).block();
    }
}
