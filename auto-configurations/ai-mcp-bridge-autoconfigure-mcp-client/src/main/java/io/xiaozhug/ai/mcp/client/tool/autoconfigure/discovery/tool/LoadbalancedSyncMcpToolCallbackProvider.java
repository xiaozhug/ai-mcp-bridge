package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerProcessorManager;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpClientManager;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpSyncClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.support.ToolUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 负载均衡同步MCP工具回调提供者
 *
 * @author xiaozhug
 */
public class LoadbalancedSyncMcpToolCallbackProvider implements ToolCallbackProvider {

	private final LoadbalancedMcpClientManager<LoadbalancedMcpSyncClient> loadbalancedMcpClientManager;
	private final DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager;

	public LoadbalancedSyncMcpToolCallbackProvider(DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
												   LoadbalancedMcpClientManager<LoadbalancedMcpSyncClient> loadbalancedMcpClientManager) {
		this.loadbalancedMcpClientManager = loadbalancedMcpClientManager;
		this.discoveryMcpServerProcessorManager = discoveryMcpServerProcessorManager;
	}

	@Override
	public ToolCallback[] getToolCallbacks() {
		ArrayList<Object> toolCallbacks = new ArrayList<>();

		loadbalancedMcpClientManager.getLoadbalancedMcpClientMap().forEach((serviceName, value) -> {
			value.listTools().tools().stream().map((tool) -> {
				return new LoadbalancedSyncMcpToolCallback(serviceName, discoveryMcpServerProcessorManager, loadbalancedMcpClientManager, tool);
			}).forEach(toolCallbacks::add);
		});

		ToolCallback[] array = toolCallbacks.toArray(new ToolCallback[0]);
		this.validateToolCallbacks(array);
		return array;
	}

	private void validateToolCallbacks(ToolCallback[] toolCallbacks) {
		List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
		if (!duplicateToolNames.isEmpty()) {
			throw new IllegalStateException(
					"Multiple tools with the same name (%s)".formatted(String.join(", ", duplicateToolNames)));
		}
	}

}
