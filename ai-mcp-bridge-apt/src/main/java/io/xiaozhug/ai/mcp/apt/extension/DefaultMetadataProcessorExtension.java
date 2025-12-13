package io.xiaozhug.ai.mcp.apt.extension;

import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;

import javax.lang.model.element.ExecutableElement;

/**
 * 默认的元数据处理器扩展
 *
 * @author xiaozhug
 */
public class DefaultMetadataProcessorExtension implements MetadataProcessorExtension {

    @Override
    public void afterProcessParameter(ExecutableElement methodElement, McpMetadataItem mcpMetadataItem, McpMetadataItem.Param param) {
        String paramType = param.getParamType();
        if (paramType.equals("javax.servlet.http.HttpServletRequest") ||
            paramType.equals("javax.servlet.http.HttpServletResponse") ||
            paramType.equals("jakarta.servlet.http.HttpServletRequest") ||
            paramType.equals("jakarta.servlet.http.HttpServletResponse")) {
            param.setEnabled(false);
        }
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
