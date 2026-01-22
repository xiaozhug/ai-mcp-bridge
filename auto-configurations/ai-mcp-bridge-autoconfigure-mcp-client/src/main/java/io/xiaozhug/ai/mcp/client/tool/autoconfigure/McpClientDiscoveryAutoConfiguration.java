package io.xiaozhug.ai.mcp.client.tool.autoconfigure;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestTemplateMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.WebClientMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.McpClientProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryFetchProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.WebClientRequestHeaderExchangeFilterFunction;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.WebFluxMcpClientCreator;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.WebMvcMcpClientCreator;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

/**
 * MCP客户端自动配置
 *
 * @author xiaozhug
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({ExistNamedContextFactoryDiscoveryMcpFetchAutoConfiguration.class, NotExistNamedContextFactoryDiscoveryMcpFetchAutoConfiguration.class})
@ConditionalOnProperty(prefix = DiscoveryFetchProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
public class McpClientDiscoveryAutoConfiguration implements EnvironmentAware {

    private Environment environment;

    @Bean
    public McpClientProperties mcpClientProperties() {
        McpClientProperties mcpClientProperties = new McpClientProperties();
        mcpClientProperties.setName(environment.getProperty("spring.ai.mcp.client.name", "spring.ai.mcp.client"));
        mcpClientProperties.setVersion(environment.getProperty("spring.ai.mcp.client.version", " 1.0.0"));
        mcpClientProperties.setRequestTimeout(Duration.ofSeconds(Long.parseLong(environment.getProperty("spring.ai.mcp.client.request-timeout-seconds", "20"))));
        mcpClientProperties.setInitialized(Boolean.parseBoolean(environment.getProperty("spring.ai.mcp.client.initialized", "true")));
        return mcpClientProperties;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(WebFluxSseClientTransport.class)
    public static class WebFluxMcpClientCreatorConfiguration {

        @Bean
        public WebFluxMcpClientCreator webFluxMcpClientCreator(McpClientProperties mcpClientProperties,
                                                               @Qualifier("mcpWebClientExchangeFilter") List<ExchangeFilterFunction> exchangeFilterFunctions) {
            return new WebFluxMcpClientCreator(mcpClientProperties, exchangeFilterFunctions);
        }

        @Bean
        @Qualifier("mcpWebClientExchangeFilter")
        public WebClientRequestHeaderExchangeFilterFunction webClientRequestHeaderExchangeFilterFunction() {
            return new WebClientRequestHeaderExchangeFilterFunction();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(HttpClientSseClientTransport.class)
    @ConditionalOnMissingClass("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport")
    public static class WebMvcMcpClientCreatorConfiguration {

        @Bean
        public WebMvcMcpClientCreator webMvcMcpClientCreator(McpClientProperties mcpClientProperties) {
            return new WebMvcMcpClientCreator(mcpClientProperties);
        }
    }




    public final static String LOAD_BALANCED_REST_MCP_CLIENT_TRANSPORT_BEAN_NAME = "loadBalancedRestMcpClientTransport";

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(WebClient.class)
    public static class LoadBalancedWebClientMcpClientTransportConfiguration {

        public final static String LOAD_BALANCED_WEB_CLIENT_BUILDER_MCP_CLIENT_BEAN_NAME = "loadBalancedWebClientBuilderMcpClient";

        @Bean
        @LoadBalanced
        @Qualifier(LOAD_BALANCED_WEB_CLIENT_BUILDER_MCP_CLIENT_BEAN_NAME)
        @ConditionalOnMissingBean(name = LOAD_BALANCED_WEB_CLIENT_BUILDER_MCP_CLIENT_BEAN_NAME)
        public WebClient.Builder loadBalancedWebClientBuilderMcpClient() {
            return WebClient.builder();
        }

        @Bean
        @Qualifier(LOAD_BALANCED_REST_MCP_CLIENT_TRANSPORT_BEAN_NAME)
        public RestMcpClientTransport loadBalancedRestMcpClientTransport(@Qualifier(LOAD_BALANCED_WEB_CLIENT_BUILDER_MCP_CLIENT_BEAN_NAME) WebClient.Builder webClientBuilder) {
            return new WebClientMcpClientTransport(webClientBuilder);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestTemplate.class)
    @ConditionalOnMissingClass("org.springframework.web.reactive.function.client.WebClient")
    public static class LoadBalancedRestTemplateMcpClientTransportConfiguration {

        public final static String LOAD_BALANCED_REST_TEMPLATE_MCP_CLIENT_BEAN_NAME = "loadBalancedRestTemplateMcpClient";

        @Bean
        @LoadBalanced
        @Qualifier(LOAD_BALANCED_REST_TEMPLATE_MCP_CLIENT_BEAN_NAME)
        @ConditionalOnMissingBean(name = LOAD_BALANCED_REST_TEMPLATE_MCP_CLIENT_BEAN_NAME)
        public RestTemplate loadBalancedRestTemplateMcpClient() {
            return new RestTemplate();
        }

        @Bean
        @Qualifier(LOAD_BALANCED_REST_MCP_CLIENT_TRANSPORT_BEAN_NAME)
        public RestMcpClientTransport loadBalancedRestMcpClientTransport(@Qualifier(LOAD_BALANCED_REST_TEMPLATE_MCP_CLIENT_BEAN_NAME) RestTemplate restTemplate) {
            return new RestTemplateMcpClientTransport(restTemplate);
        }
    }
}
