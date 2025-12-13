package io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.util.Assert;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * 同步MCP工具回调提供者
 *
 * @author xiaozhug
 */
public class SyncHttpToolCallbackProvider implements HttpToolCallbackProvider {

    private final List<McpSyncClient> mcpClients;
    private final McpSseClientProperties sseProperties;
    private final RestMcpClientTransport restMcpClientTransport;

    public SyncHttpToolCallbackProvider(List<McpSyncClient> mcpClients, McpSseClientProperties sseProperties, RestMcpClientTransport restMcpClientTransport) {
        Assert.notNull(mcpClients, "MCP clients must not be null");
        this.mcpClients = mcpClients;
        this.sseProperties = sseProperties;
        this.restMcpClientTransport = restMcpClientTransport;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        var array = this.mcpClients.stream()
                .flatMap(mcpClient -> mcpClient.listTools()
                        .tools()
                        .stream()
                        .filter(tool -> {
                            try {
                                return tool.inputSchema().defs().get("requestTemplateInfo") != null;
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .map(tool -> new SyncHttpToolCallback(restMcpClientTransport, mcpClient, sseProperties, tool)))
                .toArray(ToolCallback[]::new);
        validateToolCallbacks(array);
        return array;
    }
}
