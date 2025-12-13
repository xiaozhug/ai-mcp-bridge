package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.McpClientProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.AbstractMcpClientCreator;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 负载均衡MCP异步客户端
 *
 * @author xiaozhug
 */
@Slf4j
public class LoadbalancedMcpAsyncClient extends LoadbalancedMcpClient<McpAsyncClient> {

	public LoadbalancedMcpAsyncClient(String serviceName, List<McpToolSpecification> metadatas, McpClientProperties mcpClientProperties, AbstractMcpClientCreator mcpClientCreator) {
		super(serviceName, metadatas, mcpClientProperties, mcpClientCreator);

		init();
	}

	public Mono<McpSchema.CallToolResult> callTool(McpSchema.CallToolRequest callToolRequest) {
		List<McpAsyncClient> asyncClients = new ArrayList<>();

		keyToToolNameMap.forEach((key, value) -> {
			if (value.contains(callToolRequest.name())) {
				List<McpAsyncClient> clients = keyToClientMap.get(key);
				if (clients != null) {
					asyncClients.addAll(clients);
				}
			}
		});

		return chooseMcpClient(asyncClients).callTool(callToolRequest).onErrorResume(throwable -> {
			log.error("Error calling tool {}: {}", callToolRequest.name(), throwable.getMessage());
			return Mono.just(new McpSchema.CallToolResult(throwable.toString(), true));
		});
	}

	public Mono<McpSchema.ListToolsResult> listTools() {
		return Mono.defer(() -> {
			List<McpToolSpecification> metadatas = getMetadatas();
			if(CollectionUtils.isEmpty(metadatas)){
				return Mono.just(new McpSchema.ListToolsResult(Collections.emptyList(), null));
			}

			return Mono.just(new McpSchema.ListToolsResult(
					metadatas.stream()
							.flatMap(metadata -> metadata.getTools().stream())
							.collect(Collectors.toMap(McpTool::getName, item -> item, (item1, item2) -> item1))
							.values().stream().map(tool -> new McpSchema.Tool(
									tool.getName(),
									tool.getDescription(),
									tool.getInputSchema()
							))
							.collect(Collectors.toList()),
					null));
		});
	}

	@Override
	protected McpAsyncClient getMcpClient(List<McpAsyncClient> asynClients) {
		if (asynClients.isEmpty()) {
			throw new IllegalStateException("No McpAsyncClient available");
		}
		// 从client2CountMap中挑选value最小的键是哪个
		String clientInfoName = client2CountMap.entrySet()
				.stream()
				.min(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.get();

		client2CountMap.put(clientInfoName, client2CountMap.get(clientInfoName) + 1);
		// 从clients中找到clientInfoName对应的client
		return asynClients.stream()
				.filter(aysnClient -> aysnClient.getClientInfo().name().equals(clientInfoName))
				.findFirst()
				.get();
	}

	@Override
	protected void createMcpClient(McpToolSpecification metadata) {
		try {
			McpSchema.Implementation clientInfo = new McpSchema.Implementation(
					this.connectedClientName(mcpClientProperties.getName(), metadata.getServiceName()  + "-" + metadata.getInstanceId()),
					mcpClientProperties.getVersion());

			McpAsyncClient asyncClient = mcpClientCreator.createMcpAsyncClient(clientInfo, metadata);
			if (mcpClientProperties.isInitialized()) {
				asyncClient.initialize();
				String key = metadata.getInstanceId() + "-" + metadata.getUrl();
				keyToClientMap.computeIfAbsent(key, k -> new ArrayList<>()).add(asyncClient);
			}
			log.info("Added McpAsyncClient: {}", clientInfo.name());

			client2CountMap.put(asyncClient.getClientInfo().name(), 0);
		} catch (Throwable t) {
			log.error("Failed to create McpAsyncClient for metadata: {}", metadata, t);
		}
	}

	@Override
	public void destroy() throws Exception {
		if (!keyToClientMap.isEmpty()) {
			keyToClientMap.forEach((key, value) -> {
				removeMcpClient(key);
			});
		}

		keyToClientMap.clear();
		client2CountMap.clear();
	}

	@Override
	protected void removeMcpClient(String metadataKey) {
		List<McpAsyncClient> clients = keyToClientMap.remove(metadataKey);
		if (clients != null) {
			for (McpAsyncClient asyncClient : clients) {
				try {
					asyncClient.closeGracefully().block();
					log.info("Removed McpAsyncClient: {}", asyncClient.getClientInfo().name());
				} catch (Exception e) {
					log.warn("Failed to close McpAsyncClient: {}", asyncClient.getClientInfo().name(), e);
				}
				client2CountMap.remove(asyncClient.getClientInfo().name());
			}
			keyToClientMap.remove(metadataKey);
		}
	}
}
