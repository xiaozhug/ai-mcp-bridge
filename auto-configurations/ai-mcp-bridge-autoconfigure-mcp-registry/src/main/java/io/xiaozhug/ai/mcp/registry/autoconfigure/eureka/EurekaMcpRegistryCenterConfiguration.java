package io.xiaozhug.ai.mcp.registry.autoconfigure.eureka;

import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.cloud.netflix.eureka.*;
import org.springframework.cloud.netflix.eureka.config.DiscoveryClientOptionalArgsConfiguration;
import org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration;
import org.springframework.cloud.netflix.eureka.reactive.EurekaReactiveDiscoveryClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * Eureka MCP 注册中心配置
 *
 * @author xiaozhug
 */
@Import(value = {
        EurekaDiscoveryClientConfiguration.class,
        DiscoveryClientOptionalArgsConfiguration.class,
        LoadBalancerEurekaAutoConfiguration.class,
        EurekaReactiveDiscoveryClientConfiguration.class,
        EurekaClientAutoConfiguration.class
})
public class EurekaMcpRegistryCenterConfiguration {

    @Bean
    public static RefreshScope refreshScope() {
        return new RefreshScope();
    }

    @Bean
    @ConditionalOnBean(McpToolSpecification.class)
    public ApplicationInfoManagerBeanPostProcessor eurekaApplicationInfoManagerBeanPostProcessor(List<McpToolSpecification> mcpConfigurationMetadatas) {
        return new ApplicationInfoManagerBeanPostProcessor(mcpConfigurationMetadatas);
    }

}
