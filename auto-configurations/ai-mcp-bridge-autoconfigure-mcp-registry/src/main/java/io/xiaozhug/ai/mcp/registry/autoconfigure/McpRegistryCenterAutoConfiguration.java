package io.xiaozhug.ai.mcp.registry.autoconfigure;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.registry.autoconfigure.consul.ConsulMcpMcpRegistryCenterContextFactory;
import io.xiaozhug.ai.mcp.registry.autoconfigure.consul.ConsulMcpRegistryCenterConfiguration;
import io.xiaozhug.ai.mcp.registry.autoconfigure.eureka.ApplicationInfoManagerBeanPostProcessor;
import io.xiaozhug.ai.mcp.registry.autoconfigure.eureka.EurekaMcpMcpRegistryCenterContextFactory;
import io.xiaozhug.ai.mcp.registry.autoconfigure.eureka.EurekaMcpRegistryCenterConfiguration;
import io.xiaozhug.ai.mcp.registry.autoconfigure.event.ParentWebServerInitializedEvent;
import io.xiaozhug.ai.mcp.registry.autoconfigure.nacos.NacosMcpMcpRegistryCenterContextFactory;
import io.xiaozhug.ai.mcp.registry.autoconfigure.nacos.NacosMcpRegistryCenterConfiguration;
import io.xiaozhug.ai.mcp.registry.autoconfigure.zookeeper.ZookeeperMcpMcpRegistryCenterContextFactory;
import io.xiaozhug.ai.mcp.registry.autoconfigure.zookeeper.ZookeeperMcpRegistryCenterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

/**
 * MCP 注册中心自动配置
 *
 * @author xiaozhug
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(name = {
        "io.xiaozhug.ai.mcp.metadata.file.expose.autoconfigure.McpMetadataExposeAutoConfiguration",
        "io.xiaozhug.ai.mcp.server.expose.autoconfigure.McpServerExposeAutoConfiguration"
})
public class McpRegistryCenterAutoConfiguration {

    public static final String DEFAULT_MCP_REGISTRY_CENTER_FACTORY_NAME = "mcp-default";

    @Autowired(required = false)
    private McpRegistryCenterContextFactory mcpRegistryCenterContextFactory;

    @EventListener(WebServerInitializedEvent.class)
    public void onParentWebServerInitialized(WebServerInitializedEvent event) {
        if(mcpRegistryCenterContextFactory != null){
            mcpRegistryCenterContextFactory.getChildContext().publishEvent(new ParentWebServerInitializedEvent(event.getWebServer()) {
                @Override
                public WebServerApplicationContext getApplicationContext() {
                    return event.getApplicationContext();
                }
            });
        }
    }

    @ConditionalOnClass({EurekaClientAutoConfiguration.class})
    public static class EurekaConfiguration {

        public static final String EUREKA_MCP_REGISTRY_CENTER_FACTORY_NAME = "mcp-eureka";

        @Bean
        @ConditionalOnProperty(prefix = "mcp", name = "registry", havingValue = "eureka")
        public EurekaMcpMcpRegistryCenterContextFactory eurekaMcpMcpRegistryCenterContextFactory(List<McpRegistryCenterContextFactory.McpRegistryCenterSpecification> specifications) {
            return new EurekaMcpMcpRegistryCenterContextFactory(specifications);
        }

        @Bean
        @ConditionalOnProperty(prefix = "mcp", name = "registry", havingValue = "eureka")
        public McpRegistryCenterContextFactory.McpRegistryCenterSpecification eurekaMcpRegistryCenterConfigurationSpecification () {
            return new McpRegistryCenterContextFactory.McpRegistryCenterSpecification(EUREKA_MCP_REGISTRY_CENTER_FACTORY_NAME, new Class<?>[]{
                    EurekaMcpRegistryCenterConfiguration.class
            });
        }

        @Bean
        @ConditionalOnBean(McpToolSpecification.class)
        @ConditionalOnMissingBean(McpRegistryCenterContextFactory.class)
        public ApplicationInfoManagerBeanPostProcessor eurekaApplicationInfoManagerBeanPostProcessor(List<McpToolSpecification> mcpConfigurationMetadatas) {
            return new ApplicationInfoManagerBeanPostProcessor(mcpConfigurationMetadatas);
        }
    }

    @ConditionalOnClass({ZookeeperAutoConfiguration.class})
    public static class ZookeeperConfiguration {

        public static final String ZOOKEEPER_MCP_REGISTRY_CENTER_FACTORY_NAME = "mcp-zookeeper";

        @Bean
        @ConditionalOnProperty(prefix = "mcp", name = "registry", havingValue = "zookeeper")
        public ZookeeperMcpMcpRegistryCenterContextFactory zookeeperMcpMcpRegistryCenterContextFactory(List<McpRegistryCenterContextFactory.McpRegistryCenterSpecification> specifications) {
            return new ZookeeperMcpMcpRegistryCenterContextFactory(specifications);
        }

        @Bean
        @ConditionalOnProperty(prefix = "mcp", name = "registry", havingValue = "zookeeper")
        public McpRegistryCenterContextFactory.McpRegistryCenterSpecification zookeeperMcpRegistryCenterConfigurationSpecification () {
            return new McpRegistryCenterContextFactory.McpRegistryCenterSpecification(
                    ZOOKEEPER_MCP_REGISTRY_CENTER_FACTORY_NAME, new Class<?>[]{
                    ZookeeperMcpRegistryCenterConfiguration.class
            });
        }
    }

    @ConditionalOnClass({ConsulAutoConfiguration.class})
    public static class ConsulConfiguration {

        public static final String CONSUL_MCP_REGISTRY_CENTER_FACTORY_NAME = "mcp-consul";

        @Bean
        @ConditionalOnProperty(prefix = "mcp", name = "registry", havingValue = "consul")
        public ConsulMcpMcpRegistryCenterContextFactory consulMcpMcpRegistryCenterContextFactory(List<McpRegistryCenterContextFactory.McpRegistryCenterSpecification> specifications) {
            return new ConsulMcpMcpRegistryCenterContextFactory(specifications);
        }

        @Bean
        @ConditionalOnProperty(prefix = "mcp", name = "registry", havingValue = "consul")
        public McpRegistryCenterContextFactory.McpRegistryCenterSpecification consulMcpRegistryCenterConfigurationSpecification () {
            return new McpRegistryCenterContextFactory.McpRegistryCenterSpecification(
                    CONSUL_MCP_REGISTRY_CENTER_FACTORY_NAME, new Class<?>[]{
                    ConsulMcpRegistryCenterConfiguration.class
            });
        }
    }

    @ConditionalOnClass({NacosDiscoveryAutoConfiguration.class})
    public static class NacosConfiguration {

        public static final String NACOS_MCP_REGISTRY_CENTER_FACTORY_NAME = "mcp-nacos";

        @Bean
        @ConditionalOnProperty(prefix = "mcp", name = "registry", havingValue = "nacos")
        public NacosMcpMcpRegistryCenterContextFactory nacosMcpMcpRegistryCenterContextFactory(List<McpRegistryCenterContextFactory.McpRegistryCenterSpecification> specifications) {
            return new NacosMcpMcpRegistryCenterContextFactory(specifications);
        }

        @Bean
        @ConditionalOnProperty(prefix = "mcp", name = "registry", havingValue = "nacos")
        public McpRegistryCenterContextFactory.McpRegistryCenterSpecification nacosMcpRegistryCenterConfigurationSpecification () {
            return new McpRegistryCenterContextFactory.McpRegistryCenterSpecification(
                    NACOS_MCP_REGISTRY_CENTER_FACTORY_NAME, new Class<?>[]{
                    NacosMcpRegistryCenterConfiguration.class
            });
        }
    }

    @Bean
    @ConditionalOnMissingBean(McpRegistryCenterContextFactory.class)
    @ConditionalOnBean(McpToolSpecification.class)
    public InstancePreRegisteredApplicationListener instancePreRegisteredApplicationListener(List<McpToolSpecification> mcpConfigurationMetadatas){
        return new InstancePreRegisteredApplicationListener(mcpConfigurationMetadatas);
    }
}
