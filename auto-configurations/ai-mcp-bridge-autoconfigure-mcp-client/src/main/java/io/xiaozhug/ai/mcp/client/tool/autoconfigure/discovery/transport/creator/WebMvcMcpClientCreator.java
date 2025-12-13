package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.McpClientProperties;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.net.http.HttpClient;

/**
 * WebMvc MCP客户端创建者
 *
 * @author xiaozhug
 */
public class WebMvcMcpClientCreator extends AbstractMcpClientCreator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String sseEndpoint = "/sse";
    private final InterceptingRequestBuilder interceptingRequestBuilder = new InterceptingRequestBuilder();

    public WebMvcMcpClientCreator(McpClientProperties mcpClientProperties) {
        super(mcpClientProperties);
    }

    @Override
    public McpSyncClient createMcpSyncClient(McpSchema.Implementation clientInfo, McpToolSpecification metadata) {
        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(metadata.getUrl())
                .sseEndpoint(sseEndpoint)
                .clientBuilder(HttpClient.newBuilder())
                .requestBuilder(interceptingRequestBuilder.build())
                .objectMapper(objectMapper)
                .build();

        McpClient.SyncSpec syncSpec = McpClient.sync(transport)
                .clientInfo(clientInfo)
                .requestTimeout(mcpClientProperties.getRequestTimeout());
        return syncSpec.build();
    }

    @Override
    public McpAsyncClient createMcpAsyncClient(McpSchema.Implementation clientInfo, McpToolSpecification metadata) {
        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(metadata.getUrl())
                .sseEndpoint(sseEndpoint)
                .clientBuilder(HttpClient.newBuilder())
                .requestBuilder(interceptingRequestBuilder.build())
                .objectMapper(objectMapper)
                .build();

        McpClient.AsyncSpec asyncSpec = McpClient.async(transport)
                .clientInfo(clientInfo)
                .requestTimeout(mcpClientProperties.getRequestTimeout());
        return asyncSpec.build();
    }
}
