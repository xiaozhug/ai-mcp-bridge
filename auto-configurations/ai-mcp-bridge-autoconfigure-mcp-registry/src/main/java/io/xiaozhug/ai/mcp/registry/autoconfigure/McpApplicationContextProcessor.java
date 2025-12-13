package io.xiaozhug.ai.mcp.registry.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * @author xiaozhug
 */
public class McpApplicationContextProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    public static ThreadLocal <Boolean> mcpContextThreadLocal = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        mcpContextThreadLocal.set(true);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
