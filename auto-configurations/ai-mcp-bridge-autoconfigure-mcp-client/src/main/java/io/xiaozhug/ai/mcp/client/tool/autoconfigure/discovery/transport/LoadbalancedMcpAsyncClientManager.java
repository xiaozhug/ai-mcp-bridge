package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerFetcher;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.McpClientProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.AbstractMcpClientCreator;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;

import java.util.List;

/**
 * 负载均衡MCP异步客户端管理器
 *
 * @author xiaozhug
 */
public class LoadbalancedMcpAsyncClientManager extends LoadbalancedMcpClientManager<LoadbalancedMcpAsyncClient> {

    public LoadbalancedMcpAsyncClientManager(DiscoveryMcpServerFetcher discoveryMcpServerFetcher, McpClientProperties mcpClientProperties, AbstractMcpClientCreator mcpClientCreator) {
        super(discoveryMcpServerFetcher, mcpClientProperties, mcpClientCreator);
    }

    @Override
    protected LoadbalancedMcpAsyncClient createLoadbalancedMcpClientInstance(String serviceName, List<McpToolSpecification> mcpToolSpecificationList,
                                                                             McpClientProperties mcpClientProperties, AbstractMcpClientCreator mcpClientCreator) {
        return new LoadbalancedMcpAsyncClient(serviceName, mcpToolSpecificationList, mcpClientProperties, mcpClientCreator);
    }
}
