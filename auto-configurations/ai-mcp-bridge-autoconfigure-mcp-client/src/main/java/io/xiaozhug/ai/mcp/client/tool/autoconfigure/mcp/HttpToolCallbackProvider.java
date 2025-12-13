package io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.support.ToolUtils;

import java.util.List;

/**
 * HTTP工具回调提供者
 *
 * @author xiaozhug
 */
public interface HttpToolCallbackProvider extends ToolCallbackProvider {

    default void validateToolCallbacks(ToolCallback[] toolCallbacks) {
        List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
        if (!duplicateToolNames.isEmpty()) {
            throw new IllegalStateException(
                    "Multiple tools with the same name (%s)".formatted(String.join(", ", duplicateToolNames)));
        }
    }
}
