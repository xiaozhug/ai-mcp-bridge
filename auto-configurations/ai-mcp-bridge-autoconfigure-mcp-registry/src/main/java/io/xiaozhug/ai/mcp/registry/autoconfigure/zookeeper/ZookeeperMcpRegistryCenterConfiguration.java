package io.xiaozhug.ai.mcp.registry.autoconfigure.zookeeper;

import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.*;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependenciesAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.reactive.ZookeeperReactiveDiscoveryClientConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperAutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistryAutoConfiguration;
import org.springframework.cloud.zookeeper.support.CuratorServiceDiscoveryAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Zookeeper MCP 注册中心配置
 *
 * @author xiaozhug
 */
@Import(value = {
        ZookeeperAutoConfiguration.class,
        CuratorServiceDiscoveryAutoConfiguration.class,
        ZookeeperServiceRegistryAutoConfiguration.class,
        ZookeeperAutoServiceRegistrationAutoConfiguration.class,
        ZookeeperDiscoveryAutoConfiguration.class,
        LoadBalancerZookeeperAutoConfiguration.class,
        ZookeeperReactiveDiscoveryClientConfiguration.class,
        ZookeeperDependenciesAutoConfiguration.class,
        ZookeeperDiscoveryClientConfiguration.class
})
public class ZookeeperMcpRegistryCenterConfiguration {
}
