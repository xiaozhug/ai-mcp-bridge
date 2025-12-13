package io.xiaozhug.ai.mcp.client.tool.autoconfigure.http;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.HttpToolCallback;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp.HttpToolCallbackProvider;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP获取工具回调提供者
 *
 * @author xiaozhug
 */
public class HttpFetchToolCallbackProvider implements HttpToolCallbackProvider {

    private final String PATH = "/v1/expose/mcp-metadata";
    private final HttpFetchProperties httpFetchProperties;
    private final Map<String, String> connections;
    private final RestMcpClientTransport restMcpClientTransport;

    public HttpFetchToolCallbackProvider(HttpFetchProperties httpFetchProperties, RestMcpClientTransport restMcpClientTransport) {
        this.httpFetchProperties = httpFetchProperties;
        this.connections = httpFetchProperties.getConnections();
        this.restMcpClientTransport = restMcpClientTransport;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        List<ToolCallback> toolCallbackList = new ArrayList<>();

        for (Map.Entry<String, String> entry : connections.entrySet()) {
            String baseUrl = entry.getValue() + PATH;

            McpToolSpecification mcpToolSpecification = restMcpClientTransport.getMcpToolSpecification(httpFetchProperties, baseUrl);

            if (mcpToolSpecification != null) {
                List<McpTool> tools = mcpToolSpecification.getTools();
                if (tools == null || tools.isEmpty()) {
                    continue;
                }

                tools.forEach(tool -> toolCallbackList.add(new HttpToolCallback(restMcpClientTransport, entry.getKey(), tool, entry.getValue())));
            }

        }

        ToolCallback[] toolCallbacks = toolCallbackList.toArray(new ToolCallback[0]);

        validateToolCallbacks(toolCallbacks);

        return toolCallbacks;
    }
}
