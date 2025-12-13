package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerFetcher;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerRefreshEvent;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.McpClientProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.AbstractMcpClientCreator;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡MCP客户端管理器
 *
 * @author xiaozhug
 */
@Slf4j
public abstract class LoadbalancedMcpClientManager<T extends LoadbalancedMcpClient<?>> implements InitializingBean, ApplicationListener<DiscoveryMcpServerRefreshEvent>, BeanFactoryAware {

    protected final static String BEAN_NAME_PREFIX = "loadbalancedMcpClient-";
    protected final DiscoveryMcpServerFetcher discoveryMcpServerFetcher;
    protected final McpClientProperties mcpClientProperties;
    protected final AbstractMcpClientCreator mcpClientCreator;
    protected DefaultListableBeanFactory beanFactory;
    protected Class<T> resolvedGenericClass;

    public LoadbalancedMcpClientManager(DiscoveryMcpServerFetcher discoveryMcpServerFetcher, McpClientProperties mcpClientProperties, AbstractMcpClientCreator mcpClientCreator) {
        this.discoveryMcpServerFetcher = discoveryMcpServerFetcher;
        this.mcpClientProperties = mcpClientProperties;
        this.mcpClientCreator = mcpClientCreator;
        ResolvableType resolvableType = ResolvableType.forClass(this.getClass()).getSuperType();
        this.resolvedGenericClass = (Class<T>) resolvableType.getGeneric(0).resolve();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public void onApplicationEvent(DiscoveryMcpServerRefreshEvent event) {
        Map<String, List<McpToolSpecification>> serviceNameToExposeConfigurationMetadatasMap = event.getSource().getMcpServerMetadataCache().getServiceNameToExposeConfigurationMetadatasMap();
        Map<String, T> loadbalancedMcpClientMap = beanFactory.getBeansOfType(resolvedGenericClass);

        loadbalancedMcpClientMap.forEach((beanName, loadbalancedMcpClient) -> {
            if (!serviceNameToExposeConfigurationMetadatasMap.containsKey(loadbalancedMcpClient.getServiceName())) {
                beanFactory.destroySingleton(beanName);
                log.info("Destroyed LoadbalancedMcpClient bean: {}", beanName);
            } else {
                List<McpToolSpecification> newMetadataList = serviceNameToExposeConfigurationMetadatasMap.get(loadbalancedMcpClient.getServiceName());
                loadbalancedMcpClient.refreshExposeConfigurationMetadataList(newMetadataList);
            }
        });

        serviceNameToExposeConfigurationMetadatasMap.forEach((serviceName, exposeConfigurationMetadataList) -> {
            LoadbalancedMcpClient loadbalancedMcpClient = loadbalancedMcpClientMap.get(BEAN_NAME_PREFIX + serviceName);
            if (loadbalancedMcpClient == null) {
                this.registerMcpClients(serviceName, exposeConfigurationMetadataList);
                log.info("Registered new LoadbalancedMcpClient for service: {}", serviceName);
            }
        });

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, List<McpToolSpecification>> serviceNameToExposeConfigurationMetadatasMap =
                discoveryMcpServerFetcher.getMcpServerMetadataCache().getServiceNameToExposeConfigurationMetadatasMap();
        if(!CollectionUtils.isEmpty(serviceNameToExposeConfigurationMetadatasMap)){
            serviceNameToExposeConfigurationMetadatasMap.forEach(this::registerMcpClients);
        } else {
            log.warn("McpServerMetadataCache is null during LoadbalancedMcpClientManager initialization.");
        }
    }

    protected void registerMcpClients(String serviceName, List<McpToolSpecification> mcpToolSpecificationList) {
        T loadbalancedMcpClient = createLoadbalancedMcpClientInstance(serviceName, mcpToolSpecificationList, mcpClientProperties, mcpClientCreator);
        beanFactory.registerSingleton(BEAN_NAME_PREFIX + serviceName, loadbalancedMcpClient);
    }

    protected abstract T createLoadbalancedMcpClientInstance(String serviceName, List<McpToolSpecification> mcpToolSpecificationList,
                                                             McpClientProperties mcpClientProperties, AbstractMcpClientCreator mcpClientCreator);

    public Map<String, T> getLoadbalancedMcpClientMap(){
        return beanFactory.getBeansOfType(resolvedGenericClass);
    }

    public List<T> getLoadbalancedMcpClients(){
        return beanFactory.getBeansOfType(resolvedGenericClass).values().stream().toList();
    }
}
