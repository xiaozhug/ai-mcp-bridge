package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.McpClientProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.AbstractMcpClientCreator;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 负载均衡MCP同步客户端
 *
 * @author xiaozhug
 */
@Slf4j
public class LoadbalancedMcpSyncClient extends LoadbalancedMcpClient<McpSyncClient> {

	public LoadbalancedMcpSyncClient(String serviceName, List<McpToolSpecification> metadatas, McpClientProperties mcpClientProperties,
									 AbstractMcpClientCreator mcpClientCreator) {
		super(serviceName, metadatas, mcpClientProperties, mcpClientCreator);

		init();
	}

	public McpSchema.CallToolResult callTool(McpSchema.CallToolRequest callToolRequest) {
		List<McpSyncClient> syncClients = new ArrayList<>();

		keyToToolNameMap.forEach((key, value) -> {
            if (value.contains(callToolRequest.name())) {
                List<McpSyncClient> clients = keyToClientMap.get(key);
                if (clients != null) {
					syncClients.addAll(clients);
                }
            }
        });

		McpSyncClient mcpSyncClient = chooseMcpClient(syncClients);
		try {
			return mcpSyncClient.callTool(callToolRequest);
		} catch (Throwable t) {
			log.error("Error calling tool {}: {}", callToolRequest.name(), t);
			return new McpSchema.CallToolResult(t.toString(), true);
		}
	}

	public McpSchema.ListToolsResult listTools() {
		List<McpToolSpecification> metadatas = getMetadatas();
		if(CollectionUtils.isEmpty(metadatas)){
			return new McpSchema.ListToolsResult(Collections.emptyList(), null);
		}

		return new McpSchema.ListToolsResult(
				metadatas.stream()
						.flatMap(metadata -> metadata.getTools().stream())
						.collect(Collectors.toMap(McpTool::getName, item -> item, (item1, item2) -> item1))
						.values().stream().map(tool -> {
							return new McpSchema.Tool(
									tool.getName(),
									tool.getDescription(),
									tool.getInputSchema()
							);
						})
						.collect(Collectors.toList()),
				null
		);
	}

	@Override
	protected McpSyncClient getMcpClient(List<McpSyncClient> syncClients) {
		if (syncClients.isEmpty()) {
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
		return syncClients.stream()
				.filter(syncClient -> syncClient.getClientInfo().name().equals(clientInfoName))
				.findFirst()
				.get();
	}

	@Override
	protected void createMcpClient(McpToolSpecification metadata) {
		try {
			McpSchema.Implementation clientInfo = new McpSchema.Implementation(
					this.connectedClientName(mcpClientProperties.getName(), metadata.getServiceName()  + "-" + metadata.getInstanceId()),
					mcpClientProperties.getVersion());

			McpSyncClient syncClient = mcpClientCreator.createMcpSyncClient(clientInfo, metadata);
			if (mcpClientProperties.isInitialized()) {
				syncClient.initialize();
				String key = metadata.getInstanceId() + "-" + metadata.getUrl();
				keyToClientMap.computeIfAbsent(key, k -> new ArrayList<>()).add(syncClient);
			}
			log.info("Added McpSyncClient: {}", clientInfo.name());

			client2CountMap.put(syncClient.getClientInfo().name(), 0);
		} catch (Throwable t) {
			log.error("Failed to create McpSyncClient for metadata: {}", metadata, t);
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
		List<McpSyncClient> clients = keyToClientMap.remove(metadataKey);
		if (clients != null) {
			for (McpSyncClient syncClient : clients) {
				try {
					syncClient.closeGracefully();
				} catch (Exception e) {
					log.warn("Failed to close McpSyncClient: {}", syncClient.getClientInfo().name(), e);
				}
			}
		}
	}
}
