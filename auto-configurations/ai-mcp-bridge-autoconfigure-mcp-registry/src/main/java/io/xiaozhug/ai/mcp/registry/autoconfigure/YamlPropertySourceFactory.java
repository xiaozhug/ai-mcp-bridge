package io.xiaozhug.ai.mcp.registry.autoconfigure;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author xiaozhug
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    private YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        List<PropertySource<?>> sources = loader.load(name != null ? name : resource.getResource().getFilename(), resource.getResource());
        return sources.get(0); // 返回第一个 PropertySource
    }
}