package io.xiaozhug.ai.mcp.registry.autoconfigure.nacos;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Nacos 自动配置导入选择器
 *
 * @author xiaozhug
 */
public class NacosAutoConfigurationImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        try {
            Class.forName("com.alibaba.cloud.nacos.discovery.NacosDiscoveryHeartBeatConfiguration");
        } catch (ClassNotFoundException e) {
            return new String[]{
                    "com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration",
                    "com.alibaba.cloud.nacos.endpoint.NacosDiscoveryEndpointAutoConfiguration",
                    "com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration",
                    "com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration",
                    "com.alibaba.cloud.nacos.discovery.reactive.NacosReactiveDiscoveryClientConfiguration",
                    "com.alibaba.cloud.nacos.discovery.configclient.NacosConfigServerAutoConfiguration",
                    "com.alibaba.cloud.nacos.loadbalancer.LoadBalancerNacosAutoConfiguration",
                    "com.alibaba.cloud.nacos.NacosServiceAutoConfiguration",
                    "com.alibaba.cloud.nacos.utils.UtilIPv6AutoConfiguration"
            };
        }

        return new String[]{
                "com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration",
                "com.alibaba.cloud.nacos.endpoint.NacosDiscoveryEndpointAutoConfiguration",
                "com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration",
                "com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration",
                "com.alibaba.cloud.nacos.discovery.NacosDiscoveryHeartBeatConfiguration",
                "com.alibaba.cloud.nacos.discovery.reactive.NacosReactiveDiscoveryClientConfiguration",
                "com.alibaba.cloud.nacos.discovery.configclient.NacosConfigServerAutoConfiguration",
                "com.alibaba.cloud.nacos.loadbalancer.LoadBalancerNacosAutoConfiguration",
                "com.alibaba.cloud.nacos.NacosServiceAutoConfiguration",
                "com.alibaba.cloud.nacos.util.UtilIPv6AutoConfiguration"

        };
    }
}
