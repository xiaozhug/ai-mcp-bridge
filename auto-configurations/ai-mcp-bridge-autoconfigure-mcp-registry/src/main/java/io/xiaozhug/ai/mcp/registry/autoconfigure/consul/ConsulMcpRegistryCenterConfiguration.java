package io.xiaozhug.ai.mcp.registry.autoconfigure.consul;

import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.*;
import org.springframework.cloud.consul.discovery.reactive.ConsulReactiveDiscoveryClientConfiguration;
import org.springframework.cloud.consul.serviceregistry.*;
import org.springframework.cloud.consul.support.ConsulHeartbeatAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Consul MCP 注册中心配置
 *
 * @author xiaozhug
 */
@Import(value = {
        ConsulAutoConfiguration.class,
        ConsulAutoServiceRegistrationAutoConfiguration.class,
        ConsulServiceRegistryAutoConfiguration.class,
        ConsulDiscoveryClientConfiguration.class,
        ConsulReactiveDiscoveryClientConfiguration.class,
        ConsulCatalogWatchAutoConfiguration.class,
        ConsulHeartbeatAutoConfiguration.class
})
public class ConsulMcpRegistryCenterConfiguration {

}
