package io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.util.Assert;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 异步MCP获取工具回调提供者
 *
 * @author xiaozhug
 */
public class AsyncMcpFetchToolCallbackProvider implements HttpToolCallbackProvider {

    private final List<McpAsyncClient> mcpClients;
    private final McpSseClientProperties sseProperties;
    private final RestMcpClientTransport restMcpClientTransport;

    public AsyncMcpFetchToolCallbackProvider(List<McpAsyncClient> mcpClients, McpSseClientProperties sseProperties, RestMcpClientTransport restMcpClientTransport) {
        Assert.notNull(mcpClients, "MCP clients must not be null");
        this.mcpClients = mcpClients;
        this.sseProperties = sseProperties;
        this.restMcpClientTransport = restMcpClientTransport;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        List<ToolCallback> toolCallbackList = new ArrayList<>();

        for (McpAsyncClient mcpClient : this.mcpClients) {

            ToolCallback[] toolCallbacks = mcpClient.listTools()
                    .map(response -> response.tools()
                            .stream()
                            .filter(tool -> {
                                try {
                                    return tool.inputSchema().defs().get("requestTemplateInfo") != null;
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .map(tool -> new AsyncHttpToolCallback(restMcpClientTransport, mcpClient, sseProperties, tool))
                            .toArray(ToolCallback[]::new))
                    .block();

            validateToolCallbacks(toolCallbacks);

            toolCallbackList.addAll(List.of(toolCallbacks));
        }

        return toolCallbackList.toArray(new ToolCallback[0]);
    }
}
