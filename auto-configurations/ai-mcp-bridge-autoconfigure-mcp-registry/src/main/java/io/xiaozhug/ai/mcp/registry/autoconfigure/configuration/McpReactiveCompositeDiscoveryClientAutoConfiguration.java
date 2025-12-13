package io.xiaozhug.ai.mcp.registry.autoconfigure.configuration;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClient;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClientAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP反应式复合发现客户端自动配置
 *
 * @author xiaozhug
 */
public class McpReactiveCompositeDiscoveryClientAutoConfiguration extends ReactiveCompositeDiscoveryClientAutoConfiguration  implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Bean
    @Primary
    @ConditionalOnBean(ReactiveCompositeDiscoveryClientAutoConfiguration.class)
    public ReactiveCompositeDiscoveryClient reactiveCompositeDiscoveryClient(
            List<ReactiveDiscoveryClient> discoveryClients) {
        return super.reactiveCompositeDiscoveryClient(new ArrayList<>(applicationContext.getBeansOfType(ReactiveDiscoveryClient.class).values()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
