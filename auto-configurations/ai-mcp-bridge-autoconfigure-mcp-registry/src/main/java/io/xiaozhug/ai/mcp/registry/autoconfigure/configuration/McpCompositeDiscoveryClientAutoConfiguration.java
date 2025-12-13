package io.xiaozhug.ai.mcp.registry.autoconfigure.configuration;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP复合发现客户端自动配置
 *
 * @author xiaozhug
 */
public class McpCompositeDiscoveryClientAutoConfiguration extends CompositeDiscoveryClientAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean
    @Primary
    @ConditionalOnBean(CompositeDiscoveryClientAutoConfiguration.class)
    public CompositeDiscoveryClient compositeDiscoveryClient(List<DiscoveryClient> discoveryClients) {
        return super.compositeDiscoveryClient(new ArrayList<>(applicationContext.getBeansOfType(DiscoveryClient.class).values()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
