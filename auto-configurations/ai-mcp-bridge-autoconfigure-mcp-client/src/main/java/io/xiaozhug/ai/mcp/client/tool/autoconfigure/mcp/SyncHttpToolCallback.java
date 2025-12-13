package io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.HttpToolCallback;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;

/**
 * 同步HTTP工具回调
 *
 * @author xiaozhug
 */
public class SyncHttpToolCallback extends HttpToolCallback {

    public SyncHttpToolCallback(RestMcpClientTransport restMcpClientTransport, McpSyncClient mcpClient,
                                McpSseClientProperties sseProperties, McpSchema.Tool tool) {
        super(restMcpClientTransport, mcpClient.getClientInfo(), sseProperties, tool);
    }
}
