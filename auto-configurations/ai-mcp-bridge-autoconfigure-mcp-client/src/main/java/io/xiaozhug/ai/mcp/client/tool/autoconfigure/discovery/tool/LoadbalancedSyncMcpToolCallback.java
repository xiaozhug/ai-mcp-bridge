package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerProcessorManager;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpClientManager;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 负载均衡同步MCP工具回调
 *
 * @author xiaozhug
 */
@Slf4j
public class LoadbalancedSyncMcpToolCallback extends LoadbalancedMcpToolCallback {

	public LoadbalancedSyncMcpToolCallback(String serviceName,
										   DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
										   LoadbalancedMcpClientManager<LoadbalancedMcpSyncClient> loadbalancedMcpClientManager,
										   McpSchema.Tool tool) {
        super(serviceName, discoveryMcpServerProcessorManager, tool);

		LoadbalancedMcpSyncClient mcpClient = loadbalancedMcpClientManager.getLoadbalancedMcpClientMap().get(serviceName);
		this.toolDefinition = ToolDefinition.builder()
				.name(McpToolUtils.prefixedToolName(mcpClient.chooseMcpClient().getClientInfo().name(), this.tool.name()))
				.description(this.tool.description())
				.inputSchema(ModelOptionsUtils.toJsonString(this.tool.inputSchema()))
				.build();
	}
}
