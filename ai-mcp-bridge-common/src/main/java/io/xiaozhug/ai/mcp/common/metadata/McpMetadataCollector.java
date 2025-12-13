package io.xiaozhug.ai.mcp.common.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * MCP元数据收集器
 *
 * @author xiaozhug
 */
@Slf4j
public class McpMetadataCollector {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static McpMetadata fetchConfigurationMetadata(){
        Enumeration<URL> resources = null;

        try {
            resources = McpMetadataCollector.class.getClassLoader().getResources("META-INF/mcp-metadata.json");
            if(resources == null || !resources.hasMoreElements()){
                log.warn("无法找到META-INF/mcp-metadata.json文件");
                return null;
            }

            McpMetadata metadata = new McpMetadata();

            while (resources != null && resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream inputStream = url.openConnection().getInputStream()){
                    if (inputStream == null) {
                        log.warn("{},无法找到META-INF/mcp-metadata.json文件", url);
                    } else {
                        McpMetadata config = objectMapper.readValue(inputStream, McpMetadata.class);
                        if(Objects.isNull(metadata.getItems())){
                            metadata.setItems(new ArrayList<>());
                        }
                        if(Objects.nonNull(config)){
                            List<McpMetadataItem> items = config.getItems();
                            if(items != null && !items.isEmpty()){
                                metadata.getItems().addAll(config.getItems());
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return metadata;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
