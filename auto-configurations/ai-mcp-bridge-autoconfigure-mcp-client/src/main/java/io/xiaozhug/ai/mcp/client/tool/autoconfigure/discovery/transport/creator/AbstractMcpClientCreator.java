package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.McpClientProperties;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * 抽象MCP客户端创建者
 *
 * @author xiaozhug
 */
public abstract class AbstractMcpClientCreator {

    protected final McpClientProperties mcpClientProperties;

    public AbstractMcpClientCreator(McpClientProperties mcpClientProperties) {
        this.mcpClientProperties = mcpClientProperties;
    }

    public abstract McpSyncClient createMcpSyncClient(McpSchema.Implementation clientInfo, McpToolSpecification metadata);

    public abstract McpAsyncClient createMcpAsyncClient(McpSchema.Implementation clientInfo, McpToolSpecification metadata);
}
