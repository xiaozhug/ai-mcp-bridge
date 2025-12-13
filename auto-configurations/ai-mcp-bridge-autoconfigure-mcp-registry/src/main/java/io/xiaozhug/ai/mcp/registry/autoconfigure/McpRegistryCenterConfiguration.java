package io.xiaozhug.ai.mcp.registry.autoconfigure;

import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.registry.autoconfigure.configuration.McpCompositeDiscoveryClientAutoConfiguration;
import io.xiaozhug.ai.mcp.registry.autoconfigure.configuration.McpReactiveCompositeDiscoveryClientAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

/**
 * MCP 注册中心配置
 *
 * @author xiaozhug
 */
@Import(value = {
        McpCompositeDiscoveryClientAutoConfiguration.class,
        McpReactiveCompositeDiscoveryClientAutoConfiguration.class
})
public class McpRegistryCenterConfiguration {

    @Bean
    @ConditionalOnBean(McpToolSpecification.class)
    public InstancePreRegisteredApplicationListener instancePreRegisteredApplicationListener(List<McpToolSpecification> mcpConfigurationMetadatas){
        return new InstancePreRegisteredApplicationListener(mcpConfigurationMetadatas);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(ContextRefreshedEvent event) {
        AbstractApplicationContext applicationContext = (AbstractApplicationContext) event.getApplicationContext();
        ApplicationContext parent = applicationContext.getParent();
        if (parent != null) {
            McpRegistryCenterContextFactory mcpRegistryCenterContextFactory = parent.getBean(McpRegistryCenterContextFactory.class);
            ((ConfigurableEnvironment) parent.getEnvironment()).getPropertySources().remove(mcpRegistryCenterContextFactory.getName());
            McpApplicationContextProcessor.mcpContextThreadLocal.set(false);
        }
    }
}
