package io.xiaozhug.ai.mcp.client.tool.autoconfigure;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.*;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool.DiscoveryFetchToolCallbackProvider;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool.LoadbalancedAsyncMcpToolCallbackProvider;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool.LoadbalancedSyncMcpToolCallbackProvider;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.*;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.AbstractMcpClientCreator;
import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterAutoConfiguration;
import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterContextFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

import static io.xiaozhug.ai.mcp.client.tool.autoconfigure.McpClientDiscoveryAutoConfiguration.LOAD_BALANCED_REST_MCP_CLIENT_TRANSPORT_BEAN_NAME;
import static io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterAutoConfiguration.DEFAULT_MCP_REGISTRY_CENTER_FACTORY_NAME;

/**
 * 基于已存在命名上下文工厂的发现式MCP获取自动配置
 *
 * @author xiaozhug
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(McpRegistryCenterContextFactory.class)
@AutoConfigureAfter(McpRegistryCenterAutoConfiguration.class)
@EnableConfigurationProperties(DiscoveryFetchProperties.class)
@ConditionalOnProperty(prefix = DiscoveryFetchProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
public class ExistNamedContextFactoryDiscoveryMcpFetchAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ToolCallback.class)
    public static class ExistNamedContextFactoryDiscoveryFetchToolCallbackProviderConfiguration {

        @Bean
        @ConditionalOnMissingBean(McpRegistryCenterContextFactory.class)
        public DiscoveryFetchToolCallbackProvider discoveryFetchToolCallbackProviderFromCurrent(DiscoveryMcpServerFetcher discoveryMcpServerFetcher,
                                                                                                DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager) {
            return new DiscoveryFetchToolCallbackProvider(discoveryMcpServerFetcher, discoveryMcpServerProcessorManager);
        }

        @Bean
        @ConditionalOnBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
        public DiscoveryFetchToolCallbackProvider discoveryFetchToolCallbackProvider(McpRegistryCenterContextFactory mcpRegistryCenterContextFactory) {
            GenericApplicationContext childContext = mcpRegistryCenterContextFactory.getChildContext();
            DiscoveryMcpServerFetcher discoveryMcpServerFetcher = childContext.getBean(DiscoveryMcpServerFetcher.class);
            DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager = childContext.getBean(DiscoveryMcpServerProcessorManager.class);
            return new DiscoveryFetchToolCallbackProvider(discoveryMcpServerFetcher, discoveryMcpServerProcessorManager);
        }

        @Bean
        @ConditionalOnMissingBean(McpRegistryCenterContextFactory.class)
        @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "SYNC",
                matchIfMissing = true)
        public LoadbalancedSyncMcpToolCallbackProvider loadbalancedSyncMcpToolCallbackProviderFromCurrent(DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
                                                                                                          LoadbalancedMcpClientManager<LoadbalancedMcpSyncClient> loadbalancedMcpClientManager) {
            return new LoadbalancedSyncMcpToolCallbackProvider(discoveryMcpServerProcessorManager, loadbalancedMcpClientManager);
        }

        @Bean
        @ConditionalOnMissingBean(McpRegistryCenterContextFactory.class)
        @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "ASYNC")
        public LoadbalancedAsyncMcpToolCallbackProvider loadbalancedAsyncMcpToolCallbackProviderFromCurrent(DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
                                                                                                            LoadbalancedMcpClientManager<LoadbalancedMcpAsyncClient> loadbalancedMcpClientManager) {
            return new LoadbalancedAsyncMcpToolCallbackProvider(discoveryMcpServerProcessorManager, loadbalancedMcpClientManager);
        }


        @Bean
        @ConditionalOnBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
        @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "SYNC",
                matchIfMissing = true)
        public LoadbalancedSyncMcpToolCallbackProvider loadbalancedSyncMcpToolCallbackProvider(DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
                                                                                               McpRegistryCenterContextFactory mcpRegistryCenterContextFactory) {
            GenericApplicationContext childContext = mcpRegistryCenterContextFactory.getChildContext();
            return new LoadbalancedSyncMcpToolCallbackProvider(discoveryMcpServerProcessorManager, childContext.getBean(LoadbalancedMcpClientManager.class));
        }

        @Bean
        @ConditionalOnBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
        @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "ASYNC")
        public LoadbalancedAsyncMcpToolCallbackProvider loadbalancedAsyncMcpToolCallbackProvider(DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
                                                                                                 McpRegistryCenterContextFactory mcpRegistryCenterContextFactory) {
            GenericApplicationContext childContext = mcpRegistryCenterContextFactory.getChildContext();
            return new LoadbalancedAsyncMcpToolCallbackProvider(discoveryMcpServerProcessorManager, childContext.getBean(LoadbalancedMcpAsyncClientManager.class));
        }

    }

    @Bean
    @ConditionalOnBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
    public McpRegistryCenterContextFactory.McpRegistryCenterSpecification eurekaMcpRegistryCenterDiscoveryConfigurationSpecification () {
        return new McpRegistryCenterContextFactory.McpRegistryCenterSpecification(
                "default." + DEFAULT_MCP_REGISTRY_CENTER_FACTORY_NAME, new Class<?>[]{ExistNamedContextFactoryDiscoveryMcpFetchAutoConfiguration.class}
        );
    }

    @Bean
    @ConditionalOnMissingBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
    public DiscoveryMcpServerFetcher discoveryMcpFetch(DiscoveryClient discoveryClient, DiscoveryFetchProperties discoveryFetchProperties) {
        return new DiscoveryMcpServerFetcher(discoveryClient, discoveryFetchProperties);
    }

    @Bean
    @ConditionalOnMissingBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
    public DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager(GenericApplicationContext context, List<DiscoveryMcpServerProcessor> processors, DiscoveryMcpServerFetcher discoveryMcpServerFetcher) {
        DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager = new DiscoveryMcpServerProcessorManager(processors, discoveryMcpServerFetcher);

        GenericApplicationContext parent = (GenericApplicationContext) context.getParent();
        if(parent != null){
            parent.getDefaultListableBeanFactory().registerSingleton("discoveryMcpServerProcessorManager", discoveryMcpServerProcessorManager);
        }

        return discoveryMcpServerProcessorManager;
    }

    @Bean
    @ConditionalOnMissingBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
    public HttpClientDiscoveryProcessor httpClientDiscoveryMcpServerProcessor(@Qualifier(LOAD_BALANCED_REST_MCP_CLIENT_TRANSPORT_BEAN_NAME) RestMcpClientTransport restMcpClientTransport) {
        return new HttpClientDiscoveryProcessor(restMcpClientTransport);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "ASYNC")
    @ConditionalOnMissingBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
    public McpAsyncClientDiscoveryProcessor mcpAsyncClientDiscoveryProcessor(LoadbalancedMcpClientManager<LoadbalancedMcpAsyncClient> loadbalancedMcpClientManager) {
        return new McpAsyncClientDiscoveryProcessor(loadbalancedMcpClientManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "SYNC",
            matchIfMissing = true)
    @ConditionalOnMissingBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
    public McpSyncClientDiscoveryProcessor mcpSyncClientDiscoveryProcessor(LoadbalancedMcpClientManager<LoadbalancedMcpSyncClient> loadbalancedMcpClientManager) {
        return new McpSyncClientDiscoveryProcessor(loadbalancedMcpClientManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "SYNC",
            matchIfMissing = true)
    @ConditionalOnMissingBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
    public LoadbalancedMcpSyncClientManager loadbalancedMcpSyncClientManager(DiscoveryMcpServerFetcher discoveryMcpServerFetcher, McpClientProperties mcpClientProperties,
                                                                             AbstractMcpClientCreator mcpClientCreator) {
        return new LoadbalancedMcpSyncClientManager(discoveryMcpServerFetcher, mcpClientProperties, mcpClientCreator);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.mcp.client", name = { "type" }, havingValue = "ASYNC")
    @ConditionalOnMissingBean(value = McpRegistryCenterContextFactory.class, search = SearchStrategy.CURRENT)
    public LoadbalancedMcpAsyncClientManager loadbalancedMcpAsyncClientManager(DiscoveryMcpServerFetcher discoveryMcpServerFetcher, McpClientProperties mcpClientProperties,
                                                                               AbstractMcpClientCreator mcpClientCreator) {
        return new LoadbalancedMcpAsyncClientManager(discoveryMcpServerFetcher, mcpClientProperties, mcpClientCreator);
    }

}
