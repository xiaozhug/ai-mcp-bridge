package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerProcessorManager;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpAsyncClient;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpClientManager;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.support.ToolUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 负载均衡异步MCP工具回调提供者
 *
 * @author xiaozhug
 */
public class LoadbalancedAsyncMcpToolCallbackProvider implements ToolCallbackProvider {

	private final LoadbalancedMcpClientManager<LoadbalancedMcpAsyncClient> loadbalancedMcpClientManager;
	private final DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager;

	public LoadbalancedAsyncMcpToolCallbackProvider(DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
													LoadbalancedMcpClientManager<LoadbalancedMcpAsyncClient> loadbalancedMcpClientManager) {
		this.loadbalancedMcpClientManager = loadbalancedMcpClientManager;
		this.discoveryMcpServerProcessorManager = discoveryMcpServerProcessorManager;
	}

	@Override
	public ToolCallback[] getToolCallbacks() {
		ArrayList<Object> toolCallbacks = new ArrayList<>();

		//todo lazy load
		loadbalancedMcpClientManager.getLoadbalancedMcpClientMap().forEach((serviceName, mcpClient) -> {
			toolCallbacks.addAll(Objects.requireNonNull(mcpClient.listTools().map(response -> {
				return response.tools().stream().map((tool) -> {
					return new LoadbalancedAsyncMcpToolCallback(serviceName, discoveryMcpServerProcessorManager, loadbalancedMcpClientManager, tool);
				}).toList();
			}).block()));
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
