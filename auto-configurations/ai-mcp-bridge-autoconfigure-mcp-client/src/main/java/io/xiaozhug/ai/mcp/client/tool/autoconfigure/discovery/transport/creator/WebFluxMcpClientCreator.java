package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.McpClientProperties;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * WebFlux MCP客户端创建者
 *
 * @author xiaozhug
 */
public class WebFluxMcpClientCreator extends AbstractMcpClientCreator {

	private final WebClient.Builder webClientBuilder;

	public WebFluxMcpClientCreator(McpClientProperties mcpClientProperties, List<ExchangeFilterFunction> exchangeFilterFunctions) {
		super(mcpClientProperties);
        this.webClientBuilder = WebClient.builder().filters(filters -> {
			if (exchangeFilterFunctions != null) {
				filters.addAll(exchangeFilterFunctions);
			}
		});
    }

	@Override
	public McpSyncClient createMcpSyncClient(McpSchema.Implementation clientInfo, McpToolSpecification metadata) {
		WebClient.Builder builder = webClientBuilder.baseUrl(metadata.getUrl());
		WebFluxSseClientTransport transport = new WebFluxSseClientTransport(builder);

		McpClient.SyncSpec syncSpec = McpClient.sync(transport)
				.clientInfo(clientInfo)
				.requestTimeout(mcpClientProperties.getRequestTimeout());
		return syncSpec.build();
	}

	@Override
	public McpAsyncClient createMcpAsyncClient(McpSchema.Implementation clientInfo, McpToolSpecification metadata) {
		WebClient.Builder builder = webClientBuilder.baseUrl(metadata.getUrl());
//			webClientBuilder.filter()
		WebFluxSseClientTransport transport = new WebFluxSseClientTransport(builder);

		McpClient.AsyncSpec asyncSpec = McpClient.async(transport)
				.clientInfo(clientInfo)
				.requestTimeout(mcpClientProperties.getRequestTimeout());
		return asyncSpec.build();
	}
}