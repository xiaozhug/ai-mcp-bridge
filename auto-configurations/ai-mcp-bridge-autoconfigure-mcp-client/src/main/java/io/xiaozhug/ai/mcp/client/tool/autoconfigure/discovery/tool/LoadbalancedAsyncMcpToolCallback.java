package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerProcessorManager;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpClientManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 负载均衡异步MCP工具回调
 *
 * @author xiaozhug
 */
@Slf4j
public class LoadbalancedAsyncMcpToolCallback extends LoadbalancedMcpToolCallback {

	public LoadbalancedAsyncMcpToolCallback(String serviceName,
											DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
											LoadbalancedMcpClientManager<LoadbalancedMcpAsyncClient> loadbalancedMcpClientManager,
											McpSchema.Tool tool) {
        super(serviceName, discoveryMcpServerProcessorManager, tool);

		LoadbalancedMcpAsyncClient mcpClients = loadbalancedMcpClientManager.getLoadbalancedMcpClientMap().get(serviceName);
		this.toolDefinition = ToolDefinition.builder()
			.name(McpToolUtils.prefixedToolName(mcpClients.chooseMcpClient().getClientInfo().name(), this.tool.name()))
			.description(this.tool.description())
			.inputSchema(ModelOptionsUtils.toJsonString(this.tool.inputSchema()))
			.build();
	}
}
