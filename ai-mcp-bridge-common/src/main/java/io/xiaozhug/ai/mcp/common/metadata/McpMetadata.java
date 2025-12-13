package io.xiaozhug.ai.mcp.common.metadata;

import java.util.List;

/**
 * MCP元数据
 *
 * @author xiaozhug
 */
public class McpMetadata {

    private List<McpMetadataItem> items;

    public List<McpMetadataItem> getItems() {
        return items;
    }

    public void setItems(List<McpMetadataItem> items) {
        this.items = items;
    }
}
