package io.xiaozhug.demo.metadata.apt.extension;

import io.xiaozhug.ai.mcp.apt.extension.FieldExplorerExtension;

public class CustomFieldExplorerExtension implements FieldExplorerExtension {


    @Override
    public int getOrder() {
        return 0;
    }
}
