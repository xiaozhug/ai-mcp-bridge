package io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.McpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.http.HttpFetchProperties;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * @author xiaozhug
 */
public interface RestMcpClientTransport extends McpClientTransport {

    McpToolSpecification getMcpToolSpecification(HttpFetchProperties httpFetchProperties, String baseUrl);

    String call(String baseUrl, RequestTemplateInfo requestTemplateInfo, Map<String, Object> args, @Nullable Map<String, Object> context);
}
