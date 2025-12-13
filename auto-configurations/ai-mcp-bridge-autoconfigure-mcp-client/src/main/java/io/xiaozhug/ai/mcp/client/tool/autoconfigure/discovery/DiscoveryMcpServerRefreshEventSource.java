package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;

import lombok.Data;

/**
 * 发现MCP服务刷新事件源
 *
 * @author xiaozhug
 */
@Data
public class DiscoveryMcpServerRefreshEventSource {

    private final DiscoveryMcpServerFetcher.HttpMetadataCache httpMetadataCache;
    private final DiscoveryMcpServerFetcher.McpServerMetadataCache mcpServerMetadataCache;
}
