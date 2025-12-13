package io.xiaozhug.ai.mcp.client.tool.autoconfigure;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestTemplateMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.WebClientMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.http.HttpFetchProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.http.HttpFetchToolCallbackProvider;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp.McpFetchProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp.SyncHttpToolCallbackProvider;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp.AsyncMcpFetchToolCallbackProvider;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static io.xiaozhug.ai.mcp.client.tool.autoconfigure.McpClientAutoConfiguration.RestMcpClientTransportConfiguration.REST_MCP_CLIENT_TRANSPORT_BEAN_NAME;

/**
 * MCP客户端到HTTP自动配置
 *
 * @author xiaozhug
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(McpClientDiscoveryAutoConfiguration.class)
@EnableConfigurationProperties({HttpFetchProperties.class, McpFetchProperties.class})
@ConditionalOnClass(ToolCallback.class)
public class McpClientAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "SYNC",
            matchIfMissing = true)
    @ConditionalOnProperty(prefix = McpFetchProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    public SyncHttpToolCallbackProvider syncHttpToolCallbackProvider(ObjectProvider<List<McpSyncClient>> mcpClientsProvider,
                                                                     McpSseClientProperties sseProperties,
                                                                     @Qualifier(REST_MCP_CLIENT_TRANSPORT_BEAN_NAME)
                                                                     RestMcpClientTransport restMcpClientTransport) {
        List<McpSyncClient> mcpClients = mcpClientsProvider.stream().flatMap(List::stream).toList();
        return new SyncHttpToolCallbackProvider(mcpClients, sseProperties, restMcpClientTransport);
    }

    @Bean
    @ConditionalOnProperty(prefix = McpFetchProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    @ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "ASYNC")
    public AsyncMcpFetchToolCallbackProvider asyncHttpToolCallbackProvider(ObjectProvider<List<McpAsyncClient>> mcpClientsProvider,
                                                                           McpSseClientProperties sseProperties,
                                                                           @Qualifier(REST_MCP_CLIENT_TRANSPORT_BEAN_NAME)
                                                                           RestMcpClientTransport restMcpClientTransport) {
        List<McpAsyncClient> mcpClients = mcpClientsProvider.stream().flatMap(List::stream).toList();
        return new AsyncMcpFetchToolCallbackProvider(mcpClients, sseProperties, restMcpClientTransport);
    }

    @Bean
    @ConditionalOnProperty(prefix = HttpFetchProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    public HttpFetchToolCallbackProvider httpFetchToolCallbackProvider(HttpFetchProperties httpFetchProperties,
                                                                       @Qualifier(REST_MCP_CLIENT_TRANSPORT_BEAN_NAME)
                                                                       RestMcpClientTransport restMcpClientTransport) {
        return new HttpFetchToolCallbackProvider(httpFetchProperties, restMcpClientTransport);
    }

    @Configuration(proxyBeanMethods = false)
    @Conditional(ToolCallbackProvider.class)
    public static class RestMcpClientTransportConfiguration {

        public final static String REST_MCP_CLIENT_TRANSPORT_BEAN_NAME = "mcpClientAutoConfigurationRestMcpClientTransport";

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass(WebClient.class)
        public static class WebClientMcpClientTransportConfiguration {

            public final static String WEB_CLIENT_BUILDER_MCP_CLIENT_BEAN_NAME = "webClientMcpClientBuilder";

            @Bean
            @Qualifier(WEB_CLIENT_BUILDER_MCP_CLIENT_BEAN_NAME)
            @ConditionalOnMissingBean(name = WEB_CLIENT_BUILDER_MCP_CLIENT_BEAN_NAME)
            public WebClient.Builder webClientMcpClientBuilder() {
                return WebClient.builder();
            }

            @Bean
            @Qualifier(REST_MCP_CLIENT_TRANSPORT_BEAN_NAME)
            public RestMcpClientTransport restMcpClientTransport(@Qualifier(WEB_CLIENT_BUILDER_MCP_CLIENT_BEAN_NAME) WebClient.Builder webClientBuilder) {
                return new WebClientMcpClientTransport(webClientBuilder);
            }
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass(RestTemplate.class)
        @ConditionalOnMissingClass("org.springframework.web.reactive.function.client.WebClient")
        public static class RestTemplateMcpClientTransportConfiguration {

            public final static String REST_TEMPLATE_MCP_CLIENT_BEAN_NAME = "restTemplateMcpClient";

            @Bean
            @Qualifier(REST_TEMPLATE_MCP_CLIENT_BEAN_NAME)
            @ConditionalOnMissingBean(name = REST_TEMPLATE_MCP_CLIENT_BEAN_NAME)
            public RestTemplate restTemplateMcpClient() {
                return new RestTemplate();
            }

            @Bean
            @Qualifier(REST_MCP_CLIENT_TRANSPORT_BEAN_NAME)
            public RestMcpClientTransport restMcpClientTransport(@Qualifier(REST_TEMPLATE_MCP_CLIENT_BEAN_NAME) RestTemplate restTemplate) {
                return new RestTemplateMcpClientTransport(restTemplate);
            }
        }
    }

    private static class ToolCallbackProvider extends AnyNestedCondition {

        ToolCallbackProvider() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(prefix = McpFetchProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
        static class OnHttpToolCallbackProvider {

        }

        @ConditionalOnProperty(prefix = HttpFetchProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
        static class OnHttpFetchToolCallbackProvider {

        }

    }

}
