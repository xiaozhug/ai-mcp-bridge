package io.xiaozhug.ai.mcp.registry.autoconfigure.consul;

import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterAutoConfiguration;
import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterContextFactory;
import io.xiaozhug.ai.mcp.registry.autoconfigure.event.ParentWebServerInitializedEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationListener;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

/**
 * Consul MCP 注册中心上下文工厂
 *
 * @author xiaozhug
 */
public class ConsulMcpMcpRegistryCenterContextFactory extends McpRegistryCenterContextFactory {

    public ConsulMcpMcpRegistryCenterContextFactory(List<McpRegistryCenterSpecification> specifications) {
        super(specifications);
    }

    @Override
    public void onApplicationEvent(ParentWebServerInitializedEvent event) {
        super.onApplicationEvent(event);
        GenericApplicationContext childContext = getChildContext();
        childContext.getBeanProvider(ConsulAutoServiceRegistrationListener.class)
                .ifAvailable(listener -> {
                    listener.onApplicationEvent(new WebServerInitializedEvent(event.getWebServer()) {
                        @Override
                        public WebServerApplicationContext getApplicationContext() {
                            return event.getApplicationContext();
                        }
                    });
                });
    }

    @Override
    public String getName() {
        return McpRegistryCenterAutoConfiguration.ConsulConfiguration.CONSUL_MCP_REGISTRY_CENTER_FACTORY_NAME;
    }
}