package io.xiaozhug.ai.mcp.registry.autoconfigure;

import io.xiaozhug.ai.mcp.registry.autoconfigure.event.ChildApplicationContextCreatedEvent;
import io.xiaozhug.ai.mcp.registry.autoconfigure.event.ParentWebServerInitializedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * MCP 注册中心上下文工厂
 *
 * @author xiaozhug
 */
@Slf4j
public abstract class McpRegistryCenterContextFactory extends NamedContextFactory<McpRegistryCenterContextFactory.McpRegistryCenterSpecification> implements ApplicationListener<ParentWebServerInitializedEvent> {

    public static final String MCP_PROPERTY_PREFIX = "mcp.registry-center";
    public static final String MCP_CONTEXT_ID = "mcp.registry-center";
    private static final YamlPropertySourceFactory yamlPropertySourceFactory = new YamlPropertySourceFactory();

    public McpRegistryCenterContextFactory(List<McpRegistryCenterContextFactory.McpRegistryCenterSpecification> specifications) {
        super(McpRegistryCenterConfiguration.class, MCP_PROPERTY_PREFIX, MCP_CONTEXT_ID);
        if(!CollectionUtils.isEmpty(specifications)){
            McpRegistryCenterSpecification defaultSpec = new McpRegistryCenterSpecification("default.extension-condition",
                    new Class<?>[]{McpApplicationContextProcessor.class});
            specifications.add(defaultSpec);
            setConfigurations(specifications);
        }
    }

    @Override
    public void onApplicationEvent(ParentWebServerInitializedEvent event) {
        GenericApplicationContext childContext = getChildContext();
        childContext.getBeanProvider(AbstractAutoServiceRegistration.class)
                        .ifAvailable(abstractAutoServiceRegistration -> {
                            abstractAutoServiceRegistration.onApplicationEvent(new WebServerInitializedEvent(event.getWebServer()) {
                                @Override
                                public WebServerApplicationContext getApplicationContext() {
                                    return event.getApplicationContext();
                                }
                            });
                            log.info("MCP Registry Center Auto Service Registration started with port: {}", event.getWebServer().getPort());
                        });
    }

    public GenericApplicationContext getChildContext() {
        try {
            boolean notContains = !getContextNames().contains(getName());
            if(notContains){
                ClassPathResource resource = new ClassPathResource("application-" + getName() + ".yml");
                PropertySource<?> propertySource = yamlPropertySourceFactory.createPropertySource(getName(), new EncodedResource(resource));
                ((ConfigurableEnvironment) getParent().getEnvironment()).getPropertySources().addFirst(propertySource);
            }

            // 解决spring-cloud-context-4.3.0.jar找不到方法问题
            Method getContextMethod = ReflectionUtils.findMethod(NamedContextFactory.class, "getContext", String.class);
            GenericApplicationContext applicationContext = (GenericApplicationContext) getContextMethod.invoke(this, getName());

            if (notContains) {
                getParent().publishEvent(new ChildApplicationContextCreatedEvent(applicationContext));
            }

            return applicationContext;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize MCP Registry Center Context for " + getName(), t);
        }
    }

    public static class McpRegistryCenterSpecification implements NamedContextFactory.Specification {
        private String name;
        private Class<?>[] configurations;

        public McpRegistryCenterSpecification(){}

        public McpRegistryCenterSpecification(String name, Class<?>[] configurations) {
            this.name = name;
            this.configurations = configurations;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<?>[] getConfiguration() {
            return configurations;
        }
    }

    protected abstract String getName();
}
