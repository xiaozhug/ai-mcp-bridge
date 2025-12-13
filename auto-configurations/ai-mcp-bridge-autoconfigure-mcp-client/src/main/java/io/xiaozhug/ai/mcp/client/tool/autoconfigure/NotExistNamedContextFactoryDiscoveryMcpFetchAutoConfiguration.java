package io.xiaozhug.ai.mcp.client.tool.autoconfigure;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.*;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool.DiscoveryFetchToolCallbackProvider;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool.LoadbalancedAsyncMcpToolCallbackProvider;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool.LoadbalancedSyncMcpToolCallbackProvider;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.*;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.AbstractMcpClientCreator;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static io.xiaozhug.ai.mcp.client.tool.autoconfigure.McpClientDiscoveryAutoConfiguration.LOAD_BALANCED_REST_MCP_CLIENT_TRANSPORT_BEAN_NAME;


/**
 * @author xiaozhug
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingClass("io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterContextFactory")
@EnableConfigurationProperties(DiscoveryFetchProperties.class)
@ConditionalOnProperty(prefix = DiscoveryFetchProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
public class NotExistNamedContextFactoryDiscoveryMcpFetchAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ToolCallback.class)
    public static class NotExistNamedContextFactoryDiscoveryFetchToolCallbackProviderConfiguration {

        @Bean
        public DiscoveryFetchToolCallbackProvider discoveryFetchToolCallbackProviderFromCurrent(DiscoveryMcpServerFetcher discoveryMcpServerFetcher,
                                                                                                DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager) {
            return new DiscoveryFetchToolCallbackProvider(discoveryMcpServerFetcher, discoveryMcpServerProcessorManager);
        }

        @Bean
        @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "SYNC",
                matchIfMissing = true)
        public LoadbalancedSyncMcpToolCallbackProvider loadbalancedSyncMcpToolCallbackProvider(DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
                                                                                               LoadbalancedMcpClientManager<LoadbalancedMcpSyncClient> loadbalancedMcpClientManager) {
            return new LoadbalancedSyncMcpToolCallbackProvider(discoveryMcpServerProcessorManager, loadbalancedMcpClientManager);
        }

        @Bean
        @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "ASYNC")
        public LoadbalancedAsyncMcpToolCallbackProvider loadbalancedAsyncMcpToolCallbackProvider(DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
                                                                                                 LoadbalancedMcpClientManager<LoadbalancedMcpAsyncClient> loadbalancedMcpClientManager) {
            return new LoadbalancedAsyncMcpToolCallbackProvider(discoveryMcpServerProcessorManager, loadbalancedMcpClientManager);
        }
    }

    @Bean
    public DiscoveryMcpServerFetcher discoveryMcpFetch(DiscoveryClient discoveryClient, DiscoveryFetchProperties discoveryFetchProperties) {
        return new DiscoveryMcpServerFetcher(discoveryClient, discoveryFetchProperties);
    }

    @Bean
    public DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager(List<DiscoveryMcpServerProcessor> processors, DiscoveryMcpServerFetcher discoveryMcpServerFetcher) {
        return new DiscoveryMcpServerProcessorManager(processors, discoveryMcpServerFetcher);
    }

    @Bean
    public HttpClientDiscoveryProcessor httpClientDiscoveryMcpServerProcessor(@Qualifier(LOAD_BALANCED_REST_MCP_CLIENT_TRANSPORT_BEAN_NAME) RestMcpClientTransport restMcpClientTransport) {
        return new HttpClientDiscoveryProcessor(restMcpClientTransport);
    }

    @Bean
    @ConditionalOnBean(LoadbalancedMcpAsyncClient.class)
    public McpAsyncClientDiscoveryProcessor mcpAsyncClientDiscoveryProcessor(LoadbalancedMcpClientManager<LoadbalancedMcpAsyncClient> loadbalancedMcpClientManager) {
        return new McpAsyncClientDiscoveryProcessor(loadbalancedMcpClientManager);
    }

    @Bean
    @ConditionalOnBean(LoadbalancedMcpSyncClient.class)
    public McpSyncClientDiscoveryProcessor mcpSyncClientDiscoveryProcessor(LoadbalancedMcpClientManager<LoadbalancedMcpSyncClient> loadbalancedMcpClientManager) {
        return new McpSyncClientDiscoveryProcessor(loadbalancedMcpClientManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "SYNC",
            matchIfMissing = true)
    public LoadbalancedMcpSyncClientManager loadbalancedMcpSyncClientManager(DiscoveryMcpServerFetcher discoveryMcpServerFetcher, McpClientProperties mcpClientProperties,
                                                                             AbstractMcpClientCreator mcpClientCreator) {
        return new LoadbalancedMcpSyncClientManager(discoveryMcpServerFetcher, mcpClientProperties, mcpClientCreator);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "ASYNC")
    public LoadbalancedMcpAsyncClientManager loadbalancedMcpAsyncClientManager(DiscoveryMcpServerFetcher discoveryMcpServerFetcher, McpClientProperties mcpClientProperties,
                                                                               AbstractMcpClientCreator mcpClientCreator) {
        return new LoadbalancedMcpAsyncClientManager(discoveryMcpServerFetcher, mcpClientProperties, mcpClientCreator);
    }

}
